package com.deduplicationServer;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpHeaderValues.*;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;

public class ChannelHandler extends SimpleChannelInboundHandler <FullHttpRequest>{
    private static final Logger log= LoggerFactory.getLogger(ChannelHandler.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request){
        String key=request.headers().get("IdempotencyKey");
        boolean keepAlive= HttpUtil.isKeepAlive(request);
        if(key!=null){
            String hashedRequest= hashRequest(request);
            CompletableFuture<Data> response= DeduplicationFunc
                    .responseHandler(request,key,hashedRequest);
            response.thenAccept((data)->{
                FullHttpResponse res=new DefaultFullHttpResponse(data.version(),
                        data.status(),
                        Unpooled.wrappedBuffer(data.responseMessage()));
                res.headers().set(data.headers());
                res.headers().setInt(CONTENT_LENGTH,res.content().readableBytes());
                if(keepAlive) res.headers().set(CONNECTION, KEEP_ALIVE);
                else res.headers().set(CONNECTION,CLOSE);
                ChannelFuture f=ctx.writeAndFlush(res);
                if(!keepAlive) f.addListener(ChannelFutureListener.CLOSE);
            });
        }else{
            FullHttpResponse failedResponse=new DefaultFullHttpResponse(request.protocolVersion(),
                    HttpResponseStatus.BAD_REQUEST,
                    Unpooled.wrappedBuffer("No Idempotency key".getBytes()));
            failedResponse.headers()
                    .set(CONTENT_TYPE,TEXT_PLAIN)
                    .setInt(CONTENT_LENGTH,failedResponse.content().readableBytes());
            if(keepAlive) failedResponse.headers().set(CONNECTION,KEEP_ALIVE);
            else failedResponse.headers().set(CONNECTION,CLOSE);
            ChannelFuture f=ctx.writeAndFlush(failedResponse);
            if(!keepAlive) f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        log.warn(Arrays.toString(cause.getStackTrace()));
        ctx.close();
    }

    private static String hashRequest(FullHttpRequest request){
        byte[] body= new byte[request.content().readableBytes()];
        request.content().getBytes(0,body);
        try{
            MessageDigest md= MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(body);
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
