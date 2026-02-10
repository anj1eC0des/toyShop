package com.deduplicationServer;

import io.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DeduplicationFunc {
    private static final Map<String, Transaction> cache=Cache.cache;
    private static final Logger log= LoggerFactory.getLogger(DeduplicationFunc.class);
    static CompletableFuture<Data> responseHandler(HttpRequest request, String key, String hashedRequest){
        return cache.computeIfAbsent(key,
                (k)->{
            log.info("NEW KEY.");
            return new Transaction(hashedRequest,request);
        })
                .getResponse(hashedRequest);
    }
}
