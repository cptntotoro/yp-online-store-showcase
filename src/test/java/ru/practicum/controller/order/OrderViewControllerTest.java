package ru.practicum.controller.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.exception.order.OrderNotFoundException;
import ru.practicum.mapper.order.OrderMapper;
import ru.practicum.model.order.Order;
import ru.practicum.service.order.OrderService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderViewControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private Model model;

    @InjectMocks
    private OrderViewController orderViewController;

    @Test
    void showOrderList_ShouldReturnOrdersViewWithAttributes() {
        UUID userUuid = UUID.randomUUID();
        Order order1 = new Order();
        Order order2 = new Order();
        OrderDto orderDto1 = new OrderDto();
        OrderDto orderDto2 = new OrderDto();
        List<Order> orders = List.of(order1, order2);
        BigDecimal totalAmount = BigDecimal.valueOf(100.0);

        when(orderService.getUserOrders(userUuid)).thenReturn(orders);
        when(orderMapper.orderToOrderDto(order1)).thenReturn(orderDto1);
        when(orderMapper.orderToOrderDto(order2)).thenReturn(orderDto2);
        when(orderService.getUserTotalAmount(userUuid)).thenReturn(totalAmount);

        String viewName = orderViewController.showOrderList(userUuid, model);

        assertEquals("order/orders", viewName);
        verify(model).addAttribute("orders", List.of(orderDto1, orderDto2));
        verify(model).addAttribute("hasOrders", true);
        verify(model).addAttribute("cartTotal", totalAmount);
    }

    @Test
    void showOrderList_WithEmptyOrders_ShouldSetHasOrdersFalse() {
        UUID userUuid = UUID.randomUUID();
        when(orderService.getUserOrders(userUuid)).thenReturn(List.of());

        orderViewController.showOrderList(userUuid, model);

        verify(model).addAttribute(eq("hasOrders"), eq(false));
    }

    @Test
    void showOrderDetails_ShouldReturnOrderViewWithOrder() {
        UUID userUuid = UUID.randomUUID();
        UUID orderUuid = UUID.randomUUID();
        Order order = new Order();
        OrderDto orderDto = new OrderDto();

        when(orderService.getByUuid(userUuid, orderUuid)).thenReturn(order);
        when(orderMapper.orderToOrderDto(order)).thenReturn(orderDto);

        String viewName = orderViewController.showOrderDetails(userUuid, orderUuid, model);

        assertEquals("order/order", viewName);
        verify(model).addAttribute("order", orderDto);
    }

    @Test
    void cancel_ShouldCancelOrderAndRedirect() {
        UUID userUuid = UUID.randomUUID();
        UUID orderUuid = UUID.randomUUID();

        String redirectUrl = orderViewController.cancel(userUuid, orderUuid);

        assertEquals("redirect:/orders/" + orderUuid, redirectUrl);
        verify(orderService).cancel(userUuid, orderUuid);
    }

    @Test
    void showOrderDetails_WithInvalidOrder_ShouldThrowException() {
        UUID userUuid = UUID.randomUUID();
        UUID invalidOrderUuid = UUID.randomUUID();

        when(orderService.getByUuid(userUuid, invalidOrderUuid))
                .thenThrow(new OrderNotFoundException("Order not found"));

        assertThrows(OrderNotFoundException.class, () -> orderViewController.showOrderDetails(userUuid, invalidOrderUuid, model));
    }
}
