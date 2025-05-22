package ru.practicum.repository.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.cart.CartItem;
import ru.practicum.model.order.Order;
import ru.practicum.model.order.OrderStatus;
import ru.practicum.model.product.Product;
import ru.practicum.model.user.User;
import ru.practicum.repository.cart.CartRepository;
import ru.practicum.repository.product.ProductRepository;
import ru.practicum.repository.user.UserRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    private UUID testUserUuid;
    private Cart testCart;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Очищаем все таблицы в правильном порядке из-за foreign key constraints
        orderRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        // Создаем тестового пользователя
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user = userRepository.save(user);
        testUserUuid = user.getUuid();

        // Создаем тестовый продукт
        testProduct = new Product();
        testProduct.setName("Test Product");
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(100.0));
        testProduct = productRepository.save(testProduct);

        // Создаем тестовую корзину с товаром
        testCart = new Cart();
        testCart.setUserUuid(testUserUuid);
        testCart = cartRepository.save(testCart);

        CartItem cartItem = new CartItem(testCart, testProduct, 1);
        testCart.getItems().add(cartItem);
        cartRepository.save(testCart); // Сохраняем корзину с элементом
    }

    private Order createTestOrder(OrderStatus status) {
        Order order = new Order(testUserUuid, testCart);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Test
    void findByUserUuid_shouldReturnEmptyList_whenNoOrdersExist() {
        List<Order> result = orderRepository.findByUserUuid(testUserUuid);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserUuid_shouldReturnOrders_whenOrdersExist() {
        createTestOrder(OrderStatus.CREATED);
        createTestOrder(OrderStatus.PROCESSING);

        List<Order> result = orderRepository.findByUserUuid(testUserUuid);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(o -> o.getUserUuid().equals(testUserUuid)));
    }

    @Test
    void findByUserUuid_shouldReturnOnlyUserOrders_whenMultipleUsersExist() {
        // Создаем второго пользователя
        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2 = userRepository.save(user2);

        // Создаем заказы для обоих пользователей
        createTestOrder(OrderStatus.CREATED); // для testUserUuid
        createTestOrder(OrderStatus.PAID);    // для testUserUuid

        // Создаем корзину и заказ для второго пользователя
        Cart cart2 = new Cart();
        cart2.setUserUuid(user2.getUuid());
        cart2 = cartRepository.save(cart2);

        CartItem cartItem2 = new CartItem(cart2, testProduct, 2);
        cart2.getItems().add(cartItem2);
        cartRepository.save(cart2);

        Order order3 = new Order(user2.getUuid(), cart2);
        orderRepository.save(order3);

        List<Order> result = orderRepository.findByUserUuid(testUserUuid);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(o -> o.getUserUuid().equals(testUserUuid)));
    }

    @Test
    void findByUserUuid_shouldReturnOrdersWithCorrectItems() {
        Order order = createTestOrder(OrderStatus.CREATED);

        List<Order> result = orderRepository.findByUserUuid(testUserUuid);

        assertEquals(1, result.size());
        Order foundOrder = result.getFirst();
        assertEquals(1, foundOrder.getItems().size());
        assertEquals(testProduct.getUuid(), foundOrder.getItems().getFirst().getProduct().getUuid());
        assertEquals(1, foundOrder.getItems().getFirst().getQuantity());
    }
}