# Hostinger auth API

This PHP API replaces Firebase Authentication for the app's email/password sign-in. It sends six-digit email verification and password-reset codes through Hostinger SMTP using `no-reply@yawarconsulting.com`.

## Before it can go live

1. Create the `no-reply@yawarconsulting.com` mailbox in Hostinger Email. The sender must be a real Hostinger mailbox. Keep its password private; enter it only into the server-side auth configuration.
2. Create a **new, dedicated MySQL database and user** in hPanel. Do not reuse the WordPress database.
3. Import `schema.sql` into that new database using phpMyAdmin.
4. Copy `auth-config.example.php` to `public_html/.private/auth-config.php` and set the new database credentials, a random OTP pepper of at least 32 bytes, and the mailbox password. The existing `.private` folder is intended for server-only files; preserve its deny-access rule.
5. Upload `api/.htaccess`, `api/v1/.htaccess`, and `api/v1/auth.php` under `public_html/api/`. Do not upload `auth-config.example.php` as the live config.
6. Keep the domain on HTTPS. SMTP is configured for `smtp.hostinger.com` on port 465 with implicit TLS; the sender and SMTP username must both be `no-reply@yawarconsulting.com`.

The app API base URL is `https://yawarconsulting.com/api/v1/`. Endpoints accept JSON and return generic messages for registration and password-reset requests. Verification codes expire after 10 minutes, are single use, are stored as keyed hashes, have a five-attempt limit, and are subject to resend and request limits. Passwords use PHP `password_hash`; bearer session tokens are random, stored hashed in MySQL, expire after 30 days, and are encrypted with Android Keystore on the device.

## Account and data migration notes

- Firebase passwords cannot be exported for import into Hostinger. Current Firebase users must register again and verify their email in the new system.
- New public registrations always receive the `patient` role. Grant `doctor` or `admin` only to authorized staff through a controlled database operation; never accept a role from the signup form.
- This API covers authentication, email verification, password reset, and sessions. Hospital/doctor catalogs and patient appointments, claims, and messages are still stored locally or seeded as demo records by the current app. They have **not** been moved to this Hostinger database or made suitable for real patient records.
- Keep all database and SMTP credentials out of GitHub, Android resources, and app requests. The Android app talks only to the HTTPS API; it never connects to MySQL or SMTP directly.
