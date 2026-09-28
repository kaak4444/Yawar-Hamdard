<?php
/**
 * Plugin Name: Yawar Care Workflow API
 * Description: Role-protected care routing, direct YHCS messages, hospital records and payment tracking for the Yawar Hamdard Android app.
 * Version: 1.1.0
 */
declare(strict_types=1);

if (!defined('ABSPATH')) exit;

function yh_workflow_db(): PDO
{
    static $db = null;
    if ($db instanceof PDO) return $db;
    $configPath = dirname(__DIR__, 3) . '/.private/auth-config.php';
    if (!is_file($configPath)) throw new RuntimeException('The private Yawar auth configuration is not installed.');
    if (!defined('YAWAR_AUTH_CONFIG_CONTEXT')) define('YAWAR_AUTH_CONFIG_CONTEXT', true);
    $config = require $configPath;
    $db = new PDO(
        'mysql:host=' . $config['db_host'] . ';dbname=' . $config['db_name'] . ';charset=utf8mb4',
        $config['db_user'],
        $config['db_password'],
        [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION, PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC, PDO::ATTR_EMULATE_PREPARES => false]
    );
    return $db;
}

function yh_ensure_direct_message_tables(PDO $db): void
{
    static $ready = false;
    if ($ready) return;
    $db->exec(
        'CREATE TABLE IF NOT EXISTS care_direct_conversations (
            id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
            support_key VARCHAR(8) NOT NULL,
            support_user_id BIGINT UNSIGNED NOT NULL,
            participant_user_id BIGINT UNSIGNED NOT NULL,
            created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
            PRIMARY KEY (id),
            UNIQUE KEY uq_direct_support_participant (support_user_id, participant_user_id),
            KEY idx_direct_participant_updated (participant_user_id, updated_at),
            KEY idx_direct_support_updated (support_user_id, updated_at),
            CONSTRAINT fk_direct_support_user FOREIGN KEY (support_user_id) REFERENCES app_users(id) ON DELETE RESTRICT,
            CONSTRAINT fk_direct_participant_user FOREIGN KEY (participant_user_id) REFERENCES app_users(id) ON DELETE RESTRICT
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci'
    );
    $db->exec(
        'CREATE TABLE IF NOT EXISTS care_direct_messages (
            id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
            conversation_id BIGINT UNSIGNED NOT NULL,
            sender_user_id BIGINT UNSIGNED NOT NULL,
            body TEXT NOT NULL,
            created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
            PRIMARY KEY (id),
            KEY idx_direct_message_conversation (conversation_id, created_at),
            CONSTRAINT fk_direct_message_conversation FOREIGN KEY (conversation_id) REFERENCES care_direct_conversations(id) ON DELETE CASCADE,
            CONSTRAINT fk_direct_message_sender FOREIGN KEY (sender_user_id) REFERENCES app_users(id) ON DELETE RESTRICT
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci'
    );
    $ready = true;
}

function yh_direct_support_profiles(): array
{
    return [
        'YHCS1' => ['email' => 'info@yawarconsulting.com', 'role' => 'call_center'],
        'YHCS2' => ['email' => 'ibrahimkakar182@gmail.com', 'role' => 'admin'],
    ];
}

function yh_private_config(): array
{
    $configPath = dirname(__DIR__, 3) . '/.private/auth-config.php';
    if (!is_file($configPath)) throw new RuntimeException('The private Yawar auth configuration is not installed.');
    if (!defined('YAWAR_AUTH_CONFIG_CONTEXT')) define('YAWAR_AUTH_CONFIG_CONTEXT', true);
    return require $configPath;
}

function yh_error(int $status, string $message): WP_Error
{
    return new WP_Error('yawar_workflow_error', $message, ['status' => $status]);
}

function yh_ok(array $data = [], int $status = 200): WP_REST_Response
{
    return new WP_REST_Response(['ok' => true, 'data' => $data], $status);
}

function yh_body(WP_REST_Request $request): array
{
    $body = $request->get_json_params();
    return is_array($body) ? $body : [];
}

function yh_actor(WP_REST_Request $request): array|WP_Error
{
    $header = (string)$request->get_header('authorization');
    if (!preg_match('/^Bearer ([A-Za-z0-9_-]{40,100})$/', $header, $match)) return yh_error(401, 'Please sign in again.');
    try {
        $db = yh_workflow_db();
        $query = $db->prepare(
            'SELECT u.id, u.email, u.role, u.hospital_id, p.full_name, p.phone, s.id AS session_id
             FROM app_sessions s JOIN app_users u ON u.id = s.user_id
             LEFT JOIN app_profiles p ON p.user_id = u.id
             WHERE s.token_hash = ? AND s.revoked_at IS NULL AND s.expires_at > UTC_TIMESTAMP()
               AND u.email_verified_at IS NOT NULL AND u.is_active = 1
               AND (u.role <> \'hospital\' OR (u.hospital_id IS NOT NULL AND EXISTS (
                    SELECT 1 FROM care_hospitals h WHERE h.id = u.hospital_id AND h.active = 1
               ))) LIMIT 1'
        );
        $query->execute([hash('sha256', $match[1])]);
        $actor = $query->fetch();
        if ($actor === false) return yh_error(401, 'Please sign in again.');
        $email = strtolower((string)$actor['email']);
        if ($actor['role'] === 'admin' && !in_array($email, ['m.ibrahim@yawarconsulting.com', 'ibrahimkakar182@gmail.com'], true)) {
            return yh_error(403, 'This manager account is not authorized.');
        }
        if ($actor['role'] === 'call_center' && !in_array($email, [
            'dr_eimalmalik@yawarconsulting.com', 'yh24@yawarconsulting.com',
            'info@yawarconsulting.com',
        ], true)) {
            return yh_error(403, 'This call-center account is not authorized.');
        }
        $db->prepare('UPDATE app_sessions SET last_used_at = UTC_TIMESTAMP() WHERE id = ?')->execute([(int)$actor['session_id']]);
        return $actor;
    } catch (Throwable $error) {
        error_log('Yawar workflow authentication error: ' . $error->getMessage());
        return yh_error(503, 'The care service is temporarily unavailable.');
    }
}

function yh_roles(array $actor, array $roles): bool|WP_Error
{
    return in_array($actor['role'], $roles, true) ? true : yh_error(403, 'Your account does not have access to this action.');
}

function yh_request_access(PDO $db, array $actor, string $id): bool
{
    $query = $db->prepare('SELECT patient_user_id, assigned_hospital_id, assigned_doctor_id FROM care_requests WHERE id = ? LIMIT 1');
    $query->execute([$id]);
    $row = $query->fetch();
    if ($row === false) return false;
    return match ($actor['role']) {
        'admin', 'call_center' => true,
        'patient' => (int)$row['patient_user_id'] === (int)$actor['id'],
        'hospital' => $actor['hospital_id'] !== null && (int)$row['assigned_hospital_id'] === (int)$actor['hospital_id'],
        'doctor' => doctorOwnsRequest($db, $actor, $row['assigned_doctor_id']),
        default => false,
    };
}

function doctorOwnsRequest(PDO $db, array $actor, mixed $doctorProfileId): bool
{
    if ($doctorProfileId === null) return false;
    $query = $db->prepare('SELECT 1 FROM doctor_profiles WHERE id = ? AND user_id = ? AND verified_at IS NOT NULL AND is_active = 1');
    $query->execute([(int)$doctorProfileId, (int)$actor['id']]);
    return $query->fetchColumn() !== false;
}

function yh_get_request(PDO $db, string $id): ?array
{
    $query = $db->prepare(
        'SELECT r.*, h.name AS hospital_name, h.phone AS hospital_phone, h.whatsapp_phone AS hospital_whatsapp_phone,
                d.id AS doctor_profile_id, p.full_name AS doctor_name
         FROM care_requests r
         LEFT JOIN care_hospitals h ON h.id = r.assigned_hospital_id
         LEFT JOIN doctor_profiles d ON d.id = r.assigned_doctor_id
         LEFT JOIN app_profiles p ON p.user_id = d.user_id
         WHERE r.id = ? LIMIT 1'
    );
    $query->execute([$id]);
    $row = $query->fetch();
    return $row === false ? null : $row;
}

function yh_request_json(PDO $db, array $row): array
{
    $documents = $db->prepare('SELECT id, original_name FROM care_documents WHERE request_id = ? ORDER BY created_at');
    $documents->execute([$row['id']]);
    $files = $documents->fetchAll();
    return [
        'id' => (string)$row['id'],
        'patientName' => (string)$row['patient_name'],
        'patientPhone' => (string)$row['patient_phone'],
        'isDependent' => false,
        'dependentName' => '',
        'dependentRelation' => '',
        'specialty' => (string)$row['specialty'],
        'reasonForCare' => (string)$row['reason_for_care'],
        'symptomsSummary' => (string)$row['symptoms_summary'],
        'urgency' => (string)$row['urgency'],
        'visitType' => (string)$row['visit_type'],
        'province' => (string)$row['province'],
        'facilityId' => $row['assigned_hospital_id'] === null ? '' : (string)$row['assigned_hospital_id'],
        'facilityName' => (string)($row['hospital_name'] ?? ''),
        'doctorId' => $row['doctor_profile_id'] === null ? '' : (string)$row['doctor_profile_id'],
        'doctorName' => (string)($row['doctor_name'] ?? ''),
        'appointmentDate' => (string)$row['appointment_date'],
        'timeSlot' => (string)$row['time_slot'],
        'corporateMemberId' => '',
        'attachedDocuments' => array_map(static fn(array $file): string => (string)$file['original_name'], $files),
        'uploadedDocumentIds' => array_map(static fn(array $file): string => (string)$file['id'], $files),
        'contactPreference' => 'In-App and WhatsApp',
        'status' => (string)$row['status'],
        'coordinatorNotes' => (string)$row['coordinator_notes'],
        'doctorPreparationNotes' => '',
        'emailAlertSentTo' => '',
        'createdAtTimestamp' => (int)(strtotime((string)$row['created_at'] . ' UTC') * 1000),
    ];
}

function yh_hospital_json(array $row): array
{
    return [
        'id' => (string)$row['id'], 'name' => (string)$row['name'], 'facilityType' => 'Partner Hospital',
        'province' => (string)$row['province'], 'district' => (string)$row['city'], 'address' => (string)$row['address'],
        'distanceKm' => 0.0, 'travelTimeMin' => 0, 'hasEmergency24h' => false,
        'emergencyPhone' => (string)$row['phone'], 'contactPhone' => (string)$row['phone'],
        'departments' => [], 'operatingHours' => 'Contact hospital for hours', 'acceptedProgrammes' => [],
        'latitude' => 0.0, 'longitude' => 0.0, 'verificationStatus' => 'Partner Hospital',
        'mapSearchUrl' => '', 'logoFile' => (string)$row['logo_url'],
        'email' => (string)$row['email'], 'whatsappPhone' => (string)$row['whatsapp_phone'],
    ];
}

function yh_send_workflow_email(string $recipient, string $subject, string $message): bool
{
    return wp_mail($recipient, $subject, $message, ['From: Yawar Hamdard <no-reply@yawarconsulting.com>']);
}

add_action('phpmailer_init', static function ($mailer): void {
    try {
        $config = yh_private_config();
        if (empty($config['smtp_host']) || empty($config['smtp_user']) || empty($config['smtp_password']) || (int)$config['smtp_port'] !== 465) return;
        $mailer->isSMTP();
        $mailer->Host = (string)$config['smtp_host'];
        $mailer->Port = 465;
        $mailer->SMTPSecure = 'ssl';
        $mailer->SMTPAuth = true;
        $mailer->Username = (string)$config['smtp_user'];
        $mailer->Password = (string)$config['smtp_password'];
        $mailer->SMTPAutoTLS = false;
        $mailer->setFrom('no-reply@yawarconsulting.com', 'Yawar Hamdard', false);
    } catch (Throwable $error) {
        error_log('Yawar workflow SMTP setup failed: ' . $error->getMessage());
    }
});

function yh_dashboard(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    try {
        $db = yh_workflow_db();
        $where = '1=1';
        $params = [];
        if ($actor['role'] === 'patient') { $where = 'r.patient_user_id = ?'; $params[] = (int)$actor['id']; }
        elseif ($actor['role'] === 'hospital') { $where = 'r.assigned_hospital_id = ?'; $params[] = (int)($actor['hospital_id'] ?? 0); }
        elseif ($actor['role'] === 'doctor') {
            $where = 'r.assigned_doctor_id IN (SELECT id FROM doctor_profiles WHERE user_id = ? AND verified_at IS NOT NULL)';
            $params[] = (int)$actor['id'];
        }
        $query = $db->prepare(
            'SELECT r.*, h.name AS hospital_name, d.id AS doctor_profile_id, p.full_name AS doctor_name
             FROM care_requests r LEFT JOIN care_hospitals h ON h.id = r.assigned_hospital_id
             LEFT JOIN doctor_profiles d ON d.id = r.assigned_doctor_id
             LEFT JOIN app_profiles p ON p.user_id = d.user_id WHERE ' . $where . ' ORDER BY r.created_at DESC LIMIT 500'
        );
        $query->execute($params);
        $requests = array_map(static fn(array $row): array => yh_request_json($db, $row), $query->fetchAll());

        $visibleHospitals = $db->query('SELECT * FROM care_hospitals WHERE active = 1 ORDER BY name')->fetchAll();
        $doctorSql =
            'SELECT d.id, d.specialty, d.subspecialty, d.license_number, d.hospital_id, d.verified_at,
                    d.photo_url, p.full_name, p.phone, h.name AS hospital_name
             FROM doctor_profiles d JOIN app_profiles p ON p.user_id = d.user_id
             LEFT JOIN care_hospitals h ON h.id = d.hospital_id
             WHERE d.is_active = 1';
        $doctorParams = [];
        if ($actor['role'] === 'doctor') {
            $doctorSql .= ' AND d.user_id = ?';
            $doctorParams[] = (int)$actor['id'];
        } elseif ($actor['role'] !== 'admin') {
            $doctorSql .= ' AND d.verified_at IS NOT NULL';
        }
        $doctorSql .= ' ORDER BY p.full_name';
        $doctorQuery = $db->prepare($doctorSql);
        $doctorQuery->execute($doctorParams);
        $doctors = $doctorQuery->fetchAll();

        $messages = [];
        $requestIds = array_column($requests, 'id');
        if ($requestIds !== []) {
            $marks = implode(',', array_fill(0, count($requestIds), '?'));
            $messageQuery = $db->prepare(
                'SELECT m.id, m.request_id, m.sender_user_id, m.body, m.created_at, u.role, p.full_name
                 FROM care_messages m JOIN app_users u ON u.id = m.sender_user_id
                 LEFT JOIN app_profiles p ON p.user_id = u.id
                 WHERE m.request_id IN (' . $marks . ') ORDER BY m.created_at ASC LIMIT 2000'
            );
            $messageQuery->execute($requestIds);
            foreach ($messageQuery->fetchAll() as $row) {
                $messages[] = [
                    'id' => 'remote_' . $row['id'], 'senderRole' => strtoupper((string)$row['role']),
                    'senderName' => (string)($row['full_name'] ?? 'Yawar Care Team'), 'content' => (string)$row['body'],
                    'timestamp' => (int)(strtotime((string)$row['created_at'] . ' UTC') * 1000), 'isRead' => false,
                    'conversationId' => 'case_' . (string)$row['request_id'], 'messageType' => 'TEXT',
                    'attachmentName' => '', 'attachmentSize' => '', 'voiceDurationSec' => 0, 'deliveryStatus' => 'SENT',
                ];
            }
        }

        $paymentQuery = $db->prepare(
            'SELECT pm.*, h.name AS hospital_name FROM monthly_hospital_payments pm
             JOIN care_hospitals h ON h.id = pm.hospital_id
             WHERE ? IN (\'admin\', \'call_center\') OR pm.hospital_id = ?
             ORDER BY pm.due_date DESC LIMIT 300'
        );
        $paymentQuery->execute([(string)$actor['role'], (int)($actor['hospital_id'] ?? 0)]);
        $payments = array_map(static fn(array $p): array => [
            'id' => (string)$p['id'], 'hospitalId' => (string)$p['hospital_id'], 'hospitalName' => (string)$p['hospital_name'],
            'month' => (string)$p['payment_month'], 'amountAf' => (float)$p['amount_af'], 'dueDate' => (string)$p['due_date'],
            'paidAt' => $p['paid_at'] === null ? '' : (string)$p['paid_at'], 'status' => (string)$p['status'], 'notes' => (string)$p['notes'],
        ], $paymentQuery->fetchAll());

        return yh_ok([
            'requests' => $requests,
            'messages' => $messages,
            'hospitals' => array_map('yh_hospital_json', $visibleHospitals),
            'doctors' => array_map(static fn(array $d): array => [
                'id' => (string)$d['id'], 'name' => (string)$d['full_name'], 'specialty' => (string)$d['specialty'],
                'subspecialty' => (string)$d['subspecialty'], 'hospitalAffiliation' => (string)($d['hospital_name'] ?? ''),
                'province' => '', 'city' => '', 'languages' => [], 'yearsExperience' => 0, 'earliestAvailable' => '',
                'inPersonAvailable' => true, 'virtualAvailable' => false, 'homeVisitAvailable' => false,
                'consultationFeeAf' => '', 'isVerified' => $d['verified_at'] !== null, 'distanceKm' => 0.0, 'education' => '',
                'licenseNo' => (string)$d['license_number'], 'weeklySchedule' => '', 'preparationNote' => '',
                'verificationStatus' => $d['verified_at'] !== null ? 'Verified Specialist' : 'Pending Verification',
                'photoFile' => (string)$d['photo_url'], 'bio' => '', 'phone' => (string)$d['phone'],
            ], $doctors),
            'payments' => $payments,
            'profile' => ['id' => 'current_user', 'fullName' => (string)($actor['full_name'] ?? ''), 'phone' => (string)($actor['phone'] ?? ''), 'email' => (string)$actor['email']],
        ]);
    } catch (Throwable $error) {
        error_log('Yawar dashboard read failed: ' . $error->getMessage());
        return yh_error(503, 'Could not load shared care records.');
    }
}

function yh_create_request(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    if (($role = yh_roles($actor, ['patient'])) !== true) return $role;
    $body = yh_body($request);
    $reason = trim((string)($body['reasonForCare'] ?? ''));
    if ($reason === '' || strlen($reason) > 1000) return yh_error(422, 'Describe the problem you need help with.');
    if (($body['consentGiven'] ?? false) !== true) return yh_error(422, 'Consent is required before you send this care request.');
    try {
        $db = yh_workflow_db();
        $id = 'YH-' . gmdate('ymd') . '-' . strtoupper(bin2hex(random_bytes(3)));
        $insert = $db->prepare(
            'INSERT INTO care_requests (id, patient_user_id, patient_name, patient_phone, specialty, reason_for_care,
             symptoms_summary, urgency, visit_type, province, appointment_date, time_slot, status, coordinator_notes, consented_at)
             VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, \'SUBMITTED\', \'Received by the Yawar call center.\', UTC_TIMESTAMP())'
        );
        $insert->execute([
            $id, (int)$actor['id'], (string)$actor['full_name'], (string)$actor['phone'],
            substr(trim((string)($body['specialty'] ?? 'General consultation')), 0, 120), $reason,
            substr(trim((string)($body['symptomsSummary'] ?? '')), 0, 5000),
            in_array(($body['urgency'] ?? ''), ['Routine', 'Priority', 'Urgent'], true) ? $body['urgency'] : 'Routine',
            in_array(($body['visitType'] ?? ''), ['IN_PERSON', 'VIRTUAL', 'HOME_VISIT'], true) ? $body['visitType'] : 'IN_PERSON',
            substr(trim((string)($body['province'] ?? '')), 0, 80), substr(trim((string)($body['appointmentDate'] ?? '')), 0, 40),
            substr(trim((string)($body['timeSlot'] ?? '')), 0, 40),
        ]);
        $created = yh_get_request($db, $id);
        return yh_ok(['request' => yh_request_json($db, $created)], 201);
    } catch (Throwable $error) {
        error_log('Yawar request create failed: ' . $error->getMessage());
        return yh_error(503, 'Could not submit your request. Try again when you have a connection.');
    }
}

function yh_route_request(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    $db = yh_workflow_db();
    $id = sanitize_text_field((string)$request['request_id']);
    $body = yh_body($request);
    $hospitalId = (int)($body['hospitalId'] ?? 0);
    if ($hospitalId < 1) return yh_error(422, 'Choose a hospital first.');
    $hospitalQuery = $db->prepare('SELECT id, name, email FROM care_hospitals WHERE id = ? AND active = 1 LIMIT 1');
    $hospitalQuery->execute([$hospitalId]);
    $hospital = $hospitalQuery->fetch();
    if ($hospital === false) return yh_error(404, 'That hospital is not available.');
    if (!yh_request_access($db, $actor, $id)) return yh_error(403, 'You cannot access this care request.');
    $row = yh_get_request($db, $id);
    if ($actor['role'] === 'patient' && !in_array($row['status'], ['UNDER_REVIEW', 'AWAITING_PROVIDER'], true)) {
        return yh_error(409, 'The call center needs to review your request before you send it to a hospital.');
    }
    if (!in_array($actor['role'], ['patient', 'call_center', 'admin'], true)) return yh_error(403, 'Only the patient or Yawar call center can send this referral.');
    $update = $db->prepare('UPDATE care_requests SET assigned_hospital_id = ?, status = \'AWAITING_PROVIDER\' WHERE id = ?');
    $update->execute([$hospitalId, $id]);
    $newRow = yh_get_request($db, $id);
    if (is_array($newRow) && filter_var((string)$hospital['email'], FILTER_VALIDATE_EMAIL)) {
        $sent = yh_send_workflow_email(
            (string)$hospital['email'],
            'New secure care referral from Yawar Hamdard',
            "A new referral is waiting in your Yawar hospital dashboard.\nReference: {$id}\n\nSign in to review the patient details and respond. Patient information is not included in this email."
        );
        if (!$sent) error_log('Yawar referral notification could not be sent for ' . $id);
    }
    return yh_ok([
        'request' => yh_request_json($db, $newRow),
        'hospitalName' => (string)$hospital['name'],
        'whatsappPhone' => (string)$newRow['hospital_whatsapp_phone'],
        'whatsappManualSendRequired' => true,
    ]);
}

function yh_review_request(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    if (($role = yh_roles($actor, ['admin', 'call_center'])) !== true) return $role;
    $db = yh_workflow_db();
    $id = sanitize_text_field((string)$request['request_id']);
    if (!yh_request_access($db, $actor, $id)) return yh_error(404, 'Care request not found.');
    $body = yh_body($request);
    $notes = substr(trim((string)($body['notes'] ?? 'Reviewed by the call center.')), 0, 5000);
    $update = $db->prepare('UPDATE care_requests SET status = \'UNDER_REVIEW\', coordinator_notes = ? WHERE id = ?');
    $update->execute([$notes, $id]);
    return yh_ok(['request' => yh_request_json($db, yh_get_request($db, $id))]);
}

function yh_doctor_update_request(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    if (($role = yh_roles($actor, ['doctor'])) !== true) return $role;
    $db = yh_workflow_db();
    $id = sanitize_text_field((string)$request['request_id']);
    if (!yh_request_access($db, $actor, $id)) return yh_error(404, 'Assigned care request not found.');
    $body = yh_body($request);
    $status = (string)($body['status'] ?? '');
    if (!in_array($status, ['CONFIRMED', 'IN_CONSULTATION', 'COMPLETED', 'CANCELLED'], true)) {
        return yh_error(422, 'Choose a valid care status.');
    }
    $notes = substr(trim((string)($body['notes'] ?? '')), 0, 5000);
    $update = $db->prepare('UPDATE care_requests SET status = ?, coordinator_notes = ? WHERE id = ?');
    $update->execute([$status, $notes, $id]);
    return yh_ok(['request' => yh_request_json($db, yh_get_request($db, $id))]);
}

function yh_assign_doctor(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    if (($role = yh_roles($actor, ['admin', 'call_center', 'hospital'])) !== true) return $role;
    $db = yh_workflow_db();
    $requestId = sanitize_text_field((string)$request['request_id']);
    if (!yh_request_access($db, $actor, $requestId)) return yh_error(403, 'You cannot assign this care request.');
    $doctorId = (int)(yh_body($request)['doctorId'] ?? 0);
    if ($doctorId < 1) return yh_error(422, 'Choose a verified doctor.');
    $check = $db->prepare(
        'SELECT d.id FROM doctor_profiles d JOIN care_requests r ON r.id = ?
         WHERE d.id = ? AND d.verified_at IS NOT NULL AND d.is_active = 1
           AND (r.assigned_hospital_id IS NULL OR d.hospital_id = r.assigned_hospital_id)
           AND (? <> \'hospital\' OR d.hospital_id = ?) LIMIT 1'
    );
    $check->execute([$requestId, $doctorId, (string)$actor['role'], (int)($actor['hospital_id'] ?? 0)]);
    if ($check->fetchColumn() === false) return yh_error(422, 'The doctor must be verified and associated with the receiving hospital.');
    $db->prepare('UPDATE care_requests SET assigned_doctor_id = ? WHERE id = ?')->execute([$doctorId, $requestId]);
    return yh_ok(['request' => yh_request_json($db, yh_get_request($db, $requestId))]);
}

function yh_send_message(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    $body = yh_body($request);
    $id = sanitize_text_field((string)($body['requestId'] ?? ''));
    $content = trim((string)($body['content'] ?? ''));
    if ($id === '' || $content === '' || strlen($content) > 5000) return yh_error(422, 'Enter a message to send.');
    $db = yh_workflow_db();
    if (!yh_request_access($db, $actor, $id)) return yh_error(403, 'You cannot message this care request.');
    $insert = $db->prepare('INSERT INTO care_messages (request_id, sender_user_id, body) VALUES (?, ?, ?)');
    $insert->execute([$id, (int)$actor['id'], $content]);
    $messageId = (int)$db->lastInsertId();
    return yh_ok(['id' => 'remote_' . $messageId, 'requestId' => $id, 'senderRole' => strtoupper((string)$actor['role']), 'senderName' => (string)$actor['full_name'], 'content' => $content, 'timestamp' => (int)(microtime(true) * 1000), 'conversationId' => 'case_' . $id, 'messageType' => 'TEXT', 'deliveryStatus' => 'SENT'], 201);
}

function yh_direct_conversation_row(PDO $db, int $conversationId): ?array
{
    $query = $db->prepare(
        'SELECT c.id, c.support_key, c.support_user_id, c.participant_user_id, c.updated_at,
                sp.full_name AS support_name, pp.full_name AS participant_name, pp.phone AS participant_phone,
                (SELECT m.body FROM care_direct_messages m WHERE m.conversation_id = c.id ORDER BY m.id DESC LIMIT 1) AS last_message
         FROM care_direct_conversations c
         JOIN app_users su ON su.id = c.support_user_id
         LEFT JOIN app_profiles sp ON sp.user_id = su.id
         JOIN app_users pu ON pu.id = c.participant_user_id
         LEFT JOIN app_profiles pp ON pp.user_id = pu.id
         WHERE c.id = ? LIMIT 1'
    );
    $query->execute([$conversationId]);
    $row = $query->fetch();
    return $row === false ? null : $row;
}

function yh_direct_conversation_json(array $row, array $actor): array
{
    $profiles = yh_direct_support_profiles();
    $supportKey = (string)$row['support_key'];
    $supportName = $supportKey === 'YHCS1' ? 'YHCS 1' : 'YHCS 2';
    $isSupport = (int)$row['support_user_id'] === (int)$actor['id'];
    return [
        'id' => (string)$row['id'],
        'supportKey' => $supportKey,
        'supportName' => $supportName,
        'participantName' => (string)($row['participant_name'] ?? 'App user'),
        'participantPhone' => (string)($row['participant_phone'] ?? ''),
        'lastMessage' => (string)($row['last_message'] ?? ''),
        'updatedAtTimestamp' => (int)(strtotime((string)$row['updated_at'] . ' UTC') * 1000),
        'isSupportAccount' => $isSupport,
    ];
}

function yh_direct_messages_json(PDO $db, int $conversationId): array
{
    $query = $db->prepare(
        'SELECT m.id, m.body, m.created_at, u.role, p.full_name
         FROM care_direct_messages m JOIN app_users u ON u.id = m.sender_user_id
         LEFT JOIN app_profiles p ON p.user_id = u.id
         WHERE m.conversation_id = ? ORDER BY m.id ASC LIMIT 500'
    );
    $query->execute([$conversationId]);
    return array_map(static fn(array $row): array => [
        'id' => 'direct_' . (string)$row['id'],
        'senderRole' => strtoupper((string)$row['role']),
        'senderName' => (string)($row['full_name'] ?? 'YHCS'),
        'content' => (string)$row['body'],
        'timestamp' => (int)(strtotime((string)$row['created_at'] . ' UTC') * 1000),
    ], $query->fetchAll());
}

function yh_direct_conversation_access(PDO $db, array $actor, int $conversationId): ?array
{
    $query = $db->prepare('SELECT * FROM care_direct_conversations WHERE id = ? LIMIT 1');
    $query->execute([$conversationId]);
    $row = $query->fetch();
    if ($row === false || !in_array((int)$actor['id'], [(int)$row['support_user_id'], (int)$row['participant_user_id']], true)) return null;
    return $row;
}

function yh_direct_list(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    try {
        $db = yh_workflow_db();
        yh_ensure_direct_message_tables($db);
        $query = $db->prepare(
            'SELECT c.id FROM care_direct_conversations c
             WHERE c.support_user_id = ? OR c.participant_user_id = ?
             ORDER BY c.updated_at DESC LIMIT 300'
        );
        $query->execute([(int)$actor['id'], (int)$actor['id']]);
        $conversations = [];
        foreach ($query->fetchAll() as $item) {
            $row = yh_direct_conversation_row($db, (int)$item['id']);
            if ($row !== null) $conversations[] = yh_direct_conversation_json($row, $actor);
        }
        return yh_ok(['directConversations' => $conversations]);
    } catch (Throwable $error) {
        error_log('Yawar direct-message inbox failed: ' . $error->getMessage());
        return yh_error(503, 'YHCS messages are temporarily unavailable.');
    }
}

function yh_direct_start(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    $supportKey = strtoupper(sanitize_text_field((string)(yh_body($request)['supportKey'] ?? '')));
    $profiles = yh_direct_support_profiles();
    if (!isset($profiles[$supportKey])) return yh_error(422, 'Choose YHCS 1 or YHCS 2.');
    try {
        $db = yh_workflow_db();
        yh_ensure_direct_message_tables($db);
        $profile = $profiles[$supportKey];
        $supportQuery = $db->prepare(
            'SELECT u.id, u.role, u.is_active, u.email_verified_at
             FROM app_users u WHERE LOWER(u.email) = ? LIMIT 1'
        );
        $supportQuery->execute([$profile['email']]);
        $support = $supportQuery->fetch();
        if ($support === false || !(int)$support['is_active'] || $support['email_verified_at'] === null || $support['role'] !== $profile['role']) {
            return yh_error(503, $supportKey . ' is not registered and ready for messages yet.');
        }
        if ((int)$support['id'] === (int)$actor['id']) return yh_error(422, 'Open your YHCS message inbox to answer conversations.');
        $insert = $db->prepare(
            'INSERT INTO care_direct_conversations (support_key, support_user_id, participant_user_id)
             VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE id = LAST_INSERT_ID(id), support_key = VALUES(support_key)'
        );
        $insert->execute([$supportKey, (int)$support['id'], (int)$actor['id']]);
        $conversationId = (int)$db->lastInsertId();
        $row = yh_direct_conversation_row($db, $conversationId);
        if ($row === null) return yh_error(503, 'The YHCS conversation could not be opened.');
        return yh_ok([
            'directConversation' => yh_direct_conversation_json($row, $actor),
            'directMessages' => yh_direct_messages_json($db, $conversationId),
        ], 201);
    } catch (Throwable $error) {
        error_log('Yawar direct-message open failed: ' . $error->getMessage());
        return yh_error(503, 'The YHCS conversation is temporarily unavailable.');
    }
}

function yh_direct_get(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    $conversationId = (int)$request['conversation_id'];
    try {
        $db = yh_workflow_db();
        yh_ensure_direct_message_tables($db);
        if (yh_direct_conversation_access($db, $actor, $conversationId) === null) return yh_error(404, 'Conversation not found.');
        $row = yh_direct_conversation_row($db, $conversationId);
        if ($row === null) return yh_error(404, 'Conversation not found.');
        return yh_ok([
            'directConversation' => yh_direct_conversation_json($row, $actor),
            'directMessages' => yh_direct_messages_json($db, $conversationId),
        ]);
    } catch (Throwable $error) {
        error_log('Yawar direct-message read failed: ' . $error->getMessage());
        return yh_error(503, 'This conversation is temporarily unavailable.');
    }
}

function yh_direct_send(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    $conversationId = (int)$request['conversation_id'];
    $content = trim((string)(yh_body($request)['content'] ?? ''));
    if ($content === '' || strlen($content) > 5000) return yh_error(422, 'Enter a message up to 5,000 characters.');
    try {
        $db = yh_workflow_db();
        yh_ensure_direct_message_tables($db);
        if (yh_direct_conversation_access($db, $actor, $conversationId) === null) return yh_error(404, 'Conversation not found.');
        $db->beginTransaction();
        $insert = $db->prepare('INSERT INTO care_direct_messages (conversation_id, sender_user_id, body) VALUES (?, ?, ?)');
        $insert->execute([$conversationId, (int)$actor['id'], $content]);
        $db->prepare('UPDATE care_direct_conversations SET updated_at = UTC_TIMESTAMP() WHERE id = ?')->execute([$conversationId]);
        $db->commit();
        return yh_ok(['sent' => true], 201);
    } catch (Throwable $error) {
        if (isset($db) && $db instanceof PDO && $db->inTransaction()) $db->rollBack();
        error_log('Yawar direct-message send failed: ' . $error->getMessage());
        return yh_error(503, 'The message could not be sent right now.');
    }
}

function yh_upload_document(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    if (($role = yh_roles($actor, ['hospital'])) !== true) return $role;
    $id = sanitize_text_field((string)$request->get_param('requestId'));
    $db = yh_workflow_db();
    if (!yh_request_access($db, $actor, $id)) return yh_error(403, 'This referral is not assigned to your hospital.');
    $file = $_FILES['file'] ?? null;
    if (!is_array($file) || ($file['error'] ?? UPLOAD_ERR_NO_FILE) !== UPLOAD_ERR_OK) return yh_error(422, 'Choose a document to upload.');
    if ((int)$file['size'] < 1 || (int)$file['size'] > 15 * 1024 * 1024) return yh_error(413, 'Each file must be smaller than 15 MB.');
    $mime = (new finfo(FILEINFO_MIME_TYPE))->file((string)$file['tmp_name']);
    $extensions = ['application/pdf' => 'pdf', 'image/jpeg' => 'jpg', 'image/png' => 'png'];
    if (!isset($extensions[$mime])) return yh_error(415, 'Upload a PDF, JPG or PNG document.');
    $configPath = dirname(__DIR__, 3) . '/.private/auth-config.php';
    $config = require $configPath;
    $storage = rtrim((string)($config['documents_path'] ?? ''), '/\\');
    $webRoot = rtrim(ABSPATH, '/\\') . DIRECTORY_SEPARATOR;
    if ($storage === '' || str_starts_with(strtolower($storage . DIRECTORY_SEPARATOR), strtolower($webRoot))) {
        return yh_error(503, 'Private document storage is not configured.');
    }
    if (!is_dir($storage) && !mkdir($storage, 0750, true) && !is_dir($storage)) return yh_error(503, 'Private document storage is unavailable.');
    $storageName = bin2hex(random_bytes(24)) . '.' . $extensions[$mime];
    if (!move_uploaded_file((string)$file['tmp_name'], $storage . DIRECTORY_SEPARATOR . $storageName)) return yh_error(500, 'The document could not be saved.');
    $name = sanitize_file_name((string)($file['name'] ?? 'Hospital-record'));
    $insert = $db->prepare('INSERT INTO care_documents (request_id, uploader_user_id, original_name, storage_name, mime_type, size_bytes) VALUES (?, ?, ?, ?, ?, ?)');
    $insert->execute([$id, (int)$actor['id'], $name, $storageName, $mime, (int)$file['size']]);
    $documentId = (int)$db->lastInsertId();
    return yh_ok(['id' => (string)$documentId, 'name' => $name, 'mimeType' => $mime, 'sizeBytes' => (int)$file['size']], 201);
}

function yh_complete_documents(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    if (($role = yh_roles($actor, ['hospital'])) !== true) return $role;
    $id = sanitize_text_field((string)$request['request_id']);
    $db = yh_workflow_db();
    if (!yh_request_access($db, $actor, $id)) return yh_error(403, 'This referral is not assigned to your hospital.');
    $count = $db->prepare('SELECT COUNT(*) FROM care_documents WHERE request_id = ?');
    $count->execute([$id]);
    if ((int)$count->fetchColumn() < 1) return yh_error(422, 'Upload at least one document before marking this task complete.');
    $db->prepare('UPDATE care_requests SET status = \'COMPLETED\' WHERE id = ?')->execute([$id]);
    return yh_ok(['request' => yh_request_json($db, yh_get_request($db, $id))]);
}

function yh_get_document(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    $documentId = (int)$request['document_id'];
    $db = yh_workflow_db();
    $query = $db->prepare('SELECT * FROM care_documents WHERE id = ? LIMIT 1');
    $query->execute([$documentId]);
    $document = $query->fetch();
    if ($document === false || !yh_request_access($db, $actor, (string)$document['request_id'])) return yh_error(404, 'Document not found.');
    $configPath = dirname(__DIR__, 3) . '/.private/auth-config.php';
    $config = require $configPath;
    $path = rtrim((string)($config['documents_path'] ?? ''), '/\\') . DIRECTORY_SEPARATOR . $document['storage_name'];
    if (!is_file($path)) return yh_error(404, 'Document file is unavailable.');
    $response = new WP_REST_Response(file_get_contents($path), 200);
    $response->header('Content-Type', (string)$document['mime_type']);
    $response->header('Content-Length', (string)filesize($path));
    $response->header('Content-Disposition', 'attachment; filename="' . rawurlencode((string)$document['original_name']) . '"');
    $response->header('Cache-Control', 'no-store, private');
    return $response;
}

function yh_save_payment(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    if (($role = yh_roles($actor, ['admin', 'call_center'])) !== true) return $role;
    $body = yh_body($request);
    $hospitalId = (int)($body['hospitalId'] ?? 0);
    $month = (string)($body['month'] ?? '');
    $due = (string)($body['dueDate'] ?? '');
    $amount = $body['amountAf'] ?? null;
    if ($hospitalId < 1 || !preg_match('/^\d{4}-\d{2}(-01)?$/', $month) || !preg_match('/^\d{4}-\d{2}-\d{2}$/', $due) || !is_numeric($amount) || (float)$amount < 0) {
        return yh_error(422, 'Enter a valid hospital, month, due date and amount.');
    }
    $month = substr($month, 0, 7) . '-01';
    $status = ($body['status'] ?? '') === 'PAID' ? 'PAID' : 'SCHEDULED';
    $db = yh_workflow_db();
    $hospital = $db->prepare('SELECT id FROM care_hospitals WHERE id = ? AND active = 1');
    $hospital->execute([$hospitalId]);
    if ($hospital->fetchColumn() === false) return yh_error(404, 'Hospital not found.');
    $save = $db->prepare(
        'INSERT INTO monthly_hospital_payments (hospital_id, payment_month, amount_af, due_date, paid_at, status, notes, created_by)
         VALUES (?, ?, ?, ?, IF(? = \'PAID\', UTC_TIMESTAMP(), NULL), ?, ?, ?)
         ON DUPLICATE KEY UPDATE amount_af = VALUES(amount_af), due_date = VALUES(due_date),
         paid_at = IF(VALUES(status) = \'PAID\', COALESCE(paid_at, UTC_TIMESTAMP()), NULL),
         status = VALUES(status), notes = VALUES(notes)'
    );
    $save->execute([$hospitalId, $month, (float)$amount, $due, $status, $status, substr((string)($body['notes'] ?? ''), 0, 1000), (int)$actor['id']]);
    return yh_ok(['saved' => true], 201);
}

function yh_create_hospital(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    if (($role = yh_roles($actor, ['admin'])) !== true) return $role;
    $body = yh_body($request);
    $name = trim((string)($body['name'] ?? ''));
    if ($name === '' || strlen($name) > 180) return yh_error(422, 'Enter the hospital name.');
    $db = yh_workflow_db();
    $insert = $db->prepare('INSERT INTO care_hospitals (name, email, phone, whatsapp_phone, province, city, address, logo_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?)');
    $insert->execute([
        $name, sanitize_email((string)($body['email'] ?? '')), substr((string)($body['phone'] ?? ''), 0, 32),
        substr((string)($body['whatsappPhone'] ?? ''), 0, 32), substr((string)($body['province'] ?? ''), 0, 80),
        substr((string)($body['city'] ?? ''), 0, 80), substr((string)($body['address'] ?? ''), 0, 255),
        esc_url_raw((string)($body['logoUrl'] ?? '')),
    ]);
    return yh_ok(['id' => (string)$db->lastInsertId(), 'name' => $name], 201);
}

function yh_deactivate_hospital(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    if (($role = yh_roles($actor, ['admin'])) !== true) return $role;
    $id = (int)$request['hospital_id'];
    $db = yh_workflow_db();
    $db->prepare('UPDATE care_hospitals SET active = 0 WHERE id = ?')->execute([$id]);
    return yh_ok(['deactivated' => true]);
}

function yh_create_hospital_user(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    if (($role = yh_roles($actor, ['admin'])) !== true) return $role;
    $body = yh_body($request);
    $email = strtolower(trim((string)($body['email'] ?? '')));
    $name = trim((string)($body['fullName'] ?? ''));
    $phone = trim((string)($body['phone'] ?? ''));
    $hospitalId = (int)($body['hospitalId'] ?? 0);
    if (!is_email($email) || $name === '' || $hospitalId < 1) return yh_error(422, 'Enter a valid name, email and hospital.');
    $db = yh_workflow_db();
    $check = $db->prepare('SELECT id FROM care_hospitals WHERE id = ? AND active = 1');
    $check->execute([$hospitalId]);
    if ($check->fetchColumn() === false) return yh_error(404, 'Hospital not found.');
    $db->beginTransaction();
    try {
        $insert = $db->prepare('INSERT INTO app_users (email, password_hash, role, hospital_id, email_verified_at) VALUES (?, ?, \'hospital\', ?, UTC_TIMESTAMP())');
        $insert->execute([$email, password_hash(bin2hex(random_bytes(40)), PASSWORD_DEFAULT), $hospitalId]);
        $userId = (int)$db->lastInsertId();
        $profile = $db->prepare('INSERT INTO app_profiles (user_id, full_name, phone) VALUES (?, ?, ?)');
        $profile->execute([$userId, $name, $phone]);
        $db->commit();
        return yh_ok(['created' => true, 'email' => $email, 'passwordSetup' => 'Use Forgot password to create a private password.'], 201);
    } catch (Throwable $error) {
        if ($db->inTransaction()) $db->rollBack();
        return yh_error(409, 'That account already exists or could not be created.');
    }
}

function yh_create_doctor(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    if (($role = yh_roles($actor, ['admin'])) !== true) return $role;
    $body = yh_body($request);
    $email = strtolower(trim((string)($body['email'] ?? '')));
    $name = trim((string)($body['fullName'] ?? ''));
    $phone = trim((string)($body['phone'] ?? ''));
    $specialty = trim((string)($body['specialty'] ?? ''));
    $license = trim((string)($body['licenseNumber'] ?? ''));
    $hospitalId = isset($body['hospitalId']) ? (int)$body['hospitalId'] : 0;
    $photoUrl = esc_url_raw((string)($body['photoUrl'] ?? ''));
    $staffAddresses = [
        'm.ibrahim@yawarconsulting.com', 'dr_eimalmalik@yawarconsulting.com',
        'yh24@yawarconsulting.com', 'ibrahimkakar182@gmail.com', 'info@yawarconsulting.com',
    ];
    if (!is_email($email) || $name === '' || $specialty === '' || $license === '' || in_array($email, $staffAddresses, true)) {
        return yh_error(422, 'Enter a valid doctor name, email, specialty and license number.');
    }
    $db = yh_workflow_db();
    if ($hospitalId > 0) {
        $check = $db->prepare('SELECT id FROM care_hospitals WHERE id = ? AND active = 1');
        $check->execute([$hospitalId]);
        if ($check->fetchColumn() === false) return yh_error(404, 'Choose an active hospital.');
    }
    $db->beginTransaction();
    try {
        $user = $db->prepare('INSERT INTO app_users (email, password_hash, role, is_active, email_verified_at) VALUES (?, ?, \'doctor\', 1, UTC_TIMESTAMP())');
        $user->execute([$email, password_hash(bin2hex(random_bytes(40)), PASSWORD_DEFAULT)]);
        $userId = (int)$db->lastInsertId();
        $profile = $db->prepare('INSERT INTO app_profiles (user_id, full_name, phone) VALUES (?, ?, ?)');
        $profile->execute([$userId, $name, substr($phone, 0, 32)]);
        $doctor = $db->prepare('INSERT INTO doctor_profiles (user_id, hospital_id, specialty, license_number, photo_url) VALUES (?, ?, ?, ?, ?)');
        $doctor->execute([$userId, $hospitalId > 0 ? $hospitalId : null, substr($specialty, 0, 120), substr($license, 0, 80), substr($photoUrl, 0, 512)]);
        $db->commit();
        return yh_ok(['created' => true, 'doctorId' => (string)$db->lastInsertId(), 'email' => $email, 'verified' => false, 'passwordSetup' => 'The doctor can use Forgot password to create a private password.'], 201);
    } catch (Throwable $error) {
        if ($db->inTransaction()) $db->rollBack();
        return yh_error(409, 'That doctor account already exists or could not be created.');
    }
}

function yh_set_doctor_verification(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    if (($role = yh_roles($actor, ['admin'])) !== true) return $role;
    $body = yh_body($request);
    if (!array_key_exists('verified', $body)) return yh_error(422, 'Choose whether to verify this doctor.');
    $db = yh_workflow_db();
    $exists = $db->prepare('SELECT 1 FROM doctor_profiles WHERE id = ? AND is_active = 1');
    $exists->execute([(int)$request['doctor_id']]);
    if ($exists->fetchColumn() === false) return yh_error(404, 'Doctor not found.');
    $update = $db->prepare('UPDATE doctor_profiles SET verified_at = IF(? = 1, UTC_TIMESTAMP(), NULL) WHERE id = ? AND is_active = 1');
    $update->execute([(bool)$body['verified'] ? 1 : 0, (int)$request['doctor_id']]);
    return yh_ok(['verified' => (bool)$body['verified']]);
}

function yh_deactivate_doctor(WP_REST_Request $request): WP_REST_Response|WP_Error
{
    $actor = yh_actor($request);
    if (is_wp_error($actor)) return $actor;
    if (($role = yh_roles($actor, ['admin'])) !== true) return $role;
    $db = yh_workflow_db();
    $lookup = $db->prepare('SELECT user_id FROM doctor_profiles WHERE id = ? AND is_active = 1');
    $lookup->execute([(int)$request['doctor_id']]);
    $userId = $lookup->fetchColumn();
    if ($userId === false) return yh_error(404, 'Doctor not found.');
    $db->beginTransaction();
    try {
        $db->prepare('UPDATE doctor_profiles SET is_active = 0, verified_at = NULL WHERE id = ?')->execute([(int)$request['doctor_id']]);
        $db->prepare('UPDATE app_users SET is_active = 0 WHERE id = ?')->execute([(int)$userId]);
        $db->prepare('UPDATE app_sessions SET revoked_at = UTC_TIMESTAMP() WHERE user_id = ? AND revoked_at IS NULL')->execute([(int)$userId]);
        $db->commit();
    } catch (Throwable $error) {
        if ($db->inTransaction()) $db->rollBack();
        return yh_error(503, 'The doctor account could not be deactivated.');
    }
    return yh_ok(['deactivated' => true]);
}

function yh_daily_document_reminders(): void
{
    try {
        $db = yh_workflow_db();
        $rows = $db->query(
            'SELECT r.id, r.patient_name, r.reason_for_care, h.name AS hospital_name, h.email
             FROM care_requests r JOIN care_hospitals h ON h.id = r.assigned_hospital_id
             WHERE r.status = \'AWAITING_PROVIDER\' AND h.email <> \'\'
               AND NOT EXISTS (SELECT 1 FROM care_documents d WHERE d.request_id = r.id)
               AND (r.last_doc_reminder_on IS NULL OR r.last_doc_reminder_on < UTC_DATE())'
        )->fetchAll();
        foreach ($rows as $row) {
            $subject = 'Daily records reminder • Yawar care request ' . $row['id'];
            $message = "A patient record upload is still pending for referral {$row['id']}.\n\nPlease open the Yawar Hamdard hospital dashboard and upload the requested documents. This reminder will stop when records are uploaded and marked complete.";
            if (yh_send_workflow_email((string)$row['email'], $subject, $message)) {
                $db->prepare('UPDATE care_requests SET last_doc_reminder_on = UTC_DATE() WHERE id = ?')->execute([$row['id']]);
            }
        }
    } catch (Throwable $error) {
        error_log('Yawar document reminder job failed: ' . $error->getMessage());
    }
}

register_activation_hook(__FILE__, static function (): void {
    if (!wp_next_scheduled('yh_daily_document_reminders')) wp_schedule_event(time() + 300, 'daily', 'yh_daily_document_reminders');
});
register_deactivation_hook(__FILE__, static function (): void {
    wp_clear_scheduled_hook('yh_daily_document_reminders');
});
add_action('yh_daily_document_reminders', 'yh_daily_document_reminders');

add_action('rest_api_init', static function (): void {
    register_rest_route('yh/v1', '/dashboard', ['methods' => 'GET', 'callback' => 'yh_dashboard', 'permission_callback' => '__return_true']);
    register_rest_route('yh/v1', '/requests', ['methods' => 'POST', 'callback' => 'yh_create_request', 'permission_callback' => '__return_true']);
    register_rest_route('yh/v1', '/requests/(?P<request_id>[A-Za-z0-9-]+)/review', ['methods' => 'POST', 'callback' => 'yh_review_request', 'permission_callback' => '__return_true']);
    register_rest_route('yh/v1', '/requests/(?P<request_id>[A-Za-z0-9-]+)/doctor-status', ['methods' => 'POST', 'callback' => 'yh_doctor_update_request', 'permission_callback' => '__return_true']);
    register_rest_route('yh/v1', '/requests/(?P<request_id>[A-Za-z0-9-]+)/assign-doctor', ['methods' => 'POST', 'callback' => 'yh_assign_doctor', 'permission_callback' => '__return_true']);
    register_rest_route('yh/v1', '/requests/(?P<request_id>[A-Za-z0-9-]+)/route', ['methods' => 'POST', 'callback' => 'yh_route_request', 'permission_callback' => '__return_true']);
    register_rest_route('yh/v1', '/requests/(?P<request_id>[A-Za-z0-9-]+)/documents-complete', ['methods' => 'POST', 'callback' => 'yh_complete_documents', 'permission_callback' => '__return_true']);
    register_rest_route('yh/v1', '/messages', ['methods' => 'POST', 'callback' => 'yh_send_message', 'permission_callback' => '__return_true']);
    register_rest_route('yh/v1', '/direct-conversations', [
        ['methods' => 'GET', 'callback' => 'yh_direct_list', 'permission_callback' => '__return_true'],
        ['methods' => 'POST', 'callback' => 'yh_direct_start', 'permission_callback' => '__return_true'],
    ]);
    register_rest_route('yh/v1', '/direct-conversations/(?P<conversation_id>\d+)', ['methods' => 'GET', 'callback' => 'yh_direct_get', 'permission_callback' => '__return_true']);
    register_rest_route('yh/v1', '/direct-conversations/(?P<conversation_id>\d+)/messages', ['methods' => 'POST', 'callback' => 'yh_direct_send', 'permission_callback' => '__return_true']);
    register_rest_route('yh/v1', '/documents', ['methods' => 'POST', 'callback' => 'yh_upload_document', 'permission_callback' => '__return_true']);
    register_rest_route('yh/v1', '/documents/(?P<document_id>\d+)', ['methods' => 'GET', 'callback' => 'yh_get_document', 'permission_callback' => '__return_true']);
    register_rest_route('yh/v1', '/payments', ['methods' => 'POST', 'callback' => 'yh_save_payment', 'permission_callback' => '__return_true']);
    register_rest_route('yh/v1', '/hospitals', ['methods' => 'POST', 'callback' => 'yh_create_hospital', 'permission_callback' => '__return_true']);
    register_rest_route('yh/v1', '/hospitals/(?P<hospital_id>\d+)', ['methods' => 'DELETE', 'callback' => 'yh_deactivate_hospital', 'permission_callback' => '__return_true']);
    register_rest_route('yh/v1', '/hospital-users', ['methods' => 'POST', 'callback' => 'yh_create_hospital_user', 'permission_callback' => '__return_true']);
    register_rest_route('yh/v1', '/doctors', ['methods' => 'POST', 'callback' => 'yh_create_doctor', 'permission_callback' => '__return_true']);
    register_rest_route('yh/v1', '/doctors/(?P<doctor_id>\d+)/verification', ['methods' => 'PUT', 'callback' => 'yh_set_doctor_verification', 'permission_callback' => '__return_true']);
    register_rest_route('yh/v1', '/doctors/(?P<doctor_id>\d+)', ['methods' => 'DELETE', 'callback' => 'yh_deactivate_doctor', 'permission_callback' => '__return_true']);
});
