package ru.practicum.repository.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.practicum.config.TestR2dbcConfiguration;
import ru.practicum.dao.order.OrderItemDao;

import java.math.BigDecimal;
import java.util.UUID;

@DataR2dbcTest
@Import(TestR2dbcConfiguration.class)
@ActiveProfiles("test")
class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private DatabaseClient client;

    private UUID orderUuid;
    private UUID productUuid;

    @BeforeEach
    void setup() {

        client.sql("""
                            INSERT INTO products (name, description, price)
                            VALUES ('Test Product', 'Test Description', 10.50)
                            RETURNING product_uuid
                        """)
                .map((row, metadata) -> row.get("product_uuid", UUID.class))
                .first()
                .flatMap(productId -> {
                    productUuid = productId;
                    return client.sql("""
                                        INSERT INTO users (username, email, password)
                                        VALUES ('testuser', 'user@test.com', 'testuserpassword')
                                        RETURNING user_uuid
                                    """)
                            .map((row, metadata) -> row.get("user_uuid", UUID.class))
                            .first();
                })
                .flatMap(userId -> client.sql("""
                                    INSERT INTO carts (user_uuid)
                                    VALUES ((SELECT user_uuid FROM users LIMIT 1))
                                    RETURNING cart_uuid
                                """)
                        .map((row, metadata) -> row.get("cart_uuid", UUID.class))
                        .first()
                        .flatMap(cartId -> client.sql("""
                                            INSERT INTO orders (user_uuid, cart_uuid, total_amount)
                                            VALUES (
                                            (SELECT user_uuid FROM users LIMIT 1),
                                            (SELECT cart_uuid FROM carts LIMIT 1),
                                                    10.50)
                                            RETURNING order_uuid
                                        """)
                                .map((row, metadata) -> row.get("order_uuid", UUID.class))
                                .first()
                                .flatMap(orderId -> {
                                    orderUuid = orderId;

                                    OrderItemDao item = OrderItemDao.builder()
                                            .orderUuid(orderUuid)
                                            .productUuid(productUuid)
                                            .quantity(2)
                                            .priceAtOrder(new BigDecimal("10.50"))
                                            .build();

                                    return orderItemRepository.deleteAll()
                                            .then(orderItemRepository.save(item));
                                })
                        )
                )
                .block();


    }

    @Test
    void testFindByOrderUuidReturnsCorrectItems() {
        StepVerifier.create(orderItemRepository.findByOrderUuid(orderUuid))
                .expectNextMatches(orderItem ->
                        orderItem.getOrderUuid().equals(orderUuid) &&
                                orderItem.getProductUuid().equals(productUuid) &&
                                orderItem.getQuantity() == 2 &&
                                orderItem.getPriceAtOrder().compareTo(new BigDecimal("10.50")) == 0
                )
                .verifyComplete();
    }
}
