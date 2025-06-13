//package ru.practicum.controller.payment;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.ui.Model;
//import reactor.core.publisher.Mono;
//import ru.practicum.dto.order.OrderDto;
//import ru.practicum.mapper.order.OrderMapper;
//import ru.practicum.model.order.Order;
//import ru.practicum.service.cart.CartService;
//import ru.practicum.service.order.OrderService;
//import ru.practicum.service.payment.PaymentService;
//import java.util.UUID;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class PaymentViewControllerTest {
//
//    @Mock
//    private OrderService orderService;
//
//    @Mock
//    private PaymentService paymentService;
//
//    @Mock
//    private CartService cartService;
//
//    @Mock
//    private OrderMapper orderMapper;
//
//    @Mock
//    private Model model;
//
//    @InjectMocks
//    private PaymentViewController paymentViewController;
//
//    @Test
//    void previewOrder_ShouldCreateOrderClearCartAndReturnPaymentView() {
//        UUID userUuid = UUID.randomUUID();
//        Order mockOrder = new Order();
//        OrderDto mockOrderDto = new OrderDto();
//
//        when(orderService.create(userUuid)).thenReturn(Mono.just(mockOrder));
//        when(cartService.clear(userUuid)).thenReturn(Mono.empty());
//        when(orderMapper.orderToOrderDto(mockOrder)).thenReturn(mockOrderDto);
//
//        String viewName = paymentViewController.previewOrder(userUuid, model).block();
//
//        assertEquals("payment/payment", viewName);
//        verify(orderService).create(userUuid);
//        verify(cartService).clear(userUuid);
//        verify(model).addAttribute("order", mockOrderDto);
//    }
//
//    @Test
//    void checkout_ShouldProcessPaymentAndRedirectToOrder() {
//        UUID userUuid = UUID.randomUUID();
//        UUID orderUuid = UUID.randomUUID();
//        String cardNumber = "1234567812345678";
//
//        when(paymentService.checkout(userUuid, orderUuid, cardNumber)).thenReturn(Mono.empty());
//
//        String redirectUrl = paymentViewController.checkout(userUuid, orderUuid, cardNumber).block();
//
//        assertEquals("redirect:/orders/" + orderUuid, redirectUrl);
//        verify(paymentService).checkout(userUuid, orderUuid, cardNumber);
//    }
//}