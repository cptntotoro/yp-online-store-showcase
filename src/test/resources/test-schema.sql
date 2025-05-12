CREATE EXTENSION IF NOT EXISTS pgcrypto;

DROP TABLE IF EXISTS products, users, carts, cart_items, orders, order_items CASCADE;

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
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

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
    FOREIGN KEY (product_uuid) REFERENCES products(product_uuid)
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
    FOREIGN KEY (user_uuid) REFERENCES users(user_uuid),
    FOREIGN KEY (cart_uuid) REFERENCES carts(cart_uuid)
);

CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_orders_user ON orders(user_uuid);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);

CREATE TABLE IF NOT EXISTS order_items (
   order_item_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   order_uuid UUID NOT NULL,
   product_uuid UUID NOT NULL,
   quantity INT NOT NULL,
   price_at_order numeric(10,2),
   FOREIGN KEY (order_uuid) REFERENCES orders(order_uuid) ON DELETE CASCADE,
   FOREIGN KEY (product_uuid) REFERENCES products(product_uuid)
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_uuid);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items(product_uuid);