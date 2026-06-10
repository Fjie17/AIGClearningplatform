package com.example.learningplatform.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class EmailCodeCache {

    private static final String PREFIX = "email_code:";
    private static final long EXPIRATION_MINUTES = 5;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void saveCode(String email, String code) {
        String key = PREFIX + email;
        redisTemplate.opsForValue().set(key, code, EXPIRATION_MINUTES, TimeUnit.MINUTES);
    }

    public boolean verifyCode(String email, String code) {
        String key = PREFIX + email;
        Object cachedCode = redisTemplate.opsForValue().get(key);
        if (cachedCode == null) {
            return false;
        }
        return cachedCode.equals(code);
    }

    public void removeCode(String email) {
        String key = PREFIX + email;
        redisTemplate.delete(key);
    }
}