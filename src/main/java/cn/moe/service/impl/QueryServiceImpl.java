package cn.moe.service.impl;

import cn.moe.annotation.RequestParam;
import cn.moe.annotation.ResponseBody;
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

    @ResponseBody
    public String execute(@RequestParam(value = "username",require = true) String username) {

        return Https.get(Https.currentAndDayCourses(new User(username,1)));

    }

    public void clean() {

    }
}
