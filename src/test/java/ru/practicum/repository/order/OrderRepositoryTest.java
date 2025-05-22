//package ru.practicum.repository.order;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//import ru.practicum.model.cart.Cart;
//import ru.practicum.model.order.Order;
//import ru.practicum.model.user.User;
//import ru.practicum.repository.cart.CartRepository;
//import ru.practicum.repository.product.ProductRepository;
//import ru.practicum.repository.user.UserRepository;
//
//import java.util.List;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@DataJpaTest
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@Testcontainers
//class OrderRepositoryTest {
//
//    @Autowired
//    private OrderRepository orderRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private CartRepository cartRepository;
//
//    @Autowired
//    private ProductRepository productRepository;
//
//    private User testUser;
//    private Cart testCart;
//    private Order testOrder;
//
//    @BeforeEach
//    void setUp() {
//        // Очистка данных перед каждым тестом
//        orderRepository.deleteAll();
//        cartRepository.deleteAll();
//        userRepository.deleteAll();
//        productRepository.deleteAll();
//
//        // Создаем тестового пользователя
//        testUser = new User();
//        testUser.setUsername("гость");
//        testUser = userRepository.save(testUser);
//
//        // Создаем тестовую корзину
//        testCart = new Cart();
//        testCart.setUserUuid(testUser.getUuid());
//        testCart = cartRepository.save(testCart);
//
//        // Создаем тестовый заказ
//        testOrder = new Order();
//        testOrder.setUserUuid(testUser.getUuid());
//        testOrder.setCartUuid(testCart.getUuid());
//        testOrder.setStatus("CREATED");
//        testOrder = orderRepository.save(testOrder);
//    }
//
//    @Test
//    void findByUserUuid_shouldReturnOrders_whenUserHasOrders() {
//        // When
//        List<Order> foundOrders = orderRepository.findByUserUuid(testUser.getUuid());
//
//        // Then
//        assertThat(foundOrders)
//                .hasSize(1)
//                .first()
//                .satisfies(order -> {
//                    assertThat(order.getUserUuid()).isEqualTo(testUser.getUuid());
//                    assertThat(order.getCartUuid()).isEqualTo(testCart.getUuid());
//                    assertThat(order.getStatus()).isEqualTo("CREATED");
//                });
//    }
//
//    @Test
//    void findByUserUuid_shouldReturnEmptyList_whenUserHasNoOrders() {
//        // Given
//        UUID nonExistingUserUuid = UUID.randomUUID();
//
//        // When
//        List<Order> foundOrders = orderRepository.findByUserUuid(nonExistingUserUuid);
//
//        // Then
//        assertThat(foundOrders).isEmpty();
//    }
//
//    @Test
//    void findByUserUuid_shouldReturnMultipleOrders_whenUserHasSeveralOrders() {
//        // Given
//        Order secondOrder = new Order();
//        secondOrder.setUserUuid(testUser.getUuid());
//        secondOrder.setCartUuid(testCart.getUuid());
//        secondOrder.setStatus("PAID");
//        orderRepository.save(secondOrder);
//
//        // When
//        List<Order> foundOrders = orderRepository.findByUserUuid(testUser.getUuid());
//
//        // Then
//        assertThat(foundOrders)
//                .hasSize(2)
//                .extracting(Order::getUserUuid)
//                .containsOnly(testUser.getUuid());
//    }
//}
