CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       first_name VARCHAR(100),
                       last_name VARCHAR(100),
                       manager BOOLEAN NOT NULL DEFAULT FALSE,
                       telegram_user_id BIGINT,
                       created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email
    ON users (email);

CREATE INDEX idx_users_telegram_user_id
    ON users (telegram_user_id);