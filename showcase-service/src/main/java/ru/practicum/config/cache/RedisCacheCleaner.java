package ru.practicum.config.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisCacheCleaner implements ApplicationListener<ApplicationReadyEvent> {

    private final ReactiveRedisConnectionFactory redisConnectionFactory;

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        redisConnectionFactory.getReactiveConnection()
                .serverCommands()
                .flushAll()
                .subscribe();
    }
}