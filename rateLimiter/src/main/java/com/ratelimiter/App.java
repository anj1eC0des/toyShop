package com.ratelimiter;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

public class App {
    public void run()throws Exception{
        EventLoopGroup boss=new NioEventLoopGroup();
        EventLoopGroup worker= new NioEventLoopGroup();
        try{
            ServerBootstrap server=new ServerBootstrap();
            server.group(boss,worker)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .localAddress(new InetSocketAddress(8080))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            ChannelPipeline p=socketChannel.pipeline();
                            p.addLast(new HttpServerCodec());
//                            p.addLast(new HttpContentCompressor((CompressionOptions[]) null));
                            p.addLast(new HttpServerExpectContinueHandler());
                            p.addLast(new RateLimiterHandler());
                        }
                    });
            ChannelFuture f= server.bind().sync();

            f.channel().closeFuture().sync();

        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void main( String[] args ) throws Exception{
        new App().run();
    }
}
