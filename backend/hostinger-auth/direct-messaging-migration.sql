-- Create private YHCS contact conversations and their in-app messages.
CREATE TABLE IF NOT EXISTS care_direct_conversations (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS care_direct_messages (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    conversation_id BIGINT UNSIGNED NOT NULL,
    sender_user_id BIGINT UNSIGNED NOT NULL,
    body TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_direct_message_conversation (conversation_id, created_at),
    CONSTRAINT fk_direct_message_conversation FOREIGN KEY (conversation_id) REFERENCES care_direct_conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_direct_message_sender FOREIGN KEY (sender_user_id) REFERENCES app_users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
