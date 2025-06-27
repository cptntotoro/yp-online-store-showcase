CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    user_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Таблица балансов пользователей
CREATE TABLE IF NOT EXISTS user_balances (
    balance_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_uuid UUID NOT NULL,
    amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00 CHECK (amount >= 0),
    last_transaction_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_uuid) REFERENCES users(user_uuid) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_user_balances_user ON user_balances(user_uuid);

-- Таблица истории транзакций
CREATE TABLE IF NOT EXISTS payment_transactions (
    transaction_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_uuid UUID NOT NULL,
    order_uuid UUID,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'REFUND')),
    transaction_status VARCHAR(20) NOT NULL CHECK (transaction_status IN ('PENDING', 'COMPLETED', 'FAILED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_uuid) REFERENCES users(user_uuid) ON DELETE CASCADE,
    FOREIGN KEY (order_uuid) REFERENCES orders(order_uuid) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_transactions_user ON payment_transactions(user_uuid);
CREATE INDEX IF NOT EXISTS idx_transactions_order ON payment_transactions(order_uuid);
CREATE INDEX IF NOT EXISTS idx_transactions_created ON payment_transactions(created_at);