# Hostinger authentication and care workflow

This backend is the shared service for the Android app. It stores account profiles, patient care requests, role-scoped messages, hospital referrals, private hospital documents, and monthly hospital payouts in one dedicated Yawar app database. The Android app never connects directly to MySQL.

## Server setup

Complete these steps on the Hostinger account before using real accounts or patient records:

1. Create or confirm the `no-reply@yawarconsulting.com` mailbox. The sender and SMTP username must match.
2. Create a **dedicated app MySQL database and user** in hPanel. Do not use the WordPress database.
3. Import `schema.sql`, then `workflow-schema.sql`, then `hospital-catalog-seed.sql`, in that order. The workflow SQL contains an `ALTER TABLE`; run it once only after taking a database backup.
4. Copy `auth-config.example.php` to `public_html/.private/auth-config.php`. Set the dedicated database credentials, a random OTP pepper of at least 32 bytes, the Hostinger mailbox password, and an absolute `documents_path` outside `public_html`.
5. Keep `.private` inaccessible over HTTP. Patient files must stay in the configured private directory, outside the website document root.
6. Upload `api/.htaccess`, `api/v1/.htaccess`, and `api/v1/auth.php` under `public_html/api/`. Do not upload the example config as the live config.
7. Upload `wordpress/yh-care-workflow/yh-care-workflow.php` as the `yh-care-workflow` WordPress plugin and activate it. It reads the same private config and app database as the auth API.
8. Put real contact email addresses on hospital records and create each hospital login in the manager dashboard. The imported workbook has hospital contact details and logos, but no contact email addresses.
9. Store `provision-internal-users.php` privately beside the live config, run it from Hostinger Terminal (CLI only), then remove the provisioning script from the server. It creates the one manager and four call-center accounts with random unusable passwords; each account owner must use **Forgot password** to set their own password.
10. Configure a real Hostinger cron job to invoke WordPress `wp-cron.php` regularly (for example every 15 minutes). WordPress's visitor-triggered scheduler alone does not guarantee daily reminder delivery. PHP upload limits must allow a 15 MB file plus multipart overhead.

The Android clients use `https://yawarconsulting.com/api/v1/` for authentication and `https://yawarconsulting.com/wp-json/yh/v1/` for the workflow. Keep both on HTTPS.

Authenticated patient requests, messages, and the current user's profile are held in app memory while signed in and cleared on logout; the app does not write those workflow records to the local Room database. The hospital and doctor directory is cached locally. Keep account data out of screenshots and device backups when operating the app on shared devices.

## Account roles

- Public sign-up accepts patient and doctor accounts only.
- The manager identity is restricted to `m.ibrahim@yawarconsulting.com` by the provisioning script and API.
- Call-center access is restricted to the four allowlisted addresses in the provisioning script and API.
- Hospital accounts are created by the manager and are scoped to the assigned hospital.
- Doctors only see requests assigned to their own verified doctor profile. The manager or call center must assign a verified doctor after the hospital referral exists.

Passwords and SMTP/database credentials are not stored in this repository. The supplied staff passwords were deliberately not copied into code; set each account password privately through the emailed reset flow.

## Workflow behavior and limits

- Patients create a request; the call center reviews it; then the patient or call center can route it to a hospital.
- In-app care messages are shared with the patient and staff whose roles have access to that request.
- Hospital accounts can upload PDF, JPG, and PNG records up to 15 MB each. Documents are served only through an authenticated role-checked endpoint and stored outside the public web root.
- Referral and daily pending-record emails use Hostinger SMTP. A daily reminder is recorded only after the mailer reports success, and reminders stop after the hospital uploads a document or completes the referral. Reliable scheduling also requires the Hostinger cron job above.
- The WhatsApp button opens a message draft in WhatsApp; it cannot send automatically without WhatsApp Business API credentials and the recipient's required opt-in. A staff member must press Send in WhatsApp.
- The call button opens the phone dialer. In-app internet calling would need a voice service and credentials that are not configured here.
- The payout screen records the amount, month, due date, notes, and status supplied by staff; it does not initiate bank transfers.

## Local checks

The PHP files can be syntax-checked with `php -l`. No credentials or live database are needed to inspect or edit this source. The workflow becomes live only after the Hostinger migrations, auth API, SMTP configuration, plugin, staff provisioning, and cron setup are complete.
