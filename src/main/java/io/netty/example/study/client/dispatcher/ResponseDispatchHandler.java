package io.netty.example.study.client.dispatcher;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.example.study.common.ResponseMessage;

/**
 * @author Akmd Nannf
 * @version v1.0
 * @Description 这个是第二层解码器之后的第三层
 * 这个类存在的目的是把请求的结果放到我们建立的center上去
 * ，所以这个是一个客户端对输入数据(响应)的一个处理.
 * 这个需要放在pipeline之中
 * @date 2021/7/29 9:38
 */
public class ResponseDispatchHandler extends SimpleChannelInboundHandler<ResponseMessage> {
    private RequestPendingCenter requestPendingCenter;

    public ResponseDispatchHandler(RequestPendingCenter requestPendingCenter) {
        this.requestPendingCenter = requestPendingCenter;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ResponseMessage responseMessage) throws Exception {
        requestPendingCenter.set(responseMessage.getMessageHeader().getStreamId(),
                responseMessage.getMessageBody());
    }
}
