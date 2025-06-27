package ru.practicum.config.cache.cart;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.practicum.dto.cart.cache.CartCacheDto;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class RedisCartConfig {

    private final ReactiveRedisConnectionFactory rConnectionFactory;
    private final ObjectMapper redisObjectMapper;

    @Bean
    public ReactiveRedisTemplate<String, CartCacheDto> cartCacheTemplate() {
        Jackson2JsonRedisSerializer<CartCacheDto> serializer =
                new Jackson2JsonRedisSerializer<>(redisObjectMapper, CartCacheDto.class);

        RedisSerializationContext<String, CartCacheDto> context = RedisSerializationContext
                .<String, CartCacheDto>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();
        return new ReactiveRedisTemplate<>(rConnectionFactory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, List<CartCacheDto>> cartListCacheTemplate() {
        JavaType type = redisObjectMapper.getTypeFactory()
                .constructCollectionType(List.class, CartCacheDto.class);
        Jackson2JsonRedisSerializer<List<CartCacheDto>> serializer =
                new Jackson2JsonRedisSerializer<>(redisObjectMapper, type);

        RedisSerializationContext<String, List<CartCacheDto>> context = RedisSerializationContext
                .<String, List<CartCacheDto>>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(rConnectionFactory, context);
    }
}
