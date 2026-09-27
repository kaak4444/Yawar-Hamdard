<?php
declare(strict_types=1);

// Copy to public_html/.private/auth-config.php on the server and fill values there.
// Never commit the live file or place these credentials in the Android app.
if (!defined('YAWAR_AUTH_CONFIG_CONTEXT')) {
    http_response_code(404);
    exit;
}

return [
    'db_host' => 'localhost',
    'db_name' => 'CREATE_A_DEDICATED_APP_DATABASE',
    'db_user' => 'CREATE_A_DEDICATED_APP_DATABASE_USER',
    'db_password' => 'SET_A_LONG_RANDOM_DATABASE_PASSWORD',
    'otp_pepper' => 'SET_A_RANDOM_SECRET_OF_AT_LEAST_32_BYTES',
    'smtp_host' => 'smtp.hostinger.com',
    'smtp_port' => 465,
    'smtp_user' => 'no-reply@yawarconsulting.com',
    'smtp_password' => 'SET_THE_HOSTINGER_MAILBOX_PASSWORD',
    'from_name' => 'Yawar Hamdard',
    'documents_path' => '/path/outside/public_html/yawar-private-documents',
];
