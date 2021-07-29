package io.netty.example.study.server.codec;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author Akmd Nannf
 * @version v1.0
 * @Description 这个是服务端第一层解码，命名中以FrameDecoder为结尾，主要解决的是tcp的粘包和半包的问题
 * 输出是一个没有粘包和半包问题的ByteBuf
 * 这还需要第二层的解码，把ByteBuf转换成我们可以使用的业务类
 * @date 2021/7/28 17:20
 */
public class OrderFrameDecoder extends LengthFieldBasedFrameDecoder {
    public OrderFrameDecoder() {
        // 这个地方的initialBytesToStrip设置的要和lengthFieldLength一致
        // 这是因为我们解析的时候只获取业务字段，不要长度字段
        super(
                Integer.MAX_VALUE,
                0,
                2,
                0,
                2);
    }
}
