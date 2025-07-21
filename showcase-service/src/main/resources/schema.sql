CREATE EXTENSION IF NOT EXISTS pgcrypto;

DROP TABLE IF EXISTS products, users, carts, cart_items, orders, order_items, roles, user_roles CASCADE;

-- Таблица товаров
CREATE TABLE IF NOT EXISTS products (
    product_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    image_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_products_product_uuid ON products(product_uuid);
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_products_price ON products(price);

-- Таблица пользователей
CREATE TABLE IF NOT EXISTS users (
    user_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    account_non_locked BOOLEAN DEFAULT TRUE,
    account_non_expired BOOLEAN DEFAULT TRUE,
    credentials_non_expired BOOLEAN DEFAULT TRUE,
    enabled BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_user_user_uuid ON users(user_uuid);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Таблица ролей (справочник)
CREATE TABLE IF NOT EXISTS roles (
    role_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    description TEXT,
    name VARCHAR(20) NOT NULL UNIQUE
);

-- Таблица связи пользователей и ролей
CREATE TABLE IF NOT EXISTS user_roles (
    user_uuid UUID NOT NULL,
    role_uuid UUID NOT NULL,
    PRIMARY KEY (user_uuid, role_uuid),
    FOREIGN KEY (user_uuid) REFERENCES users(user_uuid) ON DELETE CASCADE,
    FOREIGN KEY (role_uuid) REFERENCES roles(role_uuid) ON DELETE CASCADE
);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_uuid);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_uuid);

-- Таблица корзин
CREATE TABLE IF NOT EXISTS carts (
    cart_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_uuid UUID NOT NULL,
    total_price DECIMAL(10, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_uuid) REFERENCES users(user_uuid) ON DELETE CASCADE
);

-- Таблица элементов корзины
CREATE TABLE IF NOT EXISTS cart_items (
    cart_item_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_uuid UUID NOT NULL,
    product_uuid UUID NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (cart_uuid) REFERENCES carts(cart_uuid) ON DELETE CASCADE,
    FOREIGN KEY (product_uuid) REFERENCES products(product_uuid) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_cart_items_product ON cart_items(product_uuid);

-- Таблица заказов
CREATE TABLE IF NOT EXISTS orders (
    order_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_uuid UUID NOT NULL,
    cart_uuid UUID NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    total_amount DECIMAL(10, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_uuid) REFERENCES users(user_uuid) ON DELETE CASCADE,
    FOREIGN KEY (cart_uuid) REFERENCES carts(cart_uuid) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_orders_user ON orders(user_uuid);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);

-- Таблица товаров заказа
CREATE TABLE IF NOT EXISTS order_items (
   order_item_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   order_uuid UUID NOT NULL,
   product_uuid UUID NOT NULL,
   quantity INT NOT NULL,
   price_at_order numeric(10,2),
   FOREIGN KEY (order_uuid) REFERENCES orders(order_uuid) ON DELETE CASCADE,
   FOREIGN KEY (product_uuid) REFERENCES products(product_uuid) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_uuid);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items(product_uuid);