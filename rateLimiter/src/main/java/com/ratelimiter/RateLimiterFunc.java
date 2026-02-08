package com.ratelimiter;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;

public class RateLimiterFunc {
    private static final ConcurrentHashMap<String, Bucket> tokenBucket=
            new ConcurrentHashMap<>();
    private static final Logger log= LoggerFactory.getLogger(RateLimiterFunc.class);
    final static int MAX_TOKENS=1000;
    final static int BASE_TOKENS=100;
    final static long REFILL_DURATION = 10_000_000L;
    record Bucket(long tokens,long lastRefill,boolean notAllowed){}

    static FullHttpResponse responseHandler(HttpRequest request, String ip){
        long now=System.nanoTime();
        boolean notAllowed=tokenBucket.compute(ip,(k,oldBucket)->{
            if(oldBucket==null) {
                return new Bucket(BASE_TOKENS-1,now,false);
            }
            long lastRefill=oldBucket.lastRefill();
            long elapsed=now-lastRefill;
            long oldTokens = oldBucket.tokens();
            long newTokens=Math.min(oldTokens+(elapsed/REFILL_DURATION),MAX_TOKENS);
            if(newTokens<=0) return new Bucket(0,lastRefill,true);
            if(newTokens!=oldTokens) return new Bucket(newTokens-1,now,false);
            return new Bucket(oldTokens-1,lastRefill,false);
        }).notAllowed();
        FullHttpResponse response;
        if(notAllowed) {
            log.info("Rate limited {}",ip);
            String badResponse = "Too many hits!";
            response=new DefaultFullHttpResponse(request.protocolVersion(),
                    HttpResponseStatus.TOO_MANY_REQUESTS,
                    Unpooled.wrappedBuffer(badResponse.getBytes()));
            response.headers()
                    .set(CONTENT_TYPE,TEXT_PLAIN)
                    .set(RETRY_AFTER,"1")
                    .setInt(CONTENT_LENGTH,response.content().readableBytes());
        }else{
            String goodResponse="HELLO!";
            log.info("Touched {}",ip);
            response=new DefaultFullHttpResponse(request.protocolVersion(),
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(goodResponse.getBytes()));
            response.headers()
                    .set(CONTENT_TYPE,TEXT_PLAIN)
                    .setInt(CONTENT_LENGTH,response.content().readableBytes());
        }
        return response;
    }
}
