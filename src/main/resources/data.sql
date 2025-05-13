-- Вставка продуктов
INSERT INTO products (product_uuid, name, description, price, image_url) VALUES
    (gen_random_uuid(), 'Уточка ночник', 'Ночник в виде уставшей Уточки. Делает комнату уютнее и теплее.', 1700.00, '/images/utochka-nochnik.jpg'),
    (gen_random_uuid(), 'Это Хорошо Собака Конструктор', 'Gobricks MOC Это Хорошо Собака Конструктор, Желтый', 1861.00, '/images/eto-horosho.jpg'),
    (gen_random_uuid(), 'Сборный маяк Толбухин', 'Набор для сборки 3d-модели реально существующего маяка Толбухин из Кронштадта', 4900.00, '/images/sbornyy-mayak.jpg'),
    (gen_random_uuid(), 'Манэки-нэко - Кот удачи со сменной лапой', 'Распространённая японская скульптура, часто сделанная из фарфора или керамики, которая, как полагают, приносит её владельцу удачу', 2650.00, '/images/maneki_neko.jpg'),
    (gen_random_uuid(), 'Звонок для вызова Friend Function', 'Позовите консьержа, позовите семью на ужин, позовите удачу, позовите хоть кого-нибудь, только не молчите!', 700.00, '/images/friendfunction_podarki_14.jpg'),
    (gen_random_uuid(), 'Мультитул The Modern Man Multi-Tool', 'Этот мультитул в виде кредитной карты будет всегда с вами и поможет в любой ситуации.', 770.00, '/images/multitool.jpg'),
    (gen_random_uuid(), 'Плакат Partisan Press Не все не сразу и поспать', 'Напечатано неидеально и с удовольствием.', 2400.00, '/images/plakat_ne_vse_ne_srazu.jpg'),
    (gen_random_uuid(), 'Утка классическая резиновая очень большая (30 см) цвет Желтый', 'Уточки для повседневной жизни подходят как нельзя лучше - поднимут настроение вам, и вам, и всем.', 3850.00, '/images/utochka_rezinovaya.jpg'),
    (gen_random_uuid(), 'Плакат Allmodernism Академия РАН', 'Неофициальное название — «Золотые мозги».', 2400.00, '/images/plakat_allmodernism_akademiya_ran.jpg'),
    (gen_random_uuid(), 'Интерьерный конструктор Robotime Sam''s Study Library', 'Румбокс интерьерный Sam''s Study Library - сборная модель сказочной миниатюрной библиотеки Сэма. На полках вы найдете книги по философии и искусству, инструкции и романы.', 4200.00, '/images/interernyy_konstruktor_robotime.jpg');

-- Создание заказов
INSERT INTO orders (order_uuid, created_at) VALUES
    (gen_random_uuid(), '2023-11-15 10:30:00'),
    (gen_random_uuid(), '2023-11-16 14:45:00'),
    (gen_random_uuid(), '2023-11-17 09:15:00');

-- Добавляем товары в заказы
INSERT INTO order_items (order_item_uuid, order_uuid, product_uuid, quantity) VALUES
    (gen_random_uuid(),
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 0),
    (SELECT product_uuid FROM products WHERE name = 'Уточка ночник' LIMIT 1),
    2),

    (gen_random_uuid(),
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 0),
    (SELECT product_uuid FROM products WHERE name = 'Это Хорошо Собака Конструктор' LIMIT 1),
    1);

-- Заказ 2
INSERT INTO order_items (order_item_uuid, order_uuid, product_uuid, quantity) VALUES
    (gen_random_uuid(),
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 1),
    (SELECT product_uuid FROM products WHERE name = 'Сборный маяк Толбухин' LIMIT 1),
    1),

    (gen_random_uuid(),
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 1),
    (SELECT product_uuid FROM products WHERE name = 'Манэки-нэко - Кот удачи со сменной лапой' LIMIT 1),
    3),

    (gen_random_uuid(),
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 1),
    (SELECT product_uuid FROM products WHERE name = 'Звонок для вызова Friend Function' LIMIT 1),
    2);

-- Заказ 3
INSERT INTO order_items (order_item_uuid, order_uuid, product_uuid, quantity) VALUES
    (gen_random_uuid(),
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 2),
    (SELECT product_uuid FROM products WHERE name = 'Мультитул The Modern Man Multi-Tool' LIMIT 1),
    1),

    (gen_random_uuid(),
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 2),
    (SELECT product_uuid FROM products WHERE name = 'Плакат Partisan Press Не все не сразу и поспать' LIMIT 1),
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