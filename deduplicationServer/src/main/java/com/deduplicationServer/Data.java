package com.deduplicationServer;


import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public record Data(HttpVersion version,
                   HttpResponseStatus status,
                   byte[] responseMessage,
                   HttpHeaders headers) {
}
