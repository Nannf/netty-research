package io.netty.example.study.server.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.example.study.common.ResponseMessage;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * @author Akmd Nannf
 * @version v1.0
 * @Description
 * 这个是和Decoder对应的。
 * 作用就是把Handler生成的requestMessage转换成ByteBuffer
 * @date 2021/7/28 20:04
 */
public class OrderProtocolEncoder extends MessageToMessageEncoder<ResponseMessage> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ResponseMessage responseMessage, List<Object> out) throws Exception {
        ByteBuf byteBuf = channelHandlerContext.alloc().buffer();
        responseMessage.encode(byteBuf);

        // 跟Decoder一样
        out.add(byteBuf);
    }
}
