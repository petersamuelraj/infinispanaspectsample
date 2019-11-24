package com.samples;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

public interface CacheService {
    void start();

    Object get(String cacheName, Object key);

    void put(String cacheName, Object key, Object value, long lifespan, TimeUnit unit);

    void put(String cacheName, Object key, Object value);

    boolean cacheExists(String cacheName);
}
