//package ru.practicum.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
//import org.springframework.data.redis.core.ReactiveRedisTemplate;
//import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.RedisSerializationContext;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//@Configuration
//public class RedisConfig {
//
//    @Bean
//    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
//        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
//
//        RedisSerializationContext.RedisSerializationContextBuilder<String, Object> builder =
//                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
//
//        RedisSerializationContext<String, Object> context =
//                builder.value(serializer).build();
//
//        return new ReactiveRedisTemplate<>(factory, context);
//    }
//}