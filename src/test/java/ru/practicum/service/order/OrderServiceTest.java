package ru.practicum.service.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.cart.CartItem;
import ru.practicum.model.order.Order;
import ru.practicum.model.order.OrderStatus;
import ru.practicum.model.product.Product;
import ru.practicum.repository.order.OrderRepository;
import ru.practicum.service.cart.CartService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private final UUID testUserId = UUID.randomUUID();

    @Test
    void create_shouldCreateOrderWithCartItemsAndClearCart() {
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Cart cart = new Cart();
        cart.setUuid(cartId);
        cart.setUserUuid(userId);

        Product product = new Product();
        product.setUuid(productId);
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100));

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);

        cart.setItems(new ArrayList<>(List.of(cartItem)));

        when(cartService.get(userId)).thenReturn(cart);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setUuid(UUID.randomUUID());
            return order;
        });

        Order createdOrder = orderService.create(userId);

        assertThat(createdOrder).isNotNull();
        assertThat(createdOrder.getUserUuid()).isEqualTo(userId);
        assertThat(createdOrder.getCart().getUuid()).isEqualTo(cartId);
        assertThat(createdOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(createdOrder.getTotalPrice())
                .isEqualByComparingTo(BigDecimal.valueOf(200)); // 100 * 2

        assertThat(createdOrder.getItems())
                .hasSize(1)
                .first()
                .satisfies(item -> {
                    assertThat(item.getProduct().getUuid()).isEqualTo(productId);
                    assertThat(item.getQuantity()).isEqualTo(2);
                    assertThat(item.getPriceAtOrder()).isEqualByComparingTo(BigDecimal.valueOf(100));
                });

        verify(cartService).clear(userId);

        assertThat(cart.getItems()).hasSize(1); // Оригинальная корзина не должна измениться
    }

    @Test
    void getUserOrders_shouldReturnUserOrders() {
        Order order1 = new Order();
        order1.setUserUuid(testUserId);

        Order order2 = new Order();
        order2.setUserUuid(testUserId);

        List<Order> userOrders = List.of(order1, order2);

        when(orderRepository.findByUserUuid(testUserId)).thenReturn(userOrders);

        List<Order> result = orderService.getUserOrders(testUserId);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(order -> order.getUserUuid().equals(testUserId));
    }
}