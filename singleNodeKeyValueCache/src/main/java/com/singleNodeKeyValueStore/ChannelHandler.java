package com.singleNodeKeyValueStore;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.DataInput;
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
        log.warn(Arrays.toString(cause.getStackTrace()));
        ctx.close();
    }

    void handleGet(ChannelHandlerContext ctx,FullHttpRequest request){
        log.info("Get request logged {}",request.content());
        ObjectMapper objectMapper=new ObjectMapper();
        List<String> list= objectMapper.readValue((DataInput) request.content(),
                new TypeReference<ArrayList<String>>(){});
        Map<String,String> map=new HashMap<>();
        for(String x:list) map.put(x,Cache.read(x));
        String message=objectMapper.writeValueAsString(map);
        FullHttpResponse response=new DefaultFullHttpResponse(request.protocolVersion(),
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(message.getBytes()));
        writeResponse(ctx,request,response);
    }

    void handlePost(ChannelHandlerContext ctx,FullHttpRequest request){
        ObjectMapper objectMapper=new ObjectMapper();
        Map<String,String> map= objectMapper.readValue((DataInput) request.content(),
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
    }

    void writeResponse(ChannelHandlerContext ctx,
                       FullHttpRequest request,
                       FullHttpResponse response){
        boolean keepAlive= HttpUtil.isKeepAlive(request);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE,"application/json")
                .set(HttpHeaderNames.CONTENT_LENGTH,response.content().readableBytes());
        ChannelFuture f=ctx.writeAndFlush(response);
        f.addListener(future -> log.info("Write status: {}",future.isSuccess()));
        if(!keepAlive) f.addListener(ChannelFutureListener.CLOSE);
    }
}
