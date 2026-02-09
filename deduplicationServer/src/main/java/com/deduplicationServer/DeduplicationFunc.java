package com.deduplicationServer;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;

public class DeduplicationFunc {
    private static final Map<String, Transaction> cache=Cache.cache;
    private static final Logger log= LoggerFactory.getLogger(DeduplicationFunc.class);
    static CompletableFuture<Data> responseHandler(HttpRequest request, String key, String hashedRequest){
        Transaction t=cache.get(key);
        if(t!=null && !t.getHashedRequest().equals(hashedRequest)){
            log.info("Invalid Request");
            return CompletableFuture.completedFuture( new Data(request.protocolVersion(),
                    HttpResponseStatus.UNPROCESSABLE_ENTITY,
                    "Request hashes dont match!".getBytes(),
                    new DefaultHttpHeaders()
                            .set(CONTENT_TYPE, TEXT_PLAIN)));
        }
        return cache.computeIfAbsent(key,
                (k)->{
            log.info("NEW KEY.");
            return new Transaction(hashedRequest,request);
        })
                .getResponse();
    }
}
