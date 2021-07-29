package io.netty.example.study.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.example.study.common.*;

/**
 * @author Akmd Nannf
 * @version v1.0
 * @Description 这个是接着ProtocolDecoder处理的，之前的输出只是完成转换，但是没有完成业务逻辑，
 * 这个handler可以说是实际的业务逻辑的处理类.
 * 处理的类型是Inbound，就是输入事件的处理。
 * 之所以继承SimpleChannelInboundHandler是因为它可以帮我释放资源
 * @date 2021/7/28 18:07
 */
public class OrderServerProcessHandler extends SimpleChannelInboundHandler<RequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RequestMessage requestMessage) throws Exception {
        Operation operation = requestMessage.getMessageBody();
        OperationResult operationResult = operation.execute();

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessageHeader(requestMessage.getMessageHeader());
        responseMessage.setMessageBody(operationResult);

        // 此时没有out参数了
        // 此时的responseMessage需要发送给客户端，为了解决粘包和半包问题，我们需要新建两个encoder
        channelHandlerContext.writeAndFlush(responseMessage);

        // 这种最好不要乱用，这个会把消息给pipeline上所有的handler
        // 某些情况下会有死循环
//        channelHandlerContext.channel().writeAndFlush(responseMessage);
    }
}
