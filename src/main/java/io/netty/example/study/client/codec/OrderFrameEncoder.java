package io.netty.example.study.client.codec;

import io.netty.handler.codec.LengthFieldPrepender;

/**
 * @author Akmd Nannf
 * @version v1.0
 * @Description
 * 客户端把请求转换成ByteBuf的时候，客户端也需要tcp粘包半包的处理
 * @date 2021/7/28 20:07
 */
public class OrderFrameEncoder extends LengthFieldPrepender {
    public OrderFrameEncoder() {
        super(2);
    }
}
