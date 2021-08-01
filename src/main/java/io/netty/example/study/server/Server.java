package io.netty.example.study.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.study.server.codec.OrderFrameDecoder;
import io.netty.example.study.server.codec.OrderFrameEncoder;
import io.netty.example.study.server.codec.OrderProtocolDecoder;
import io.netty.example.study.server.codec.OrderProtocolEncoder;
import io.netty.example.study.server.handler.OrderServerProcessHandler;
import io.netty.example.study.server.handler.ServerIdleCheckHandler;
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

        // 这个是共享的线程池
//        UnorderedThreadPoolEventExecutor business = new UnorderedThreadPoolEventExecutor(10, new DefaultThreadFactory("business"));


        // 当线程数是0的时候，默认是根据当前机器的cpu核数来计算
        // 我们发现，当我们使用这个作为运行任务的线程池的时候，和单线程是没有区别的
        // 这个的原因是因为ChannelOption.SINGLE_EVENTEXECUTOR_PER_GROUP默认是true的
        // 在这个情况下，我们的NioEventLoopGroup返回的线程不是线程池本身，而是线程池中的一个线程
        NioEventLoopGroup business = new NioEventLoopGroup(0, new DefaultThreadFactory("business"));

        // 设置codec和handler
        // handler 都是new的，每个
        serverBootstrap.childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                ChannelPipeline pipeline = nioSocketChannel.pipeline();

                pipeline.addLast("idleCheckHandler", new ServerIdleCheckHandler());
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


                // 我们的业务如果特别耗费性能的话，比如等待io，我们是否考虑使用多线程优化
                // 多线程优化的是什么呢
                // 为什么多线程可以优化呢？
                // 这个是因为一个EventLoop负责处置多个客户端连接，而且整个响应是异步的
                // 这样我们使用多线程可以是单个EventLoop的处置更快
                pipeline.addLast(business,new OrderServerProcessHandler());

                pipeline.addLast(metricHandler);
                pipeline.addLast(new LoggingHandler(LogLevel.INFO));
            }
        });

        ChannelFuture channelFuture = serverBootstrap.bind(8090).sync();

        channelFuture.channel().closeFuture().get();


    }

}
