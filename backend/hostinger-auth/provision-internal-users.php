<?php
declare(strict_types=1);

// Install this file beside auth-config.php in public_html/.private and run it
// from the Hostinger terminal. It is deliberately CLI-only and stores no
// initial passwords: each account gets a random unusable password and its
// owner sets a private password through the normal emailed reset flow.
if (PHP_SAPI !== 'cli') {
    http_response_code(404);
    exit;
}

define('YAWAR_AUTH_CONFIG_CONTEXT', true);
$configPath = __DIR__ . '/auth-config.php';
if (!is_file($configPath)) {
    fwrite(STDERR, "Missing private auth-config.php.\n");
    exit(1);
}
$config = require $configPath;
$db = new PDO(
    'mysql:host=' . $config['db_host'] . ';dbname=' . $config['db_name'] . ';charset=utf8mb4',
    $config['db_user'],
    $config['db_password'],
    [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION, PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC]
);

$staff = [
    'm.ibrahim@yawarconsulting.com' => ['admin', 'Yawar Hamdard Manager'],
    'dr_eimalmalik@yawarconsulting.com' => ['call_center', 'Dr Eimal Malik'],
    'yh24@yawarconsulting.com' => ['call_center', 'YHCS Call Center'],
    'ibrahimkakar182@gmail.com' => ['call_center', 'Ibrahim Kakar'],
    'info@yawarconsulting.com' => ['call_center', 'YHCS Information Desk'],
];

foreach ($staff as $email => [$role, $name]) {
    $db->beginTransaction();
    try {
        $lookup = $db->prepare('SELECT id, password_hash, email_verified_at FROM app_users WHERE email = ? LIMIT 1 FOR UPDATE');
        $lookup->execute([$email]);
        $existing = $lookup->fetch();
        $randomPassword = rtrim(strtr(base64_encode(random_bytes(48)), '+/', '-_'), '=');

        if ($existing === false) {
            $insert = $db->prepare(
                'INSERT INTO app_users (email, password_hash, role, is_active, email_verified_at)
                 VALUES (?, ?, ?, 1, UTC_TIMESTAMP())'
            );
            $insert->execute([$email, password_hash($randomPassword, PASSWORD_DEFAULT), $role]);
            $userId = (int)$db->lastInsertId();
        } else {
            $userId = (int)$existing['id'];
            $passwordHash = $existing['email_verified_at'] === null
                ? password_hash($randomPassword, PASSWORD_DEFAULT)
                : (string)$existing['password_hash'];
            $update = $db->prepare(
                'UPDATE app_users SET password_hash = ?, role = ?, is_active = 1, hospital_id = NULL,
                 email_verified_at = COALESCE(email_verified_at, UTC_TIMESTAMP()) WHERE id = ?'
            );
            $update->execute([$passwordHash, $role, $userId]);
            $revoke = $db->prepare('UPDATE app_sessions SET revoked_at = UTC_TIMESTAMP() WHERE user_id = ? AND revoked_at IS NULL');
            $revoke->execute([$userId]);
        }

        $profile = $db->prepare(
            'INSERT INTO app_profiles (user_id, full_name, phone) VALUES (?, ?, \'\')
             ON DUPLICATE KEY UPDATE full_name = VALUES(full_name)'
        );
        $profile->execute([$userId, $name]);
        $db->commit();
        fwrite(STDOUT, "Provisioned {$role}: {$email}\n");
    } catch (Throwable $error) {
        if ($db->inTransaction()) $db->rollBack();
        fwrite(STDERR, "Could not provision {$email}: {$error->getMessage()}\n");
        exit(1);
    }
}

fwrite(STDOUT, "Done. Each staff member must use Forgot password to set a password they control.\n");
