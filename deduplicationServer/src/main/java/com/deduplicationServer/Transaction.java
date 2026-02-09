package com.deduplicationServer;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;


public class Transaction{
    public enum Status{
        PROCESSING,
        FAILED,
        SUCCEEDED
    }
    private volatile Status status;
    private final LocalDateTime timestamp;
    private final String hashedRequest;
    private final CompletableFuture<Data> response;
    private static final Logger log= LoggerFactory.getLogger(Transaction.class);
    public Transaction(String hashedRequest, HttpRequest request){
        this.status=Status.PROCESSING;
        this.timestamp= java.time.LocalDateTime.now();
        this.hashedRequest=hashedRequest;
        log.info("New Transaction Created for {}",request.headers().get("IdempotencyKey"));
        this.response= processTransaction(request);
        this.response.thenAccept((response)-> this.status=Status.SUCCEEDED);
        this.response.exceptionally(exp->{
            this.status=Status.FAILED;
            return new Data(request.protocolVersion(),
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    "Failed message".getBytes(),
                    new DefaultHttpHeaders()
                            .set(CONTENT_TYPE,TEXT_PLAIN));
        });
    }
    CompletableFuture<Data> getResponse(){
        return this.response;
    }
    private CompletableFuture<Data> processTransaction(HttpRequest request){
        return CompletableFuture.supplyAsync(()->{
            String responseMessage="Response";
            return new Data(request.protocolVersion(),
                    HttpResponseStatus.OK,
                    responseMessage.getBytes(),
                    new DefaultHttpHeaders()
                            .set(CONTENT_TYPE,TEXT_PLAIN));
        });
    }
    public Status getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getHashedRequest() {
        return hashedRequest;
    }
}


