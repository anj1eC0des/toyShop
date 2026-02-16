package com.singleNodeKeyValueStore;

import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {
    private static final ConcurrentHashMap<String,String> cache=new ConcurrentHashMap<>();
    public static String read(String key) {
        String value = cache.get(key);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(Map.of(key,value));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static void write(String key, String value){
        cache.put(key,value);
    }
}
