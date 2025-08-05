package com.stu.product_service.entity;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
public class TokenBlacklistCache {
    private static final String PREFIX = "product:blacklist:";
    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void add(String jti, long ttlSeconds) {
        String key = PREFIX + jti;
        redisTemplate.opsForValue().set(key, "1", ttlSeconds, TimeUnit.SECONDS);
    }

    public boolean contains(String jti) {
        String key = PREFIX + jti;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}