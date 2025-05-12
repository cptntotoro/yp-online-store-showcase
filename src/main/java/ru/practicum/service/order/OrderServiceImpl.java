package ru.practicum.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.exception.order.OrderNotFoundException;
import ru.practicum.model.CartItem;
import ru.practicum.model.Order;
import ru.practicum.model.OrderItem;
import ru.practicum.model.Product;
import ru.practicum.repository.order.OrderRepository;
import ru.practicum.service.product.ProductServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ProductServiceImpl productService;

    @Override
    public Order add(List<CartItem> cartItems) {
        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());

        Set<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    Product product = productService.getByUuid(cartItem.getProductUuid());

                    return OrderItem.builder()
                            .product(product)
                            .quantity(cartItem.getQuantity())
                            .order(order)
                            .build();
                })
                .collect(Collectors.toSet());

        order.setItems(orderItems);
        return orderRepository.save(order);
    }

    @Override
    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order getByUuid(UUID uuid) {
        return orderRepository.findById(uuid).orElseThrow(() -> new OrderNotFoundException("Заказ не найден"));
    }
}
