CREATE TABLE early_repayments (
                                  id BIGSERIAL PRIMARY KEY,
                                  telegram_user_id BIGINT NOT NULL,
                                  credit_request_id BIGINT,
                                  amount NUMERIC(15, 2) NOT NULL,
                                  term_months INTEGER NOT NULL,
                                  annual_rate NUMERIC(6, 2) NOT NULL,
                                  payment_type VARCHAR(20) NOT NULL,
                                  early_payment NUMERIC(15, 2) NOT NULL,
                                  early_payment_month INTEGER NOT NULL,
                                  strategy VARCHAR(30) NOT NULL,
                                  interest_saved NUMERIC(15, 2) NOT NULL,
                                  months_saved INTEGER NOT NULL,
                                  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_early_repayments_telegram_user_id
    ON early_repayments (telegram_user_id);

CREATE INDEX idx_early_repayments_credit_request_id
    ON early_repayments (credit_request_id);