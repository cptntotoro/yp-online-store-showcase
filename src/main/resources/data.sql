-- Вставка продуктов
INSERT INTO products (name, description, price, image_url) VALUES
    ('Уточка ночник', 'Ночник в виде уставшей Уточки. Делает комнату уютнее и теплее.', 1700.00, '/images/utochka-nochnik.jpg'),
    ('Это Хорошо Собака Конструктор', 'Gobricks MOC Это Хорошо Собака Конструктор, Желтый', 1861.00, '/images/eto-horosho.jpg'),
    ('Сборный маяк Толбухин', 'Набор для сборки 3d-модели реально существующего маяка Толбухин из Кронштадта', 4900.00, '/images/sbornyy-mayak.jpg'),
    ('Манэки-нэко - Кот удачи со сменной лапой', 'Распространённая японская скульптура, часто сделанная из фарфора или керамики, которая, как полагают, приносит её владельцу удачу', 2650.00, '/images/maneki_neko.jpg'),
    ('Звонок для вызова Friend Function', 'Позовите консьержа, позовите семью на ужин, позовите удачу, позовите хоть кого-нибудь, только не молчите!', 700.00, '/images/friendfunction_podarki_14.jpg'),
    ('Мультитул The Modern Man Multi-Tool', 'Этот мультитул в виде кредитной карты будет всегда с вами и поможет в любой ситуации.', 770.00, '/images/multitool.jpg'),
    ('Плакат Partisan Press Не все не сразу и поспать', 'Напечатано неидеально и с удовольствием.', 2400.00, '/images/plakat_ne_vse_ne_srazu.jpg'),
    ('Утка классическая резиновая очень большая (30 см) цвет Желтый', 'Уточки для повседневной жизни подходят как нельзя лучше - поднимут настроение вам, и вам, и всем.', 3850.00, '/images/utochka_rezinovaya.jpg'),
    ('Плакат Allmodernism Академия РАН', 'Неофициальное название — «Золотые мозги».', 2400.00, '/images/plakat_allmodernism_akademiya_ran.jpg'),
    ('Интерьерный конструктор Robotime Sam''s Study Library', 'Румбокс интерьерный Sam''s Study Library - сборная модель сказочной миниатюрной библиотеки Сэма. На полках вы найдете книги по философии и искусству, инструкции и романы.', 4200.00, '/images/interernyy_konstruktor_robotime.jpg');

-- Создание заказов
INSERT INTO orders (created_at) VALUES
    ('2023-11-15 10:30:00'),
    ('2023-11-16 14:45:00'),
    ('2023-11-17 09:15:00');

-- Добавляем товары в заказы
INSERT INTO order_items (order_uuid, product_uuid, quantity) VALUES
    (
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 0),
    (SELECT product_uuid FROM products WHERE name = 'Уточка ночник' LIMIT 1),
    2),

    (
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 0),
    (SELECT product_uuid FROM products WHERE name = 'Это Хорошо Собака Конструктор' LIMIT 1),
    1);

-- Заказ 2
INSERT INTO order_items (order_uuid, product_uuid, quantity) VALUES
    (
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 1),
    (SELECT product_uuid FROM products WHERE name = 'Сборный маяк Толбухин' LIMIT 1),
    1),

    (
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 1),
    (SELECT product_uuid FROM products WHERE name = 'Манэки-нэко - Кот удачи со сменной лапой' LIMIT 1),
    3),

    (
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 1),
    (SELECT product_uuid FROM products WHERE name = 'Звонок для вызова Friend Function' LIMIT 1),
    2);

-- Заказ 3
INSERT INTO order_items (order_uuid, product_uuid, quantity) VALUES
    (
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 2),
    (SELECT product_uuid FROM products WHERE name = 'Мультитул The Modern Man Multi-Tool' LIMIT 1),
    1),

    (
    (SELECT order_uuid FROM orders ORDER BY created_at LIMIT 1 OFFSET 2),
    (SELECT product_uuid FROM products WHERE name = 'Плакат Partisan Press Не все не сразу и поспать' LIMIT 1),
    1);