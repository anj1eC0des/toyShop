package com.ratelimiter;
import com.sun.net.httpserver.* ;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class App {
    private static final ConcurrentHashMap<String,Integer> tokenBucket=new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String,Long> lastHit=new ConcurrentHashMap<>();
    final static long TARGET_NANOS = 10_000_000_000L;
    public static void main( String[] args ) {
        try{
            InetSocketAddress address= new InetSocketAddress(8080);
            if(address.isUnresolved()){
                System.out.println("unresolved address");
            }
            else{
                HttpServer server=HttpServer.create(
                       address,10);
                server.setExecutor(Executors.newFixedThreadPool(10));
                server.createContext("/",(exchange)->{
                    String ip= exchange.getRemoteAddress().getAddress().getHostAddress();
                    if(!tokenBucket.containsKey(ip)) {
                        tokenBucket.put(ip,15);
                        lastHit.put(ip,System.nanoTime()-TARGET_NANOS-1);
                    }
                    if(tokenBucket.get(ip)<=0 ) {
                        if(System.nanoTime()-lastHit.get(ip)>TARGET_NANOS){
                            tokenBucket.put(ip, 10);
                        }
                        else {
                            String badResponse= "Too many hits!";
                            exchange.sendResponseHeaders(429,badResponse.length());
                            exchange.getResponseBody().write(badResponse.getBytes());
                            exchange.close();
                            return;
                        }
                    }
                    lastHit.put(ip,System.nanoTime());
                    tokenBucket.put(ip,tokenBucket.get(ip)-1);
                    String response="HELLO!";
                    exchange.sendResponseHeaders(200,response.length());
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.close();

                });
                server.start();

                Runtime.getRuntime().addShutdownHook(new Thread(()->{
                    System.out.println("gracefully shutting down.");
                    server.stop(1);
                    System.out.println("server stopped!");
                }));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
