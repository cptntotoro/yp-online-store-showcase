INSERT INTO products (product_uuid, name, description, price, image_url) VALUES
    (gen_random_uuid(), 'RGB LED Лампа', 'Умная лампа с изменяемыми цветами и управлением через смартфон', 49.99, '/images/rgb-lamp.jpg'),
    (gen_random_uuid(), 'Коврик для мыши "Киберпанк"', 'Большой коврик с неоновым дизайном в стиле киберпанк', 29.50, '/images/cyberpunk-mousepad.jpg'),
    (gen_random_uuid(), 'Настенные часы "Терминатор"', 'LED часы с красными цифрами в стиле фильма "Терминатор"', 89.99, '/images/terminator-clock.jpg'),
    (gen_random_uuid(), 'Подставка для наушников "Ретро игровая консоль"', 'Стильная подставка в виде ретро-игровой приставки', 45.00, '/images/headphone-stand.jpg'),
    (gen_random_uuid(), '3D ночник "Звездные Войны"', 'Ночник с 3D эффектом в виде силуэтов персонажей Звездных Войн', 59.99, '/images/star-wars-lamp.jpg'),
    (gen_random_uuid(), 'Кресло геймерское "CyberThrone"', 'Эргономичное кресло с RGB подсветкой и поддержкой спины', 399.99, '/images/gaming-chair.jpg'),
    (gen_random_uuid(), 'Набор постеров "Классические игры"', 'Набор из 5 постеров с пиксельной графикой классических игр', 24.99, '/images/game-posters.jpg'),
    (gen_random_uuid(), 'USB Хаб "Ретро компьютер"', 'USB хаб в стиле ретро компьютера с 4 портами', 35.50, '/images/retro-usb-hub.jpg'),
    (gen_random_uuid(), 'Коврик для йоги "8-битный"', 'Коврик для йоги с пиксельным дизайном в стиле 8-битных игр', 42.00, '/images/8bit-yoga-mat.jpg'),
    (gen_random_uuid(), 'Настольная лампа "Тетрис"', 'Лампа с изменяемыми блоками как в игре Тетрис', 75.25, '/images/tetris-lamp.jpg');

INSERT INTO orders (order_uuid, created_at) VALUES
    (gen_random_uuid(), '2023-11-15 10:30:00'),
    (gen_random_uuid(), '2023-11-16 14:45:00'),
    (gen_random_uuid(), '2023-11-17 09:15:00');

-- Добавляем товары в заказы
INSERT INTO order_items (order_item_uuid, order_uuid, product_uuid, quantity) VALUES
    (gen_random_uuid(),
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 0),
    (SELECT product_uuid FROM products WHERE name = 'RGB LED Лампа' LIMIT 1),
    2),

    (gen_random_uuid(),
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 0),
    (SELECT product_uuid FROM products WHERE name = 'Коврик для мыши "Киберпанк"' LIMIT 1),
    1);

-- Заказ 2
INSERT INTO order_items (order_item_uuid, order_uuid, product_uuid, quantity) VALUES
    (gen_random_uuid(),
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 1),
    (SELECT product_uuid FROM products WHERE name = '3D ночник "Звездные Войны"' LIMIT 1),
    1),

    (gen_random_uuid(),
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 1),
    (SELECT product_uuid FROM products WHERE name = 'Набор постеров "Классические игры"' LIMIT 1),
    3),

    (gen_random_uuid(),
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 1),
    (SELECT product_uuid FROM products WHERE name = 'USB Хаб "Ретро компьютер"' LIMIT 1),
    2);

-- Заказ 3
INSERT INTO order_items (order_item_uuid, order_uuid, product_uuid, quantity) VALUES
    (gen_random_uuid(),
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 2),
    (SELECT product_uuid FROM products WHERE name = 'Кресло геймерское "CyberThrone"' LIMIT 1),
    1),

    (gen_random_uuid(),
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 2),
    (SELECT product_uuid FROM products WHERE name = 'Настольная лампа "Тетрис"' LIMIT 1),
     1);

--  ////////////////////////////////

-- Вставляем товары для гиковского интерьера
-- WITH inserted_products AS (
--   INSERT INTO products (product_uuid, name, description, price, image_url) VALUES
--   -- Товар 1
--   (gen_random_uuid(), 'RGB LED Лампа', 'Умная лампа с изменяемыми цветами и управлением через смартфон', 49.99, '/images/rgb-lamp.jpg'),
--
--   -- Товар 2
--   (gen_random_uuid(), 'Коврик для мыши "Киберпанк"', 'Большой коврик с неоновым дизайном в стиле киберпанк', 29.50, '/images/cyberpunk-mousepad.jpg'),
--
--   -- Товар 3
--   (gen_random_uuid(), 'Настенные часы "Терминатор"', 'LED часы с красными цифрами в стиле фильма "Терминатор"', 89.99, '/images/terminator-clock.jpg'),
--
--   -- Товар 4
--   (gen_random_uuid(), 'Подставка для наушников "Ретро игровая консоль"', 'Стильная подставка в виде ретро-игровой приставки', 45.00, '/images/headphone-stand.jpg'),
--
--   -- Товар 5
--   (gen_random_uuid(), '3D ночник "Звездные Войны"', 'Ночник с 3D эффектом в виде силуэтов персонажей Звездных Войн', 59.99, '/images/star-wars-lamp.jpg'),
--
--   -- Товар 6
--   (gen_random_uuid(), 'Кресло геймерское "CyberThrone"', 'Эргономичное кресло с RGB подсветкой и поддержкой спины', 399.99, '/images/gaming-chair.jpg'),
--
--   -- Товар 7
--   (gen_random_uuid(), 'Набор постеров "Классические игры"', 'Набор из 5 постеров с пиксельной графикой классических игр', 24.99, '/images/game-posters.jpg'),
--
--   -- Товар 8
--   (gen_random_uuid(), 'USB Хаб "Ретро компьютер"', 'USB хаб в стиле ретро компьютера с 4 портами', 35.50, '/images/retro-usb-hub.jpg'),
--
--   -- Товар 9
--   (gen_random_uuid(), 'Коврик для йоги "8-битный"', 'Коврик для йоги с пиксельным дизайном в стиле 8-битных игр', 42.00, '/images/8bit-yoga-mat.jpg'),
--
--   -- Товар 10
--   (gen_random_uuid(), 'Настольная лампа "Тетрис"', 'Лампа с изменяемыми блоками как в игре Тетрис', 75.25, '/images/tetris-lamp.jpg')
--   RETURNING product_uuid, name
-- ),
--
-- -- Создаем тестовые заказы
-- inserted_orders AS (
--   INSERT INTO orders (order_uuid, created_at) VALUES
--   (gen_random_uuid(), '2023-11-15 10:30:00'),
--   (gen_random_uuid(), '2023-11-16 14:45:00'),
--   (gen_random_uuid(), '2023-11-17 09:15:00')
--   RETURNING order_uuid, created_at
-- )
--
-- -- Добавляем товары в заказы
-- INSERT INTO order_items (order_item_uuid, order_uuid, product_uuid, quantity)
-- SELECT
--   gen_random_uuid(),
--   o.order_uuid,
--   p.product_uuid,
--   CASE
--     WHEN p.name = 'RGB LED Лампа' THEN 2
--     WHEN p.name = 'Коврик для мыши "Киберпанк"' THEN 1
--     WHEN p.name = '3D ночник "Звездные Войны"' THEN 1
--     WHEN p.name = 'Набор постеров "Классические игры"' THEN 3
--     WHEN p.name = 'USB Хаб "Ретро компьютер"' THEN 2
--     WHEN p.name = 'Кресло геймерское "CyberThrone"' THEN 1
--     WHEN p.name = 'Настольная лампа "Тетрис"' THEN 1
--     ELSE 1
--   END
-- FROM
--   inserted_products p
--   CROSS JOIN inserted_orders o
-- WHERE
--   -- Заказ 1 (15 ноября)
--   (o.created_at = '2023-11-15 10:30:00' AND p.name IN ('RGB LED Лампа', 'Коврик для мыши "Киберпанк"')) OR
--   -- Заказ 2 (16 ноября)
--   (o.created_at = '2023-11-16 14:45:00' AND p.name IN ('3D ночник "Звездные Войны"', 'Набор постеров "Классические игры"', 'USB Хаб "Ретро компьютер"')) OR
--   -- Заказ 3 (17 ноября)
--   (o.created_uuid = '2023-11-17 09:15:00' AND p.name IN ('Кресло геймерское "CyberThrone"', 'Настольная лампа "Тетрис"'));