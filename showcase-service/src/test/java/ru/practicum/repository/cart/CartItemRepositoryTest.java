package ru.practicum.repository.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.test.StepVerifier;
import ru.practicum.config.TestR2dbcConfiguration;

import java.util.UUID;

@DataR2dbcTest
@Import(TestR2dbcConfiguration.class)
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private DatabaseClient databaseClient;

    private final UUID cartUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID productUuid = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @BeforeEach
    void setUp() {
        String setupSql = """
            DELETE FROM cart_items;
            DELETE FROM orders;
            DELETE FROM carts;
            DELETE FROM products;
            DELETE FROM users;

            INSERT INTO users (user_uuid, username, email)
            VALUES ('99999999-9999-9999-9999-999999999999', 'test_user', 'test@example.com');

            INSERT INTO products (product_uuid, name, price)
            VALUES 
              ('22222222-2222-2222-2222-222222222222', 'Product 1', 100.00),
              ('33333333-3333-3333-3333-333333333333', 'Product 2', 200.00);

            INSERT INTO carts (cart_uuid, user_uuid, total_price)
            VALUES ('11111111-1111-1111-1111-111111111111', '99999999-9999-9999-9999-999999999999', 300.00);

            INSERT INTO cart_items (cart_item_uuid, cart_uuid, product_uuid, quantity)
            VALUES 
              (gen_random_uuid(), '11111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', 1),
              (gen_random_uuid(), '11111111-1111-1111-1111-111111111111', '33333333-3333-3333-3333-333333333333', 1);
        """;

        databaseClient.sql(setupSql).then().block();
    }

    @Test
    void shouldFindItemsByCartUuid() {
        StepVerifier.create(cartItemRepository.findByCartUuid(cartUuid))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void shouldDeleteItemByCartUuidAndProductUuid() {
        StepVerifier.create(cartItemRepository.deleteByCartUuidAndProductUuid(cartUuid, productUuid))
                .verifyComplete();

        StepVerifier.create(cartItemRepository.findByCartUuid(cartUuid))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldDeleteAllItemsByCartUuid() {
        StepVerifier.create(cartItemRepository.deleteByCartUuid(cartUuid))
                .verifyComplete();

        StepVerifier.create(cartItemRepository.findByCartUuid(cartUuid))
                .expectNextCount(0)
                .verifyComplete();
    }
}
