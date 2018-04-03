package cn.moe.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

public interface Service {
    //void execute(FullHttpResponse httpResponse , HttpRequest request);

    void clean();
}
