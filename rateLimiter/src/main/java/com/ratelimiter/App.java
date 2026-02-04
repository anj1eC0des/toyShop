package com.ratelimiter;
import com.sun.net.httpserver.* ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class App {
    private static final ConcurrentHashMap<String, AtomicReference<Bucket>> tokenBucket=
            new ConcurrentHashMap<>();
    private static final Logger log= LoggerFactory.getLogger(App.class);
    final static int MAX_TOKENS=15;
    final static int BASE_TOKENS=10;
    final static long REFILL_DURATION = 10_000_000_000L;
    record Bucket(int tokens,long lastRefill){}
    public static void main( String[] args ) {
        try{
            InetSocketAddress address= new InetSocketAddress(8080);
            if(address.isUnresolved()){
                log.warn("unresolved address");
            }
            else{
                HttpServer server=HttpServer.create(
                       address,10);
                server.setExecutor(Executors.newFixedThreadPool(10));
                server.createContext("/",(exchange)->{
                    long now=System.nanoTime();
                    String ip= exchange.getRemoteAddress().getAddress().getHostAddress();
                    AtomicReference<Bucket> bucket= tokenBucket.computeIfAbsent(ip,
                            (k)-> new AtomicReference<>(new Bucket(MAX_TOKENS,now)));
                    Bucket updatedBucket= bucket.updateAndGet(oldBucket->{
                       int oldTokens = oldBucket.tokens();
                       long lastRefill=oldBucket.lastRefill();
                       if(oldTokens<=0 && now-lastRefill>=REFILL_DURATION){
                           oldTokens=BASE_TOKENS;
                           lastRefill=now;
                       }
                       if(oldTokens<=0) return oldBucket;
                       return new Bucket(oldTokens-1,lastRefill);
                    });
                    if(updatedBucket.tokens()<=0) {
                        String badResponse = "Too many hits!";
                        exchange.sendResponseHeaders(429, badResponse.length());
                        exchange.getResponseBody().write(badResponse.getBytes());
                        exchange.close();
                    }else{
                        String response="HELLO!";
                        exchange.sendResponseHeaders(200,response.length());
                        exchange.getResponseBody().write(response.getBytes());
                        exchange.close();
                    }

                });
                server.start();
                Runtime.getRuntime().addShutdownHook(new Thread(()->{
                    log.info("Gracefully shutting down.");
                    server.stop(1);
                    log.info("Server stopped!");
                }));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
