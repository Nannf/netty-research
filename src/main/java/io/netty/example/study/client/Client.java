package io.netty.example.study.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.study.client.codec.OrderFrameDecoder;
import io.netty.example.study.client.codec.OrderFrameEncoder;
import io.netty.example.study.client.codec.OrderProtocolDecoder;
import io.netty.example.study.client.codec.OrderProtocolEncoder;
import io.netty.example.study.common.Operation;
import io.netty.example.study.common.RequestMessage;
import io.netty.example.study.common.order.OrderOperation;
import io.netty.example.study.util.IdUtil;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.ExecutionException;

/**
 * @author Akmd Nannf
 * @version v1.0
 * @Description 客户端
 * @date 2021/7/28 21:25
 */
public class Client {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 客户端没有server
        Bootstrap bootstrap = new Bootstrap();

        // 设置i/o 模式。 这个是反射+工厂获取的
        bootstrap.channel(NioSocketChannel.class);

        // 设置reactor模式
        bootstrap.group(new NioEventLoopGroup());

        // 设置日志，客户端只能设置一次，后设置的handler会替代之前设置的，所以这个不需要了
//        bootstrap.handler(new LoggingHandler(LogLevel.INFO));


        // 客户端没有child
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                ChannelPipeline pipeline = nioSocketChannel.pipeline();
                // 这个地方是有顺序的,顺序错了就会失败
                // 类名一样但是包不一样
                pipeline.addLast(new OrderFrameDecoder());
                pipeline.addLast(new OrderFrameEncoder());
                pipeline.addLast(new OrderProtocolDecoder());
                pipeline.addLast(new OrderProtocolEncoder());
                // 客户端没有处理
//                pipeline.addLast(new OrderServerProcessHandler());

                pipeline.addLast(new LoggingHandler(LogLevel.INFO));
            }
        });

        ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8090);

        // 得先阻塞，直到连接成功，不然我们的消息发不出去
        channelFuture.sync();

        // 这边有个问题，就是我们不想每次都发送RequestMessage,我们只想发送我们业务相关的Operation
        // 有没有办法呢？这个其实是二次编码，MessageToMessage,我们可以新增一个实现类，来完成这种转换
        RequestMessage requestMessage = new RequestMessage(IdUtil.nextId(), new OrderOperation(9527, "photo"));

        channelFuture.channel().writeAndFlush(requestMessage);

        channelFuture.channel().closeFuture().get();


    }
}
