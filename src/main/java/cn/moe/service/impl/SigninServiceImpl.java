package cn.moe.service.impl;

import cn.moe.https.CurrentCourse;
import cn.moe.https.Https;
import cn.moe.https.Parse;
import cn.moe.https.User;
import cn.moe.service.Service;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

public class SigninServiceImpl implements Service {


    public void execute(FullHttpResponse httpResponse, HttpRequest request) {
        String username = Parse.getRequestParam(request.uri(),"username");
        String cid = Parse.getRequestParam(request.uri(),"cid");
        String cname = Parse.getRequestParam(request.uri(),"cname");
        httpResponse.content().writeBytes(Https.get(Https.signCourse(new User(username,1),new CurrentCourse(cid,cname))).getBytes());

    }

    public void clean() {

    }
}
