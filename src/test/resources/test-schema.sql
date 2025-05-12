CREATE EXTENSION IF NOT EXISTS pgcrypto;

DROP TABLE IF EXISTS products, orders, order_items CASCADE;

CREATE TABLE IF NOT EXISTS products (
    product_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    image_url TEXT
);

CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_products_price ON products(price);

CREATE TABLE IF NOT EXISTS orders (
    order_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP(0) DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);

CREATE TABLE IF NOT EXISTS order_items (
   order_item_uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
   order_uuid BIGINT NOT NULL,
   product_uuid BIGINT NOT NULL,
   quantity INT NOT NULL,
   FOREIGN KEY (order_uuid) REFERENCES orders(order_uuid) ON DELETE CASCADE,
   FOREIGN KEY (product_uuid) REFERENCES products(product_uuid)
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_uuid);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items(product_uuid);