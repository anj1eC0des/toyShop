package com.deduplicationServer;

import java.util.concurrent.ConcurrentHashMap;

public class Cache {
    public static ConcurrentHashMap<String,
            Transaction> cache= new ConcurrentHashMap<>();

}

