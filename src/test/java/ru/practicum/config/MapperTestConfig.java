package ru.practicum.config;

import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.mapper.order.OrderItemMapper;
import ru.practicum.mapper.order.OrderMapper;

@TestConfiguration
public class MapperTestConfig {

    @Bean
    public OrderMapper orderMapper() {
        return Mappers.getMapper(OrderMapper.class);
    }

    @Bean
    public OrderItemMapper orderItemMapper() {
        return Mappers.getMapper(OrderItemMapper.class);
    }

    @Bean
    public CartMapper cartMapper() {
        return Mappers.getMapper(CartMapper.class);
    }
}
