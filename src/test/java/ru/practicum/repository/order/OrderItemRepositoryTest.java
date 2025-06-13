package ru.practicum.repository.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.r2dbc.core.DatabaseClient;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;
import ru.practicum.dao.order.OrderItemDao;

import java.math.BigDecimal;
import java.util.UUID;

@DataR2dbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private DatabaseClient client;

    private UUID orderUuid;
    private UUID productUuid;

    @BeforeEach
    void setup() {
        orderUuid = UUID.randomUUID();
        productUuid = UUID.randomUUID();

        client.sql("""
                            INSERT INTO products (product_uuid, name, description, price)
                            VALUES (:productUuid, 'Test Product', 'Test Description', 10.50)
                        """).bind("productUuid", productUuid).fetch().rowsUpdated()
                .then(client.sql("""
                            INSERT INTO users (user_uuid, username, email)
                            VALUES (:userUuid, 'testuser', 'user@test.com')
                        """).bind("userUuid", UUID.randomUUID()).fetch().rowsUpdated())
                .then(client.sql("""
                            INSERT INTO carts (cart_uuid, user_uuid)
                            VALUES (:cartUuid, (SELECT user_uuid FROM users LIMIT 1))
                        """).bind("cartUuid", UUID.randomUUID()).fetch().rowsUpdated())
                .then(client.sql("""
                            INSERT INTO orders (order_uuid, user_uuid, cart_uuid, total_amount)
                            VALUES (:orderUuid,
                                    (SELECT user_uuid FROM users LIMIT 1),
                                    (SELECT cart_uuid FROM carts LIMIT 1),
                                    10.50)
                        """).bind("orderUuid", orderUuid).fetch().rowsUpdated())
                .block();

        OrderItemDao item = OrderItemDao.builder()
                .orderUuid(orderUuid)
                .productUuid(productUuid)
                .quantity(2)
                .priceAtOrder(new BigDecimal("10.50"))
                .build();

        orderItemRepository.deleteAll()
                .then(orderItemRepository.save(item))
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
