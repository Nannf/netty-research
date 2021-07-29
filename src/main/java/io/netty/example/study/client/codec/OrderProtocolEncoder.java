package io.netty.example.study.client.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.example.study.common.RequestMessage;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * @author Akmd Nannf
 * @version v1.0
 * @Description
 * 对客户端而言，这个是最开始的，要把我们的请求编码成字节流
 * @date 2021/7/28 20:04
 */
public class OrderProtocolEncoder extends MessageToMessageEncoder<RequestMessage> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RequestMessage requestMessage, List<Object> out) throws Exception {
        ByteBuf byteBuf = channelHandlerContext.alloc().buffer();
        requestMessage.encode(byteBuf);
        // 跟Decoder一样
        out.add(byteBuf);
    }
}
