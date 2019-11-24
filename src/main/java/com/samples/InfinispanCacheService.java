package com.samples;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Service
public class InfinispanCacheService implements CacheService {

    Logger logger = LoggerFactory.getLogger(InfinispanCacheService.class);
    @Autowired
    private DefaultCacheManagerWrapper cacheManagerWrapper;
    private DefaultCacheManager infiniCacheManager;

    private DefaultCacheManager initializeCacheManager(){
        if(infiniCacheManager==null){
            infiniCacheManager=cacheManagerWrapper.getCacheManager();
        }
        return infiniCacheManager;
    }
    @Override
    @PostConstruct
    public void start(){
        logger.info("Initializing...InifinispanCacheService ....");
        initializeCacheManager();
        for(String cacheName : infiniCacheManager.getCacheNames()){
            infiniCacheManager.startCache(cacheName);
        }
    }
    @Override
    public Object get(String cacheName, Object key) {
        return getCache(cacheName).get(key);
    }

    @Override
    public void put(String cacheName, Object key, Object value, long lifespan, TimeUnit unit) {
        Cache cache=getCache(cacheName);
        cache.put(key,value,lifespan,unit);
    }

    @Override
    public void put(String cacheName, Object key, Object value) {
        Cache cache=getCache(cacheName);
        cache.put(key,value);
    }

    private Cache<Object,Object> getCache(String cacheName) {
        Cache<Object,Object> cache;
        if(StringUtils.isEmpty(StringUtils.trimWhitespace(cacheName))){
            cache=infiniCacheManager.getCache();
        }else{
            cache=infiniCacheManager.getCache(cacheName,false);
        }
        return cache;
    }

    @Override
    public boolean cacheExists(String cacheName) {
        return infiniCacheManager.cacheExists(cacheName);
    }
}
