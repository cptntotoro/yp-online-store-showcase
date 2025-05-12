package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.model.CartItem;
import ru.practicum.model.Order;
import ru.practicum.model.OrderItem;
import ru.practicum.model.Product;
import ru.practicum.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductService productService;

    public Order createOrder(List<CartItem> cartItems) {
        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());

        Set<OrderItem> orderItems = cartItems.stream()
                .map(cartItem -> {
                    Product product = productService.getProductById(cartItem.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found"));

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

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }
}
