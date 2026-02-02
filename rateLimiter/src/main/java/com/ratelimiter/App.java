package com.ratelimiter;
import com.sun.net.httpserver.* ;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Hello world!
 *
 */
public class App {
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
                    String response="Hello world!";
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
