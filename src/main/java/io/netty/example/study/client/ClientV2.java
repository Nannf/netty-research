package io.netty.example.study.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.example.study.client.codec.*;
import io.netty.example.study.client.dispatcher.OperationResultFuture;
import io.netty.example.study.client.dispatcher.RequestPendingCenter;
import io.netty.example.study.client.dispatcher.ResponseDispatchHandler;
import io.netty.example.study.common.Operation;
import io.netty.example.study.common.OperationResult;
import io.netty.example.study.common.RequestMessage;
import io.netty.example.study.common.order.OrderOperation;
import io.netty.example.study.util.IdUtil;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.ExecutionException;

/**
 * @author Akmd Nannf
 * @version v1.0
 * @Description V1版本的客户端我们发现它无法获取处置的结果，因为我们是异步发送的，
 * 请求的发送和结果之间需要一个对应关系，所以我们需要一个map。
 * 我们知道，异步的处置返回的都是Future,map我们之前在设计Message的时候，就使用了一个StreamId来做标识
 * 为了能完成这个交互，我们需要先写一个Future
 * @date 2021/7/28 21:25
 */
public class ClientV2 {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 客户端没有server
        Bootstrap bootstrap = new Bootstrap();

        // 设置i/o 模式。 这个是反射+工厂获取的
        bootstrap.channel(NioSocketChannel.class);

        // 设置reactor模式
        bootstrap.group(new NioEventLoopGroup());

        // 设置日志，客户端只能设置一次，后设置的handler会替代之前设置的，所以这个不需要了
//        bootstrap.handler(new LoggingHandler(LogLevel.INFO));

        RequestPendingCenter requestPendingCenter = new RequestPendingCenter();

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

                pipeline.addLast(new ResponseDispatchHandler(requestPendingCenter));
                pipeline.addLast(new OperationToRequestMessageEncoder());
                pipeline.addLast(new LoggingHandler(LogLevel.INFO));
            }
        });

        ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8090);

        // 得先阻塞，直到连接成功，不然我们的消息发不出去
        channelFuture.sync();

        // 这边有个问题，就是我们不想每次都发送RequestMessage,我们只想发送我们业务相关的Operation
        // 有没有办法呢？这个其实是二次编码，MessageToMessage,我们可以新增一个实现类，来完成这种转换

        long streamId = IdUtil.nextId();
        RequestMessage requestMessage = new RequestMessage(streamId, new OrderOperation(9527, "photo"));

        OperationResultFuture operationResultFuture = new OperationResultFuture();
        // 在发送之前做
        requestPendingCenter.add(streamId,operationResultFuture);

//        Operation operation = new OrderOperation(9527, "photo");
        // 当然，如果我们使用RequestMessage也是可以写进去的，因为MessageToMessageEncoder做了一个判断，就是我们的输入类型必须
        // 和泛型参数一致才会做转换
        channelFuture.channel().writeAndFlush(requestMessage);


        OperationResult operationResult = operationResultFuture.get();

        System.out.println(operationResult);

        channelFuture.channel().closeFuture().get();


    }
}
