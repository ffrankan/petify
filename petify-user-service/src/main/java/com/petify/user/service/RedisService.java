package com.petify.user.service;

public interface RedisService {
    
    void set(String key, Object value, int expireSeconds);
    
    <T> T get(String key, Class<T> type);
    
    void delete(String key);
    
    void deleteByPattern(String pattern);
    
    boolean hasKey(String key);
}