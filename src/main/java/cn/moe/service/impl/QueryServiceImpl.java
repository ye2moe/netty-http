package cn.moe.service.impl;

import cn.moe.https.Https;
import cn.moe.https.Parse;
import cn.moe.https.User;
import cn.moe.service.Service;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

import java.util.Random;

public class QueryServiceImpl implements Service {

    public QueryServiceImpl() {
    }

    public void execute(FullHttpResponse httpResponse , HttpRequest request) {
        String username = Parse.getRequestParam(request.uri(),"username");

        if("".equals(username))
            return;
        httpResponse.content().writeBytes(Https.get(Https.currentAndDayCourses(new User(username,1))).getBytes());

    }

    public void clean() {

    }
}
