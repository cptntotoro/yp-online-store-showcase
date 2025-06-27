package ru.practicum.config.cache.product;

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
import ru.practicum.dto.product.ProductCacheDto;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class RedisProductConfig {

    private final ReactiveRedisConnectionFactory rConnectionFactory;
    private final ObjectMapper redisObjectMapper;

    @Bean
    public ReactiveRedisTemplate<String, ProductCacheDto> productCacheTemplate() {
        Jackson2JsonRedisSerializer<ProductCacheDto> serializer =
                new Jackson2JsonRedisSerializer<>(redisObjectMapper, ProductCacheDto.class);

        RedisSerializationContext<String, ProductCacheDto> context = RedisSerializationContext
                .<String, ProductCacheDto>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();
        return new ReactiveRedisTemplate<>(rConnectionFactory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, List<ProductCacheDto>> productListCacheTemplate() {
        JavaType type = redisObjectMapper.getTypeFactory()
                .constructCollectionType(List.class, ProductCacheDto.class);
        Jackson2JsonRedisSerializer<List<ProductCacheDto>> serializer =
                new Jackson2JsonRedisSerializer<>(redisObjectMapper, type);

        RedisSerializationContext<String, List<ProductCacheDto>> context = RedisSerializationContext
                .<String, List<ProductCacheDto>>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(rConnectionFactory, context);
    }
}
