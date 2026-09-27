<?php
declare(strict_types=1);

header('Content-Type: application/json; charset=utf-8');
header('Cache-Control: no-store, private');
header('X-Content-Type-Options: nosniff');
header('Referrer-Policy: no-referrer');

final class ApiFault extends RuntimeException
{
    public function __construct(public readonly int $status, string $message)
    {
        parent::__construct($message);
    }
}

function respond(int $status, array $payload): never
{
    http_response_code($status);
    echo json_encode($payload, JSON_UNESCAPED_SLASHES | JSON_UNESCAPED_UNICODE);
    exit;
}

function success(array $payload = [], int $status = 200): never
{
    respond($status, ['ok' => true] + $payload);
}

function fail(int $status, string $message): never
{
    respond($status, ['ok' => false, 'error' => $message]);
}

function readJsonBody(): array
{
    $length = (int)($_SERVER['CONTENT_LENGTH'] ?? 0);
    if ($length > 16384) {
        throw new ApiFault(413, 'Request is too large.');
    }
    $raw = file_get_contents('php://input');
    $data = json_decode($raw === false ? '' : $raw, true, 16);
    if (!is_array($data)) {
        throw new ApiFault(400, 'Invalid request.');
    }
    return $data;
}

function requiredString(array $data, string $key, int $maxLength = 512): string
{
    $value = $data[$key] ?? null;
    if (!is_string($value) || $value === '' || strlen($value) > $maxLength) {
        throw new ApiFault(422, 'Please check the information and try again.');
    }
    return $value;
}

function normalizedEmail(array $data): string
{
    $email = strtolower(trim(requiredString($data, 'email', 254)));
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        throw new ApiFault(422, 'Enter a valid email address.');
    }
    return $email;
}

function database(array $config): PDO
{
    return new PDO(
        'mysql:host=' . $config['db_host'] . ';dbname=' . $config['db_name'] . ';charset=utf8mb4',
        $config['db_user'],
        $config['db_password'],
        [
            PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
            PDO::ATTR_EMULATE_PREPARES => false,
            PDO::ATTR_TIMEOUT => 5,
        ]
    );
}

function findUser(PDO $db, string $email): ?array
{
    $query = $db->prepare('SELECT id, email, password_hash, role, email_verified_at, is_active FROM app_users WHERE email = ? LIMIT 1');
    $query->execute([$email]);
    $user = $query->fetch();
    return $user === false ? null : $user;
}

function userPayload(PDO $db, array $user): array
{
    $profileQuery = $db->prepare('SELECT full_name, phone FROM app_profiles WHERE user_id = ? LIMIT 1');
    $profileQuery->execute([(int)$user['id']]);
    $profile = $profileQuery->fetch() ?: [];
    return [
        'id' => (int)$user['id'],
        'email' => (string)$user['email'],
        'role' => in_array($user['role'], ['patient', 'doctor', 'call_center', 'hospital', 'admin'], true) ? $user['role'] : 'patient',
        'full_name' => (string)($profile['full_name'] ?? ''),
        'phone' => (string)($profile['phone'] ?? ''),
    ];
}

function sendOtp(PDO $db, array $config, array $user, string $purpose): void
{
    $db->exec('DELETE FROM email_otp_challenges WHERE created_at < UTC_TIMESTAMP() - INTERVAL 30 DAY LIMIT 100');
    $db->exec('DELETE FROM email_otp_send_limits WHERE created_at < UTC_TIMESTAMP() - INTERVAL 30 DAY LIMIT 100');
    $limit = $db->prepare(
        'SELECT COUNT(*) AS sends, MAX(created_at) AS last_sent FROM email_otp_send_limits
         WHERE user_id = ? AND purpose = ? AND created_at >= UTC_TIMESTAMP() - INTERVAL 24 HOUR'
    );
    $limit->execute([(int)$user['id'], $purpose]);
    $limits = $limit->fetch();
    if ((int)$limits['sends'] >= 10) {
        throw new ApiFault(429, 'Too many code requests. Try again later.');
    }
    if ($limits['last_sent'] !== null && strtotime((string)$limits['last_sent'] . ' UTC') > time() - 60) {
        throw new ApiFault(429, 'Wait one minute before requesting another code.');
    }

    $code = str_pad((string)random_int(0, 999999), 6, '0', STR_PAD_LEFT);
    $codeHash = hash_hmac('sha256', $code, $config['otp_pepper']);
    $db->beginTransaction();
    try {
        $consume = $db->prepare(
            'UPDATE email_otp_challenges SET consumed_at = UTC_TIMESTAMP()
             WHERE user_id = ? AND purpose = ? AND consumed_at IS NULL'
        );
        $consume->execute([(int)$user['id'], $purpose]);
        $insert = $db->prepare(
            'INSERT INTO email_otp_challenges (user_id, purpose, code_hash, expires_at)
             VALUES (?, ?, ?, UTC_TIMESTAMP() + INTERVAL 10 MINUTE)'
        );
        $insert->execute([(int)$user['id'], $purpose, $codeHash]);
        $log = $db->prepare('INSERT INTO email_otp_send_limits (user_id, purpose) VALUES (?, ?)');
        $log->execute([(int)$user['id'], $purpose]);
        $db->commit();
    } catch (Throwable $error) {
        if ($db->inTransaction()) $db->rollBack();
        throw $error;
    }

    try {
        sendSmtpEmail($config, (string)$user['email'], $code, $purpose);
    } catch (Throwable $error) {
        error_log('Yawar auth mail send failed: ' . $error->getMessage());
        $invalidate = $db->prepare(
            'UPDATE email_otp_challenges SET consumed_at = UTC_TIMESTAMP()
             WHERE user_id = ? AND purpose = ? AND code_hash = ? AND consumed_at IS NULL'
        );
        $invalidate->execute([(int)$user['id'], $purpose, $codeHash]);
        throw new ApiFault(503, 'The email could not be sent. Try again later or contact support.');
    }
}

function enforceIpRateLimit(PDO $db, array $config, string $action): void
{
    $limits = [
        'signup' => 12,
        'verify-signup' => 30,
        'resend-signup' => 12,
        'login' => 20,
        'password-reset-start' => 8,
        'password-reset' => 30,
    ];
    if (!isset($limits[$action])) return;
    $ip = (string)($_SERVER['REMOTE_ADDR'] ?? 'unknown');
    $bucket = hash_hmac('sha256', $ip, $config['otp_pepper']);
    $upsert = $db->prepare(
        'INSERT INTO api_rate_limits (bucket_hash, action, window_started_at, request_count)
         VALUES (?, ?, UTC_TIMESTAMP(), 1)
         ON DUPLICATE KEY UPDATE
         request_count = IF(window_started_at < UTC_TIMESTAMP() - INTERVAL 15 MINUTE, 1, request_count + 1),
         window_started_at = IF(window_started_at < UTC_TIMESTAMP() - INTERVAL 15 MINUTE, UTC_TIMESTAMP(), window_started_at)'
    );
    $upsert->execute([$bucket, $action]);
    $query = $db->prepare('SELECT request_count FROM api_rate_limits WHERE bucket_hash = ? AND action = ?');
    $query->execute([$bucket, $action]);
    if ((int)$query->fetchColumn() > $limits[$action]) {
        throw new ApiFault(429, 'Too many requests. Wait a little and try again.');
    }
}

function sendSmtpEmail(array $config, string $recipient, string $code, string $purpose): void
{
    $host = (string)$config['smtp_host'];
    $port = (int)$config['smtp_port'];
    if ($port !== 465) {
        throw new RuntimeException('SMTP port must be 465 with implicit TLS.');
    }
    $context = stream_context_create(['ssl' => [
        'verify_peer' => true,
        'verify_peer_name' => true,
        'peer_name' => $host,
        'SNI_enabled' => true,
    ]]);
    $socket = stream_socket_client('ssl://' . $host . ':' . $port, $errno, $errstr, 15, STREAM_CLIENT_CONNECT, $context);
    if ($socket === false) {
        throw new RuntimeException('Could not connect to SMTP server: ' . $errstr);
    }
    stream_set_timeout($socket, 15);

    try {
        smtpExpect($socket, [220]);
        smtpCommand($socket, 'EHLO yawarconsulting.com', [250]);
        smtpCommand($socket, 'AUTH LOGIN', [334]);
        smtpCommand($socket, base64_encode((string)$config['smtp_user']), [334]);
        smtpCommand($socket, base64_encode((string)$config['smtp_password']), [235]);
        smtpCommand($socket, 'MAIL FROM:<' . $config['smtp_user'] . '>', [250]);
        smtpCommand($socket, 'RCPT TO:<' . $recipient . '>', [250, 251]);
        smtpCommand($socket, 'DATA', [354]);

        $isReset = $purpose === 'reset';
        $subject = $isReset ? 'Your Yawar Hamdard password reset code' : 'Your Yawar Hamdard verification code';
        $purposeText = $isReset ? 'reset your password' : 'verify your email address';
        $body = "Your code to {$purposeText} is: {$code}\r\n\r\n" .
            "The code expires in 10 minutes and can only be used once.\r\n" .
            "If you did not request this code, you can ignore this email.\r\n";
        $headers = [
            'Date: ' . gmdate('D, d M Y H:i:s') . ' +0000',
            'From: ' . (string)$config['from_name'] . ' <' . $config['smtp_user'] . '>',
            'To: <' . $recipient . '>',
            'Subject: ' . $subject,
            'MIME-Version: 1.0',
            'Content-Type: text/plain; charset=UTF-8',
            'Content-Transfer-Encoding: 8bit',
        ];
        $message = implode("\r\n", $headers) . "\r\n\r\n" . $body;
        $message = preg_replace('/\r?\n\./', "\r\n..", $message) ?? $message;
        smtpWrite($socket, $message . "\r\n.\r\n");
        smtpExpect($socket, [250]);
        smtpCommand($socket, 'QUIT', [221]);
    } finally {
        fclose($socket);
    }
}

function smtpExpect($socket, array $expected): void
{
    do {
        $line = fgets($socket, 2048);
        if ($line === false) {
            throw new RuntimeException('SMTP server closed the connection unexpectedly.');
        }
        if (!preg_match('/^(\d{3})([ -])/', $line, $match)) {
            throw new RuntimeException('SMTP server returned an invalid response.');
        }
        $code = (int)$match[1];
        $continues = $match[2] === '-';
    } while ($continues);

    if (!in_array($code, $expected, true)) {
        throw new RuntimeException('SMTP request failed with status ' . $code . '.');
    }
}

function smtpCommand($socket, string $command, array $expected): void
{
    smtpWrite($socket, $command . "\r\n");
    smtpExpect($socket, $expected);
}

function smtpWrite($socket, string $data): void
{
    $offset = 0;
    $length = strlen($data);
    while ($offset < $length) {
        $written = fwrite($socket, substr($data, $offset));
        if ($written === false || $written === 0) {
            throw new RuntimeException('SMTP write failed.');
        }
        $offset += $written;
    }
}

function consumeOtp(PDO $db, array $config, int $userId, string $purpose, string $code): void
{
    $query = $db->prepare(
        'SELECT id, code_hash, expires_at, failed_attempts FROM email_otp_challenges
         WHERE user_id = ? AND purpose = ? AND consumed_at IS NULL
         ORDER BY id DESC LIMIT 1 FOR UPDATE'
    );
    $query->execute([$userId, $purpose]);
    $challenge = $query->fetch();
    if ($challenge === false || strtotime((string)$challenge['expires_at'] . ' UTC') < time()) {
        throw new ApiFault(422, 'The code is invalid or expired. Request a new one.');
    }
    if ((int)$challenge['failed_attempts'] >= 5) {
        throw new ApiFault(429, 'Too many incorrect attempts. Request a new code.');
    }
    $candidateHash = hash_hmac('sha256', $code, $config['otp_pepper']);
    if (!hash_equals((string)$challenge['code_hash'], $candidateHash)) {
        $increment = $db->prepare('UPDATE email_otp_challenges SET failed_attempts = failed_attempts + 1 WHERE id = ?');
        $increment->execute([(int)$challenge['id']]);
        if ($db->inTransaction()) $db->commit();
        throw new ApiFault(422, 'The code is invalid or expired. Request a new one.');
    }
    $consume = $db->prepare('UPDATE email_otp_challenges SET consumed_at = UTC_TIMESTAMP() WHERE id = ?');
    $consume->execute([(int)$challenge['id']]);
}

function createSession(PDO $db, int $userId): string
{
    $token = rtrim(strtr(base64_encode(random_bytes(32)), '+/', '-_'), '=');
    $tokenHash = hash('sha256', $token);
    $insert = $db->prepare(
        'INSERT INTO app_sessions (user_id, token_hash, expires_at)
         VALUES (?, ?, UTC_TIMESTAMP() + INTERVAL 30 DAY)'
    );
    $insert->execute([$userId, $tokenHash]);
    return $token;
}

function bearerToken(): ?string
{
    $header = (string)($_SERVER['HTTP_AUTHORIZATION'] ?? '');
    if (!preg_match('/^Bearer ([A-Za-z0-9_-]{40,100})$/', $header, $match)) {
        return null;
    }
    return $match[1];
}

function authenticatedUser(PDO $db): array
{
    $token = bearerToken();
    if ($token === null) {
        throw new ApiFault(401, 'Please sign in again.');
    }
    $query = $db->prepare(
        'SELECT u.id, u.email, u.role, u.email_verified_at, u.is_active, s.id AS session_id
         FROM app_sessions s JOIN app_users u ON u.id = s.user_id
         WHERE s.token_hash = ? AND s.revoked_at IS NULL AND s.expires_at > UTC_TIMESTAMP()
         LIMIT 1'
    );
    $query->execute([hash('sha256', $token)]);
    $user = $query->fetch();
    if ($user === false || $user['email_verified_at'] === null || (int)$user['is_active'] !== 1) {
        throw new ApiFault(401, 'Please sign in again.');
    }
    $touch = $db->prepare('UPDATE app_sessions SET last_used_at = UTC_TIMESTAMP() WHERE id = ?');
    $touch->execute([(int)$user['session_id']]);
    return $user;
}

try {
    $secure = !empty($_SERVER['HTTPS']) && $_SERVER['HTTPS'] !== 'off';
    if (!$secure) {
        throw new ApiFault(426, 'Use a secure HTTPS connection.');
    }

    $configPath = dirname(__DIR__, 2) . '/.private/auth-config.php';
    if (!is_file($configPath)) {
        throw new RuntimeException('The server auth configuration is not installed.');
    }
    if (!defined('YAWAR_AUTH_CONFIG_CONTEXT')) {
        define('YAWAR_AUTH_CONFIG_CONTEXT', true);
    }
    $config = require $configPath;
    foreach (['db_host', 'db_name', 'db_user', 'db_password', 'otp_pepper', 'smtp_host', 'smtp_port', 'smtp_user', 'smtp_password', 'from_name'] as $key) {
        if (!isset($config[$key]) || $config[$key] === '' || str_starts_with((string)$config[$key], 'SET_') || str_starts_with((string)$config[$key], 'CREATE_')) {
            throw new RuntimeException('A required server configuration value is missing.');
        }
    }
    if (strlen((string)$config['otp_pepper']) < 32) {
        throw new RuntimeException('The OTP signing secret must contain at least 32 bytes.');
    }
    if (!hash_equals('no-reply@yawarconsulting.com', strtolower((string)$config['smtp_user']))) {
        throw new RuntimeException('The configured SMTP sender must be no-reply@yawarconsulting.com.');
    }
    $db = database($config);
    $action = (string)($_GET['action'] ?? '');
    $method = strtoupper((string)($_SERVER['REQUEST_METHOD'] ?? 'GET'));

    if ($action === 'me' && $method === 'GET') {
        $user = authenticatedUser($db);
        success(['user' => userPayload($db, $user)]);
    }
    if ($action === 'logout' && $method === 'POST') {
        $token = bearerToken();
        if ($token !== null) {
            $query = $db->prepare('UPDATE app_sessions SET revoked_at = UTC_TIMESTAMP() WHERE token_hash = ? AND revoked_at IS NULL');
            $query->execute([hash('sha256', $token)]);
        }
        success(['message' => 'Signed out.']);
    }
    if ($method !== 'POST') {
        throw new ApiFault(405, 'This request method is not supported.');
    }
    enforceIpRateLimit($db, $config, $action);
    $body = readJsonBody();
    $email = normalizedEmail($body);

    if ($action === 'signup') {
        $password = requiredString($body, 'password', 200);
        if (strlen($password) < 12) throw new ApiFault(422, 'Use a password with at least 12 characters.');
        $role = requiredString($body, 'role', 16);
        if (!in_array($role, ['patient', 'doctor'], true)) {
            throw new ApiFault(422, 'Choose patient or doctor registration. Staff accounts are invitation-only.');
        }
        $fullName = trim(requiredString($body, 'full_name', 120));
        $phone = trim(requiredString($body, 'phone', 32));
        if (!preg_match('/^.{2,}$/u', $fullName) || !preg_match('/^[+0-9() .-]{7,32}$/', $phone)) {
            throw new ApiFault(422, 'Enter a valid full name and phone number.');
        }
        $specialty = trim((string)($body['specialty'] ?? ''));
        $licenseNumber = trim((string)($body['license_number'] ?? ''));
        if ($role === 'doctor' && ($specialty === '' || $licenseNumber === '')) {
            throw new ApiFault(422, 'Doctors must provide a specialty and license number.');
        }
        $staffEmails = [
            'm.ibrahim@yawarconsulting.com',
            'dr_eimalmalik@yawarconsulting.com',
            'yh24@yawarconsulting.com',
            'ibrahimkakar182@gmail.com',
            'info@yawarconsulting.com',
        ];
        if (in_array($email, $staffEmails, true)) {
            throw new ApiFault(403, 'This is a staff account. Use the account created by the manager.');
        }
        $user = findUser($db, $email);
        if ($user !== null && (int)$user['is_active'] !== 1) {
            throw new ApiFault(403, 'This account is unavailable. Contact the Yawar manager.');
        }
        if ($user === null) {
            $db->beginTransaction();
            try {
                $insert = $db->prepare('INSERT INTO app_users (email, password_hash, role) VALUES (?, ?, ?)');
                $insert->execute([$email, password_hash($password, PASSWORD_DEFAULT), $role]);
                $user = findUser($db, $email);
                if ($user === null) throw new RuntimeException('New account could not be loaded.');
                $profileInsert = $db->prepare('INSERT INTO app_profiles (user_id, full_name, phone) VALUES (?, ?, ?)');
                $profileInsert->execute([(int)$user['id'], $fullName, $phone]);
                if ($role === 'doctor') {
                    $doctorInsert = $db->prepare('INSERT INTO doctor_profiles (user_id, specialty, license_number) VALUES (?, ?, ?)');
                    $doctorInsert->execute([(int)$user['id'], $specialty, $licenseNumber]);
                }
                $db->commit();
            } catch (Throwable $error) {
                if ($db->inTransaction()) $db->rollBack();
                throw $error;
            }
        } elseif ($user['email_verified_at'] === null && !password_verify($password, (string)$user['password_hash'])) {
            // Keep the response indistinguishable for unknown and already-used addresses.
            success(['message' => 'If this address can be registered, a verification code has been sent.']);
        } elseif ($user['email_verified_at'] === null && $user['role'] !== $role) {
            throw new ApiFault(409, 'This email already has a pending registration under another account type.');
        }
        if ($user !== null && $user['email_verified_at'] === null) {
            sendOtp($db, $config, $user, 'signup');
        }
        success(['message' => 'If this address can be registered, a verification code has been sent.']);
    }

    if ($action === 'verify-signup') {
        $code = requiredString($body, 'code', 6);
        if (!preg_match('/^\d{6}$/', $code)) throw new ApiFault(422, 'Enter the six-digit code from your email.');
        $user = findUser($db, $email);
        if ($user === null || $user['email_verified_at'] !== null) {
            throw new ApiFault(422, 'The code is invalid or expired. Request a new one.');
        }
        $db->beginTransaction();
        consumeOtp($db, $config, (int)$user['id'], 'signup', $code);
        $verify = $db->prepare('UPDATE app_users SET email_verified_at = UTC_TIMESTAMP() WHERE id = ? AND email_verified_at IS NULL');
        $verify->execute([(int)$user['id']]);
        $user['email_verified_at'] = gmdate('Y-m-d H:i:s');
        $token = createSession($db, (int)$user['id']);
        $db->commit();
        success(['token' => $token, 'user' => userPayload($db, $user)]);
    }

    if ($action === 'resend-signup') {
        $user = findUser($db, $email);
        if ($user !== null && $user['email_verified_at'] === null) {
            sendOtp($db, $config, $user, 'signup');
        }
        success(['message' => 'If this account is awaiting verification, a new code has been sent.']);
    }

    if ($action === 'login') {
        $password = requiredString($body, 'password', 200);
        $user = findUser($db, $email);
        if ($user === null || (int)$user['is_active'] !== 1 || !password_verify($password, (string)$user['password_hash'])) {
            throw new ApiFault(401, 'Email or password is incorrect.');
        }
        if ($user['email_verified_at'] === null) {
            throw new ApiFault(403, 'Verify your email address before signing in.');
        }
        $token = createSession($db, (int)$user['id']);
        success(['token' => $token, 'user' => userPayload($db, $user)]);
    }

    if ($action === 'password-reset-start') {
        $user = findUser($db, $email);
        if ($user !== null && (int)$user['is_active'] === 1 && $user['email_verified_at'] !== null) {
            try {
                sendOtp($db, $config, $user, 'reset');
            } catch (ApiFault $error) {
                if (!in_array($error->status, [429, 503], true)) throw $error;
                error_log('Yawar password reset email was suppressed by a delivery or rate limit.');
            }
        }
        success(['message' => 'If an account exists for that address, a password reset code has been sent.']);
    }

    if ($action === 'password-reset') {
        $code = requiredString($body, 'code', 6);
        $password = requiredString($body, 'password', 200);
        if (!preg_match('/^\d{6}$/', $code)) throw new ApiFault(422, 'Enter the six-digit code from your email.');
        if (strlen($password) < 12) throw new ApiFault(422, 'Use a password with at least 12 characters.');
        $user = findUser($db, $email);
        if ($user === null || (int)$user['is_active'] !== 1 || $user['email_verified_at'] === null) {
            throw new ApiFault(422, 'The code is invalid or expired. Request a new one.');
        }
        $db->beginTransaction();
        consumeOtp($db, $config, (int)$user['id'], 'reset', $code);
        $update = $db->prepare('UPDATE app_users SET password_hash = ? WHERE id = ?');
        $update->execute([password_hash($password, PASSWORD_DEFAULT), (int)$user['id']]);
        $revoke = $db->prepare('UPDATE app_sessions SET revoked_at = UTC_TIMESTAMP() WHERE user_id = ? AND revoked_at IS NULL');
        $revoke->execute([(int)$user['id']]);
        $db->commit();
        success(['message' => 'Your password has been changed. Sign in with the new password.']);
    }

    throw new ApiFault(404, 'This request is not available.');
} catch (ApiFault $error) {
    if (isset($db) && $db instanceof PDO && $db->inTransaction()) $db->rollBack();
    fail($error->status, $error->getMessage());
} catch (Throwable $error) {
    if (isset($db) && $db instanceof PDO && $db->inTransaction()) $db->rollBack();
    error_log('Yawar auth API error: ' . $error->getMessage());
    fail(500, 'The sign-in service is temporarily unavailable. Try again later.');
}
