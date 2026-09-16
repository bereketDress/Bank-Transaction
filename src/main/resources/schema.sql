CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    user_name VARCHAR(100) NOT NULL,
    user_email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(60) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accounts (
    account_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    pin_hash VARCHAR(60) NOT NULL,
    account_type VARCHAR(20) NOT NULL CHECK (account_type IN ('CHECKING', 'SAVING')),
    balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00 CHECK (balance >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id BIGSERIAL PRIMARY KEY,
    source_account_id BIGINT REFERENCES accounts(account_id),
    destination_account_id BIGINT REFERENCES accounts(account_id),
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    executed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER')),
    transaction_status VARCHAR(20) NOT NULL CHECK (transaction_status IN ('PENDING', 'COMPLETED', 'FAILED')),
    CHECK (source_account_id IS NOT NULL OR destination_account_id IS NOT NULL)
);

CREATE INDEX IF NOT EXISTS idx_transactions_source ON transactions(source_account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_destination ON transactions(destination_account_id);

CREATE TABLE IF NOT EXISTS system_logs (
    log_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(user_id),
    log_level VARCHAR(10) NOT NULL CHECK (log_level IN ('INFO', 'ERROR')),
    message VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_accounts_user ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_system_logs_user ON system_logs(user_id);
