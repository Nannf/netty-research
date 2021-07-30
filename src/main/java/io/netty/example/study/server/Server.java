package io.netty.example.study.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.study.server.codec.OrderFrameDecoder;
import io.netty.example.study.server.codec.OrderFrameEncoder;
import io.netty.example.study.server.codec.OrderProtocolDecoder;
import io.netty.example.study.server.codec.OrderProtocolEncoder;
import io.netty.example.study.server.handler.OrderServerProcessHandler;
import io.netty.example.study.server.metric.MetricHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

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

        // 设置线程池的名称
        NioEventLoopGroup boss = new NioEventLoopGroup(1,new DefaultThreadFactory("boss"));
        NioEventLoopGroup worker = new NioEventLoopGroup(1,new DefaultThreadFactory("worker"));
        // 设置reactor模式
        serverBootstrap.group(boss,worker);

        // TCP是用来实际的传数据的这个参数，所以这个是childOption,即SocketChannel
        serverBootstrap.childOption(NioChannelOption.TCP_NODELAY,true);
        // 这个是最大的等待连接数，这个是ServerSocketChannel的工作
        // 不代表一个真正的连接实体
        serverBootstrap.option(NioChannelOption.SO_BACKLOG,1024);

        // 设置日志
        serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));

        // 因为需要计算连接数量，所以这个是每个客户端连接都公用的handler
        MetricHandler metricHandler = new MetricHandler();

        // 设置codec和handler
        // handler 都是new的，每个
        serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                ChannelPipeline pipeline = nioSocketChannel.pipeline();
                // 这个地方是有顺序的,顺序错了就会失败
                // 核心是4+1
                // 最开始肯定是解码，解码第一个是粘包和半包问题的处理
                // Frame -> Protocol -> Handler(业务处理)
                // 处理完成之后，第一步要把ResponseMessage->ByteBuf Protocol
                // 这个pipeline请求和发送是反着的，所以处理粘包和半包问题的FrameEncoder
                // 要在ProtocolEncoder之后
                pipeline.addLast("frameDecoder",new OrderFrameDecoder());
                // 给handler新增名称
                pipeline.addLast("frameEncoder",new OrderFrameEncoder());

                // 每个handler都是new的新对象，不是单例模式
                pipeline.addLast(new OrderProtocolDecoder());
                pipeline.addLast(new OrderProtocolEncoder());
                pipeline.addLast(new OrderServerProcessHandler());

                pipeline.addLast(metricHandler);
                pipeline.addLast(new LoggingHandler(LogLevel.INFO));
            }
        });

        ChannelFuture channelFuture = serverBootstrap.bind(8090).sync();

        channelFuture.channel().closeFuture().get();


    }

}
