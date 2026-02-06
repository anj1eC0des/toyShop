package com.ratelimiter;
import com.sun.net.httpserver.* ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class App {
    private static final ConcurrentHashMap<String, Bucket> tokenBucket=
            new ConcurrentHashMap<>();
    private static final Logger log= LoggerFactory.getLogger(App.class);
    final static int MAX_TOKENS=1000;
    final static int BASE_TOKENS=100;
    final static long REFILL_DURATION = 10_000_000L;
    record Bucket(long tokens, long lastRefill){}
    public static void main( String[] args ) {
        try{
            InetSocketAddress address= new InetSocketAddress(8080);
            if(address.isUnresolved()){
                log.warn("unresolved address");
            }
            else{
                HttpServer server=HttpServer.create(
                       address,10);
                int cores = Runtime.getRuntime().availableProcessors();
                server.setExecutor(Executors.newFixedThreadPool(cores * 2));
                server.createContext("/",(exchange)->{
                    AtomicBoolean notAllowed = new AtomicBoolean(true);
                    String ip= exchange.getRemoteAddress().getAddress().getHostAddress();
                    tokenBucket.compute(ip,(k,oldBucket)->{
                        if(oldBucket==null) {
                            notAllowed.set(false);
                            return new Bucket(BASE_TOKENS-1,System.nanoTime());
                        }
                        long now=System.nanoTime();
                        long lastRefill=oldBucket.lastRefill();
                        long elapsed=now-lastRefill;
                        long oldTokens = oldBucket.tokens();
                        long newTokens=Math.min(oldTokens+(elapsed/REFILL_DURATION),MAX_TOKENS);
                        if(newTokens<=0) return new Bucket(0,lastRefill);
                        notAllowed.set(false);
                        if(newTokens!=oldTokens) return new Bucket(newTokens-1,now);
                        return new Bucket(oldTokens-1,lastRefill);
                    });
                    if(notAllowed.get()) {
                        String badResponse = "Too many hits!";
                        exchange.sendResponseHeaders(429, badResponse.getBytes().length);
                        exchange.getResponseBody().write(badResponse.getBytes());
                        exchange.close();
                    }else{
                        String response="HELLO!";
                        exchange.sendResponseHeaders(200,response.getBytes().length);
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
