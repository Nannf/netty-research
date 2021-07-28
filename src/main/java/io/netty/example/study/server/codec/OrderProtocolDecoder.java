package io.netty.example.study.server.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.example.study.common.RequestMessage;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * @author Akmd Nannf
 * @version v1.0
 * @Description
 * 第二层的解码器，输入是FrameDecoder的没有粘包和半包问题的ByteBuf
 * 输出是我们业务定义的RequestMessage,
 * 注意泛型参数是我们的ByteBuf,不是结果。ByteBuf可能使用的是堆外内存，或者内存池，使用完之后需要释放，这个在handler处理的
 * @date 2021/7/28 17:58
 */
public class OrderProtocolDecoder extends MessageToMessageDecoder<ByteBuf> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> out) throws Exception {
        // ByteBuf --> RequestMessage;
        RequestMessage requestMessage =  new RequestMessage();
        requestMessage.decode(byteBuf);

        // 这步是必须写的，无论是decoder还是encoder
        out.add(requestMessage);
    }
}
