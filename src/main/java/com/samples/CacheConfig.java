package com.samples;

import java.util.concurrent.TimeUnit;

public class CacheConfig {
    private String cacheName;
    private long lifespan;
    private TimeUnit unit;
    private String keyPrefix;

    public CacheConfig(String cacheName, long lifespan, TimeUnit unit, String keyPrefix) {
        this.cacheName = cacheName;
        this.lifespan = lifespan;
        this.unit = unit;
        this.keyPrefix = keyPrefix;
    }

    public static CacheConfig from(CacheResult cacheResult) {
        return new CacheConfig(cacheResult.cacheName(), cacheResult.lifespan(), cacheResult.unit(), "Test");
    }

    public String getCacheName() {
        return cacheName;
    }

    public long getLifespan() {
        return lifespan;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }
}
