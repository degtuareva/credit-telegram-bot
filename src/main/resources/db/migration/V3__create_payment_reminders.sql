CREATE TABLE payment_reminders (
                                   id BIGSERIAL PRIMARY KEY,
                                   telegram_user_id BIGINT NOT NULL,
                                   credit_request_id BIGINT,
                                   remind_at TIMESTAMP NOT NULL,
                                   status VARCHAR(30) NOT NULL,
                                   created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payment_reminders_telegram_user_id
    ON payment_reminders (telegram_user_id);

CREATE INDEX idx_payment_reminders_credit_request_id
    ON payment_reminders (credit_request_id);

CREATE INDEX idx_payment_reminders_remind_at
    ON payment_reminders (remind_at);