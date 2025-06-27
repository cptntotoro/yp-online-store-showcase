CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE INDEX IF NOT EXISTS idx_user_user ON users(user_uuid);

-- Таблица балансов пользователей
CREATE TABLE IF NOT EXISTS user_balances (
    balance_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_uuid UUID NOT NULL,
    amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00 CHECK (amount >= 0),
    last_transaction_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_user_balances_user ON user_balances(user_uuid);

-- Таблица истории транзакций
CREATE TABLE IF NOT EXISTS payment_transactions (
    transaction_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_uuid UUID NOT NULL,
    order_uuid UUID,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    transaction_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_transactions_user ON payment_transactions(user_uuid);
CREATE INDEX IF NOT EXISTS idx_transactions_order ON payment_transactions(order_uuid);