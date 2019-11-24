package com.samples;

import org.infinispan.manager.DefaultCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

@Component
public class DefaultCacheManagerWrapper {

    Logger logger = LoggerFactory.getLogger(DefaultCacheManagerWrapper.class);
    private String fileName="classpath:infinispan.xml";
    private DefaultCacheManager infiniCacheManager;

    @PostConstruct
    public void start(){
        logger.info(" Received File Name :: {} ",fileName);
        try{
            URL fileUrl=new URL(fileName);
            URLConnection urlConnection=fileUrl.openConnection();
            InputStream inputStream=urlConnection.getInputStream();
            infiniCacheManager=new DefaultCacheManager(inputStream);
            infiniCacheManager.start();
            logger.info("Cache Manager Initialized....");
        } catch(IOException mue){
            logger.error("Error creating file url ",mue.getMessage());
        }
    }
    public void stop() { infiniCacheManager.stop();}
    public DefaultCacheManager getCacheManager(){
        return infiniCacheManager;
    }
}