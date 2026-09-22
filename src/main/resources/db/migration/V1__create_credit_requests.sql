CREATE TABLE credit_requests (
                                 id BIGSERIAL PRIMARY KEY,
                                 telegram_user_id BIGINT NOT NULL,
                                 amount NUMERIC(19, 2) NOT NULL,
                                 term_months INTEGER NOT NULL,
                                 annual_rate NUMERIC(5, 2) NOT NULL,
                                 payment_type VARCHAR(32) NOT NULL,
                                 created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_credit_requests_user_id
    ON credit_requests (telegram_user_id);

CREATE INDEX idx_credit_requests_payment_type
    ON credit_requests (payment_type);

CREATE INDEX idx_credit_requests_amount
    ON credit_requests (amount);

CREATE INDEX idx_credit_requests_created_at
    ON credit_requests (created_at);