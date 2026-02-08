package com.ratelimiter;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;

import static com.ratelimiter.RateLimiterFunc.responseHandler;
import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;

public class RateLimiterHandler extends SimpleChannelInboundHandler<HttpRequest> {
    private static final Logger log= LoggerFactory.getLogger(RateLimiterHandler.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpRequest request){
        log.info("{}",request);
        SocketAddress socketAddress=ctx.channel().remoteAddress();
        boolean keepAlive= HttpUtil.isKeepAlive(request);
        if(socketAddress instanceof InetSocketAddress inetSocketAddress){
            String ip=inetSocketAddress.getAddress().getHostAddress();
            FullHttpResponse response= responseHandler(request,ip);
            if(keepAlive) response.headers().set(CONNECTION,KEEP_ALIVE);
            else response.headers().set(CONNECTION,CLOSE);
            log.info("{}",keepAlive);
            log.info("headers : {}",response.headers());
            log.info("Readable bytes: {}", response.content().readableBytes());
            ChannelFuture f=ctx.writeAndFlush(response);
            f.addListener(future -> log.info("Write status: {}",future.isSuccess()));
            if(!keepAlive) f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        log.warn(Arrays.toString(cause.getStackTrace()));
        ctx.close();

    }

//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx){
//        ctx.flush();
//    }
}
