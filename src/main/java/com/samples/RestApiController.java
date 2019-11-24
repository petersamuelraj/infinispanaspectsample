package com.samples;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class RestApiController {

    @CacheResult(cacheName= "attributeset-cache", lifespan=10,unit = TimeUnit.MINUTES)
    @GetMapping("/eavattributeset")
    public List<String> fetchAllAttributes() {
        return Arrays.asList("car", "bike");
    }
}
