package io.netty.example.study.server.codec;

import io.netty.handler.codec.LengthFieldPrepender;

/**
 * @author Akmd Nannf
 * @version v1.0
 * @Description
 * 这个是服务器端发给客户端的时候，客户端也需要tcp粘包半包的处理
 * @date 2021/7/28 20:07
 */
public class OrderFrameEncoder extends LengthFieldPrepender {
    public OrderFrameEncoder() {
        super(2);
    }
}
