package ru.practicum.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.order.OrderNotFoundException;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.order.Order;
import ru.practicum.model.order.OrderStatus;
import ru.practicum.repository.order.OrderRepository;
import ru.practicum.service.cart.CartService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;

    @Override
    @Transactional
    public Order create(UUID userUuid) {
        Cart cart = cartService.get(userUuid);
        Order order = new Order(userUuid, cart);
        order = orderRepository.save(order);

        // Очищаем корзину после создания заказа
        cartService.clear(userUuid);

        return order;
    }

    @Transactional
    @Override
    public Order updateStatus(UUID orderUuid, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderUuid)
                .orElseThrow(() -> new OrderNotFoundException("Заказ с uuid = " + orderUuid + " не найден и не был обновлен."));

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    @Override
    public List<Order> getUserOrders(UUID userUuid) {
        return orderRepository.findByUserUuid(userUuid);
    }

    @Override
    public Order getByUuid(UUID uuid) {
        return orderRepository.findById(uuid).orElseThrow(() -> new OrderNotFoundException("Заказ не найден"));
    }
}
