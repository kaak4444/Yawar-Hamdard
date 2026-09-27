-- One-time upgrade for the existing dedicated Yawar app database.
-- Back up the app database before applying this migration.
SET time_zone = '+00:00';

CREATE TABLE IF NOT EXISTS care_hospitals (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    name VARCHAR(180) NOT NULL,
    email VARCHAR(254) NOT NULL DEFAULT '',
    phone VARCHAR(32) NOT NULL DEFAULT '',
    whatsapp_phone VARCHAR(32) NOT NULL DEFAULT '',
    province VARCHAR(80) NOT NULL DEFAULT '',
    city VARCHAR(80) NOT NULL DEFAULT '',
    address VARCHAR(255) NOT NULL DEFAULT '',
    logo_url VARCHAR(512) NOT NULL DEFAULT '',
    active TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_care_hospitals_active_name (active, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE app_users
    ADD COLUMN is_active TINYINT(1) NOT NULL DEFAULT 1 AFTER role,
    ADD COLUMN hospital_id BIGINT UNSIGNED NULL AFTER role,
    ADD KEY idx_app_users_hospital (hospital_id),
    ADD CONSTRAINT fk_app_users_hospital FOREIGN KEY (hospital_id)
        REFERENCES care_hospitals(id) ON DELETE SET NULL;

CREATE TABLE IF NOT EXISTS app_profiles (
    user_id BIGINT UNSIGNED NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    phone VARCHAR(32) NOT NULL DEFAULT '',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_app_profile_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS doctor_profiles (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id BIGINT UNSIGNED NOT NULL,
    hospital_id BIGINT UNSIGNED NULL,
    specialty VARCHAR(120) NOT NULL,
    subspecialty VARCHAR(120) NOT NULL DEFAULT '',
    license_number VARCHAR(80) NOT NULL,
    photo_url VARCHAR(512) NOT NULL DEFAULT '',
    bio TEXT NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    verified_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_doctor_profile_user (user_id),
    KEY idx_doctor_profile_hospital (hospital_id),
    CONSTRAINT fk_doctor_profile_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_doctor_profile_hospital FOREIGN KEY (hospital_id) REFERENCES care_hospitals(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS care_requests (
    id VARCHAR(40) NOT NULL,
    patient_user_id BIGINT UNSIGNED NOT NULL,
    assigned_hospital_id BIGINT UNSIGNED NULL,
    assigned_doctor_id BIGINT UNSIGNED NULL,
    patient_name VARCHAR(120) NOT NULL,
    patient_phone VARCHAR(32) NOT NULL,
    specialty VARCHAR(120) NOT NULL DEFAULT '',
    reason_for_care VARCHAR(1000) NOT NULL,
    symptoms_summary TEXT NOT NULL,
    urgency VARCHAR(16) NOT NULL DEFAULT 'Routine',
    visit_type VARCHAR(24) NOT NULL DEFAULT 'IN_PERSON',
    province VARCHAR(80) NOT NULL DEFAULT '',
    appointment_date VARCHAR(40) NOT NULL DEFAULT '',
    time_slot VARCHAR(40) NOT NULL DEFAULT '',
    status VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED',
    coordinator_notes TEXT NOT NULL,
    consented_at DATETIME NOT NULL,
    last_doc_reminder_on DATE NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_care_requests_patient (patient_user_id, created_at),
    KEY idx_care_requests_hospital_status (assigned_hospital_id, status, created_at),
    CONSTRAINT fk_care_request_patient FOREIGN KEY (patient_user_id) REFERENCES app_users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_care_request_hospital FOREIGN KEY (assigned_hospital_id) REFERENCES care_hospitals(id) ON DELETE SET NULL,
    CONSTRAINT fk_care_request_doctor FOREIGN KEY (assigned_doctor_id) REFERENCES doctor_profiles(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS care_messages (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    request_id VARCHAR(40) NOT NULL,
    sender_user_id BIGINT UNSIGNED NOT NULL,
    body TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_care_messages_request_created (request_id, created_at),
    CONSTRAINT fk_care_message_request FOREIGN KEY (request_id) REFERENCES care_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_care_message_sender FOREIGN KEY (sender_user_id) REFERENCES app_users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS care_documents (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    request_id VARCHAR(40) NOT NULL,
    uploader_user_id BIGINT UNSIGNED NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    storage_name VARCHAR(80) NOT NULL,
    mime_type VARCHAR(120) NOT NULL,
    size_bytes BIGINT UNSIGNED NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_care_documents_request (request_id, created_at),
    CONSTRAINT fk_care_document_request FOREIGN KEY (request_id) REFERENCES care_requests(id) ON DELETE CASCADE,
    CONSTRAINT fk_care_document_uploader FOREIGN KEY (uploader_user_id) REFERENCES app_users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS monthly_hospital_payments (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    hospital_id BIGINT UNSIGNED NOT NULL,
    payment_month DATE NOT NULL,
    amount_af DECIMAL(14,2) NOT NULL DEFAULT 0,
    due_date DATE NOT NULL,
    paid_at DATETIME NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'SCHEDULED',
    notes VARCHAR(1000) NOT NULL DEFAULT '',
    created_by BIGINT UNSIGNED NOT NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_hospital_payment_month (hospital_id, payment_month),
    KEY idx_hospital_payment_due (status, due_date),
    CONSTRAINT fk_payment_hospital FOREIGN KEY (hospital_id) REFERENCES care_hospitals(id) ON DELETE RESTRICT,
    CONSTRAINT fk_payment_creator FOREIGN KEY (created_by) REFERENCES app_users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
