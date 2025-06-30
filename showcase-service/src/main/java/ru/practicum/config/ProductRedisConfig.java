package ru.practicum.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;
import ru.practicum.dto.product.ProductCacheDto;
import ru.practicum.model.product.Product;

import java.time.Duration;
import java.util.List;

@Configuration
public class ProductRedisConfig {

    @Bean
    @Primary
    public ReactiveRedisConnectionFactory rConnectionFactory(
            @Value("${spring.data.redis.lettuce.host}") String host,
            @Value("${spring.data.redis.lettuce.port}") int port,
            @Value("${spring.data.redis.lettuce.password}") String password) {

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        if (StringUtils.hasText(password)) {
            config.setPassword(password);
        }
        return new LettuceConnectionFactory(config);
    }

//    @Bean
//    public CacheManager cacheManager(RedisConnectionFactory rConnectionFactory) {
//        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
//                .entryTtl(Duration.ofMinutes(2))
//                .disableCachingNullValues()
//                .serializeValuesWith(RedisSerializationContext.SerializationPair
//                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
//                .prefixCacheNameWith("product_");
//
//        return RedisCacheManager.builder(rConnectionFactory)
//                .cacheDefaults(config)
//                .transactionAware()
//                .build();
//    }

    @Bean
    public ReactiveRedisTemplate<String, ProductCacheDto> productCacheTemplate(
            ReactiveRedisConnectionFactory rConnectionFactory) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ParameterNamesModule()); // для поддержки @JsonCreator
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());

        Jackson2JsonRedisSerializer<ProductCacheDto> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, ProductCacheDto.class);

        RedisSerializationContext<String, ProductCacheDto> context = RedisSerializationContext
                .<String, ProductCacheDto>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();
        return new ReactiveRedisTemplate<>(rConnectionFactory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, List<ProductCacheDto>> productListCacheTemplate(
            ReactiveRedisConnectionFactory rConnectionFactory) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new ParameterNamesModule());
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new Jdk8Module());

        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, ProductCacheDto.class);
        Jackson2JsonRedisSerializer<List<ProductCacheDto>> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, type);

        RedisSerializationContext<String, List<ProductCacheDto>> context = RedisSerializationContext
                .<String, List<ProductCacheDto>>newSerializationContext(new StringRedisSerializer())
                .value(serializer)
                .build();

        return new ReactiveRedisTemplate<>(rConnectionFactory, context);
    }
}