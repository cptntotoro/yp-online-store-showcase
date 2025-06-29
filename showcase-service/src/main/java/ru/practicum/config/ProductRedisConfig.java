//package ru.practicum.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
//import org.springframework.data.redis.core.ReactiveRedisTemplate;
//import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.RedisSerializationContext;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//import ru.practicum.dto.product.ProductOutDto;
//
//@Configuration
//public class ProductRedisConfig {
//
//    @Bean
//    public ReactiveRedisTemplate<String, ProductOutDto> productDtoRedisTemplate(
//            ReactiveRedisConnectionFactory factory) {
//        Jackson2JsonRedisSerializer<ProductOutDto> serializer =
//                new Jackson2JsonRedisSerializer<>(ProductOutDto.class);
//
//        RedisSerializationContext.RedisSerializationContextBuilder<String, ProductOutDto> builder =
//                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
//
//        RedisSerializationContext<String, ProductOutDto> context =
//                builder.value(serializer).build();
//
//        return new ReactiveRedisTemplate<>(factory, context);
//    }
//}