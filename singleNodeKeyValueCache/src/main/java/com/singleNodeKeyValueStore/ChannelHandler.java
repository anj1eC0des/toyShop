package com.singleNodeKeyValueStore;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;


public class ChannelHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger log= LoggerFactory.getLogger(ChannelHandler.class);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request){
        HttpMethod method=request.method();
        if(GET.equals(method)) handleGet(ctx,request);
        if(POST.equals(method)) handlePost(ctx,request);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause){
        log.error("Exception caught in channel handler: {}", cause.getMessage(), cause);
        cause.printStackTrace();  // Also print to console
        ctx.close();
    }

    void handleGet(ChannelHandlerContext ctx,FullHttpRequest request){
        log.info("Get request logged {}",request.content());
        ObjectMapper objectMapper=new ObjectMapper();
        try{
            String jsonString = request.content().toString(StandardCharsets.UTF_8);
            log.info("Received raw content: [{}]", jsonString);
            log.info("Content length: {}", jsonString.length());
            List<String> list= objectMapper.readValue(jsonString,
                    new TypeReference<ArrayList<String>>(){});
            Map<String,String> map=new HashMap<>();
            for(String x:list) map.put(x,Cache.read(x));
            String message=objectMapper.writeValueAsString(map);
            FullHttpResponse response=new DefaultFullHttpResponse(request.protocolVersion(),
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(message.getBytes()));
            writeResponse(ctx,request,response);
        }catch (Exception e) {
            log.error("Error in handlePost", e);  // This logs the full exception with stack trace
            e.printStackTrace();  // Also print to console to be sure
            sendErrorMessage(ctx, request);
        }

    }

    void handlePost(ChannelHandlerContext ctx,FullHttpRequest request){
        try{
            ObjectMapper objectMapper=new ObjectMapper();
            String jsonString = request.content().toString(StandardCharsets.UTF_8);
            log.info("Received raw content: [{}]", jsonString);
            log.info("Content length: {}", jsonString.length());
            Map<String,String> map= objectMapper.readValue(jsonString,
                    new TypeReference<HashMap<String,String>>(){});
            log.info("Post request logged {}",map);
            for(Map.Entry<String,String> entry:map.entrySet()){
                Cache.write(entry.getKey(), entry.getValue());
            }
            String message=objectMapper.writeValueAsString(map);
            FullHttpResponse response=new DefaultFullHttpResponse(request.protocolVersion(),
                    HttpResponseStatus.OK,
                    Unpooled.wrappedBuffer(message.getBytes()));
            writeResponse(ctx,request,response);
        } catch (Exception e) {
            log.error("Error in handlePost", e);  // This logs the full exception with stack trace
            e.printStackTrace();  // Also print to console to be sure
            sendErrorMessage(ctx, request);
        }

    }

    void writeResponse(ChannelHandlerContext ctx,
                       FullHttpRequest request,
                       FullHttpResponse response){
        log.info("Entered write response.");
        boolean keepAlive= HttpUtil.isKeepAlive(request);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,"application/json")
                .set(HttpHeaderNames.CONTENT_LENGTH,response.content().readableBytes());
        ChannelFuture f=ctx.writeAndFlush(response);
        f.addListener(future -> log.info("Write status: {}",future.isSuccess()));
        if(!keepAlive) f.addListener(ChannelFutureListener.CLOSE);
    }

    void sendErrorMessage(ChannelHandlerContext ctx,FullHttpRequest request){
        log.info("Entered error response.");
        FullHttpResponse response=new DefaultFullHttpResponse(request.protocolVersion(),
                HttpResponseStatus.BAD_REQUEST);
        boolean keepAlive= HttpUtil.isKeepAlive(request);
        ChannelFuture f=ctx.writeAndFlush(response);
        f.addListener(future -> log.info("Write status: {}",future.isSuccess()));
        if(!keepAlive) f.addListener(ChannelFutureListener.CLOSE);
    }
}
