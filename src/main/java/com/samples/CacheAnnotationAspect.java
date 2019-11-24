package com.samples;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.infinispan.jcache.annotation.DefaultCacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.cache.annotation.CacheKey;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.springframework.util.StringUtils.isEmpty;


@Aspect
@Slf4j
@Configuration
public class CacheAnnotationAspect {
    Logger logger = LoggerFactory.getLogger(CacheAnnotationAspect.class);

    @Autowired
    InfinispanCacheService cacheService;

    @Around("@annotation(com.samples.CacheResult)")
    public Object cacheResult(ProceedingJoinPoint joinPoint)throws Throwable{
        logger.info("Cache Operation :: CacheResult annotation advice invoked...");
        CacheResult cacheResult=(CacheResult) getAnnotation(joinPoint,CacheResult.class);
        CacheConfig cacheConfig= CacheConfig.from(cacheResult);
        Object resultFromCache=getFromCache(joinPoint,cacheConfig);
        if(resultFromCache!= null){
            logger.info("Returning from Cache");
            return resultFromCache;
        }
        Object result=joinPoint.proceed(joinPoint.getArgs());
        storeInCache(result,joinPoint,cacheConfig);
        return result;
    }

    private void storeInCache(Object result, ProceedingJoinPoint joinPoint, CacheConfig cacheConfig) {
        if(result==null){
            logger.info("Cache op :: null values not cached");
            return;
        }
        CacheService cacheService=getCacheService();
        if(cacheService==null){
            logger.info("Cache op :: CacheGet Failed : No CacheService available for use..");
        }
        DefaultCacheKey defaultCacheKey=getKey(joinPoint,cacheConfig);
        String cacheName=getCacheName(cacheConfig.getCacheName(),joinPoint);
        long lifeSpan=cacheConfig.getLifespan();
        if(lifeSpan== 60){
            cacheService.put(cacheName,defaultCacheKey,result);
        }else{
            cacheService.put(cacheName,defaultCacheKey,result,lifeSpan,cacheConfig.getUnit());
        }
        logger.info("Cache Op :: Result cached :: {} ",cacheConfig);
    }

    private DefaultCacheKey getKey(ProceedingJoinPoint joinPoint, CacheConfig cacheConfig) {
        List<Object> keys=new ArrayList<>();
        Object target=joinPoint.getTarget();
        MethodSignature methodSignature=MethodSignature.class.cast(joinPoint.getSignature());
        Method method=methodSignature.getMethod();
        Annotation[][] parameterAnnotations=method.getParameterAnnotations();
        if(isEmpty(StringUtils.trimWhitespace(cacheConfig.getKeyPrefix()))){
            keys.add(target.getClass().getName());
            keys.add(method.getName());
        }else{
            keys.add(cacheConfig.getKeyPrefix());
        }
        if(isCacheKeySpecified(parameterAnnotations)){
            keys.addAll(getCacheKeys(joinPoint,parameterAnnotations));
        }else{
            keys.addAll(Arrays.asList(joinPoint.getArgs()));
        }
        return new DefaultCacheKey(keys.toArray());
    }

    private Collection<?> getCacheKeys(ProceedingJoinPoint joinPoint, Annotation[][] parameterAnnotations) {
        Object[] args=joinPoint.getArgs();
        List<Object> result=new ArrayList<>();
        int i=0;
        for(Annotation[] annotations: parameterAnnotations){
            for(Annotation annotation: annotations){
                if(annotation instanceof CacheKey){
                    result.add(args[i]);
                    break;
                }
            }
            i++;
        }
        return result;
    }

    private boolean isCacheKeySpecified(Annotation[][] parameterAnnotations) {
        for(Annotation[] annotations:parameterAnnotations){
            for(Annotation annotation:annotations){
                if(annotation instanceof CacheKey) {
                    return true;
                }
            }
        }
        return false;
    }

    private Object getFromCache(ProceedingJoinPoint joinPoint, CacheConfig cacheConfig) {
        CacheService cacheService = getCacheService();
        if (cacheService == null) {
            logger.info("Cache op :: CacheGet Failed : No CacheService available for use..");
        }
        String cacheName=getCacheName(cacheConfig.getCacheName(),joinPoint);
        DefaultCacheKey defaultCacheKey=getKey(joinPoint,cacheConfig);

        return cacheService.get(cacheName,defaultCacheKey);
    }

    private String getCacheName(String cacheName, ProceedingJoinPoint joinPoint) {
        boolean nameNotDefined=isEmpty(StringUtils.trimWhitespace(cacheName));
        if(nameNotDefined){
            logger.error("Cache op :: Cache Name not defined");
        }else{
            CacheService cacheService=getCacheService();
            if(!cacheService.cacheExists(cacheName)){
                throw new RuntimeException("Cache with the name "+ cacheName+" does not exists");
            }
        }
        return cacheName;
    }

    private CacheService getCacheService() {
        return cacheService;
    }

    private Annotation getAnnotation(ProceedingJoinPoint joinPoint, Class type) {
        MethodSignature methodSignature=MethodSignature.class.cast(joinPoint.getSignature());
        Method method=methodSignature.getMethod();
        return method.getAnnotation(type);
    }

}