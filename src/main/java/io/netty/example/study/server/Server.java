package io.netty.example.study.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.study.server.codec.OrderFrameDecoder;
import io.netty.example.study.server.codec.OrderFrameEncoder;
import io.netty.example.study.server.codec.OrderProtocolDecoder;
import io.netty.example.study.server.codec.OrderProtocolEncoder;
import io.netty.example.study.server.handler.OrderServerProcessHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.ExecutionException;

/**
 * @author Akmd Nannf
 * @version v1.0
 * @Description 这个是服务器的入口类，整合codec和handler当中的实现
 * @date 2021/7/28 21:14
 */
public class Server {


    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 这个是入口
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        // 设置i/o 模式。 这个是反射+工厂获取的
        serverBootstrap.channel(NioServerSocketChannel.class);

        // 设置reactor模式
        serverBootstrap.group(new NioEventLoopGroup());

        // 设置日志
        serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));

        // 设置codec和handler

        serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                ChannelPipeline pipeline = nioSocketChannel.pipeline();
                // 这个地方是有顺序的,顺序错了就会失败
                pipeline.addLast(new OrderFrameDecoder());
                pipeline.addLast(new OrderFrameEncoder());
                pipeline.addLast(new OrderProtocolDecoder());
                pipeline.addLast(new OrderProtocolEncoder());
                pipeline.addLast(new OrderServerProcessHandler());

                pipeline.addLast(new LoggingHandler(LogLevel.INFO));
            }
        });

        ChannelFuture channelFuture = serverBootstrap.bind(8090).sync();

        channelFuture.channel().closeFuture().get();


    }

}
