-- Вставка пользователей (возвращаем сгенерированные UUID)
WITH inserted_users AS (
    INSERT INTO users (username, email)
        VALUES
            ('user1', 'user1@example.com'),
            ('user2', 'user2@example.com')
        RETURNING user_uuid
),
     user1 AS (
         SELECT user_uuid FROM inserted_users LIMIT 1 OFFSET 0
     ),
     user2 AS (
         SELECT user_uuid FROM inserted_users LIMIT 1 OFFSET 1
     ),

-- Вставка продуктов (возвращаем сгенерированные UUID)
     inserted_products AS (
         INSERT INTO products (name, description, price, image_url)
             VALUES
                 ('Уточка ночник', 'Ночник в виде уставшей Уточки. Делает комнату уютнее и теплее.', 1700.00, '/images/utochka-nochnik.jpg'),
                 ('Это Хорошо Собака Конструктор', 'Gobricks MOC Это Хорошо Собака Конструктор, Желтый', 1861.00, '/images/eto-horosho.jpg'),
                 ('Сборный маяк Толбухин', 'Набор для сборки 3d-модели реально существующего маяка Толбухин из Кронштадта', 4900.00, '/images/sbornyy-mayak.jpg'),
                 ('Манэки-нэко - Кот удачи со сменной лапой', 'Распространённая японская скульптура, часто сделанная из фарфора или керамики, которая, как полагают, приносит её владельцу удачу', 2650.00, '/images/maneki_neko.jpg'),
                 ('Звонок для вызова Friend Function', 'Позовите консьержа, позовите семью на ужин, позовите удачу, позовите хоть кого-нибудь, только не молчите!', 700.00, '/images/friendfunction_podarki_14.jpg'),
                 ('Мультитул The Modern Man Multi-Tool', 'Этот мультитул в виде кредитной карты будет всегда с вами и поможет в любой ситуации.', 770.00, '/images/multitool.jpg'),
                 ('Плакат Partisan Press Не все не сразу и поспать', 'Напечатано неидеально и с удовольствием.', 2400.00, '/images/plakat_ne_vse_ne_srazu.jpg'),
                 ('Утка классическая резиновая очень большая (30 см) цвет Желтый', 'Уточки для повседневной жизни подходят как нельзя лучше - поднимут настроение вам, и вам, и всем.', 3850.00, '/images/utochka_rezinovaya.jpg'),
                 ('Плакат Allmodernism Академия РАН', 'Неофициальное название — «Золотые мозги».', 2400.00, '/images/plakat_allmodernism_akademiya_ran.jpg'),
                 ('Интерьерный конструктор Robotime Sam''s Study Library', 'Румбокс интерьерный Sam''s Study Library - сборная модель сказочной миниатюрной библиотеки Сэма. На полках вы найдете книги по философии и искусству, инструкции и романы.', 4200.00, '/images/interernyy_konstruktor_robotime.jpg')
             RETURNING product_uuid, name
     ),
     product1 AS (SELECT product_uuid FROM inserted_products WHERE name = 'Уточка ночник'),
     product2 AS (SELECT product_uuid FROM inserted_products WHERE name = 'Это Хорошо Собака Конструктор'),
     product3 AS (SELECT product_uuid FROM inserted_products WHERE name = 'Сборный маяк Толбухин'),
     product4 AS (SELECT product_uuid FROM inserted_products WHERE name = 'Манэки-нэко - Кот удачи со сменной лапой'),
     product5 AS (SELECT product_uuid FROM inserted_products WHERE name = 'Звонок для вызова Friend Function'),
     product6 AS (SELECT product_uuid FROM inserted_products WHERE name = 'Мультитул The Modern Man Multi-Tool'),
     product7 AS (SELECT product_uuid FROM inserted_products WHERE name = 'Плакат Partisan Press Не все не сразу и поспать'),
     product8 AS (SELECT product_uuid FROM inserted_products WHERE name = 'Утка классическая резиновая очень большая (30 см) цвет Желтый'),
     product9 AS (SELECT product_uuid FROM inserted_products WHERE name = 'Плакат Allmodernism Академия РАН'),
     product10 AS (SELECT product_uuid FROM inserted_products WHERE name = 'Интерьерный конструктор Robotime Sam''s Study Library'),

-- Вставка корзин для пользователей
     inserted_carts AS (
         INSERT INTO carts (user_uuid)
             SELECT user_uuid FROM inserted_users
             RETURNING cart_uuid, user_uuid
     ),
     cart_user1 AS (SELECT cart_uuid FROM inserted_carts JOIN user1 ON inserted_carts.user_uuid = user1.user_uuid),
     cart_user2 AS (SELECT cart_uuid FROM inserted_carts JOIN user2 ON inserted_carts.user_uuid = user2.user_uuid),

-- Вставка заказов
     inserted_orders AS (
         INSERT INTO orders (user_uuid, cart_uuid, status)
             VALUES
                 ((SELECT user_uuid FROM user1), (SELECT cart_uuid FROM cart_user1), 'CREATED'),
                 ((SELECT user_uuid FROM user1), (SELECT cart_uuid FROM cart_user1), 'PROCESSING'),
                 ((SELECT user_uuid FROM user2), (SELECT cart_uuid FROM cart_user2), 'COMPLETED')
             RETURNING order_uuid, created_at
     ),
     order1 AS (SELECT order_uuid FROM inserted_orders ORDER BY created_at LIMIT 1 OFFSET 0),
     order2 AS (SELECT order_uuid FROM inserted_orders ORDER BY created_at LIMIT 1 OFFSET 1),
     order3 AS (SELECT order_uuid FROM inserted_orders ORDER BY created_at LIMIT 1 OFFSET 2)

-- Вставка элементов заказов
INSERT INTO order_items (order_uuid, product_uuid, quantity, price_at_order)
-- Заказ 1
SELECT
    (SELECT order_uuid FROM order1),
    (SELECT product_uuid FROM product1),
    2,
    (SELECT price FROM products WHERE product_uuid = (SELECT product_uuid FROM product1))
UNION ALL
SELECT
    (SELECT order_uuid FROM order1),
    (SELECT product_uuid FROM product2),
    1,
    (SELECT price FROM products WHERE product_uuid = (SELECT product_uuid FROM product2))
UNION ALL
-- Заказ 2
SELECT
    (SELECT order_uuid FROM order2),
    (SELECT product_uuid FROM product3),
    1,
    (SELECT price FROM products WHERE product_uuid = (SELECT product_uuid FROM product3))
UNION ALL
SELECT
    (SELECT order_uuid FROM order2),
    (SELECT product_uuid FROM product4),
    3,
    (SELECT price FROM products WHERE product_uuid = (SELECT product_uuid FROM product4))
UNION ALL
SELECT
    (SELECT order_uuid FROM order2),
    (SELECT product_uuid FROM product5),
    2,
    (SELECT price FROM products WHERE product_uuid = (SELECT product_uuid FROM product5))
UNION ALL
-- Заказ 3
SELECT
    (SELECT order_uuid FROM order3),
    (SELECT product_uuid FROM product6),
    1,
    (SELECT price FROM products WHERE product_uuid = (SELECT product_uuid FROM product6))
UNION ALL
SELECT
    (SELECT order_uuid FROM order3),
    (SELECT product_uuid FROM product7),
    1,
    (SELECT price FROM products WHERE product_uuid = (SELECT product_uuid FROM product7));

-- Обновляем общую сумму заказов
UPDATE orders o
SET total_amount = (
    SELECT SUM(quantity * price_at_order)
    FROM order_items
    WHERE order_uuid = o.order_uuid
);