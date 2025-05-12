package ru.practicum.service.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.exception.order.OrderNotFoundException;
import ru.practicum.model.*;
import ru.practicum.repository.order.OrderRepository;
import ru.practicum.service.product.ProductServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductServiceImpl productService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void add_shouldCreateOrderFromCartItems() {
        // given
        UUID productUuid = UUID.randomUUID();
        int quantity = 2;

        CartItem cartItem = new CartItem();
        cartItem.setProductUuid(productUuid);
        cartItem.setQuantity(quantity);

        Product product = new Product();
        product.setUuid(productUuid);
        product.setName("Test Product");
        product.setPrice(100.0);

        Order expectedOrder = new Order();
        expectedOrder.setCreatedAt(LocalDateTime.now());

        OrderItem expectedOrderItem = OrderItem.builder()
                .product(product)
                .quantity(quantity)
                .order(expectedOrder)
                .build();

        expectedOrder.setItems(Set.of(expectedOrderItem));

        when(productService.getByUuid(productUuid)).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);

        // when
        Order result = orderService.add(List.of(cartItem));

        // then
        assertNotNull(result);
        assertNotNull(result.getCreatedAt());
        assertEquals(1, result.getItems().size());

        OrderItem resultItem = result.getItems().iterator().next();
        assertEquals(product, resultItem.getProduct());
        assertEquals(quantity, resultItem.getQuantity());

        verify(productService).getByUuid(productUuid);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void getAll_shouldReturnAllOrders() {
        // given
        Order order1 = new Order();
        Order order2 = new Order();
        List<Order> expectedOrders = List.of(order1, order2);

        when(orderRepository.findAll()).thenReturn(expectedOrders);

        // when
        List<Order> result = orderService.getAll();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    void getByUuid_whenOrderExists_shouldReturnOrder() {
        // given
        UUID orderUuid = UUID.randomUUID();
        Order expectedOrder = new Order();

        when(orderRepository.findById(orderUuid)).thenReturn(Optional.of(expectedOrder));

        // when
        Order result = orderService.getByUuid(orderUuid);

        // then
        assertNotNull(result);
        assertEquals(expectedOrder, result);
        verify(orderRepository).findById(orderUuid);
    }

    @Test
    void getByUuid_whenOrderNotExists_shouldThrowException() {
        // given
        UUID orderUuid = UUID.randomUUID();

        when(orderRepository.findById(orderUuid)).thenReturn(Optional.empty());

        // when & then
        assertThrows(OrderNotFoundException.class, () -> orderService.getByUuid(orderUuid));
        verify(orderRepository).findById(orderUuid);
    }
}