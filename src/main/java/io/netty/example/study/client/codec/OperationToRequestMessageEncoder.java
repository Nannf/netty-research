package io.netty.example.study.client.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.example.study.common.Operation;
import io.netty.example.study.common.RequestMessage;
import io.netty.example.study.util.IdUtil;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * @author Akmd Nannf
 * @version v1.0
 * @Description
 * @date 2021/7/28 22:05
 */
public class OperationToRequestMessageEncoder extends MessageToMessageEncoder<Operation> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Operation operation, List<Object> out) throws Exception {
        RequestMessage requestMessage = new RequestMessage(IdUtil.nextId(), operation);

        out.add(requestMessage);
    }
}
