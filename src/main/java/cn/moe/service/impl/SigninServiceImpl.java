package cn.moe.service.impl;

import cn.moe.annotation.RequestParam;
import cn.moe.annotation.ResponseBody;
import cn.moe.wxcourse.CurrentCourse;
import cn.moe.wxcourse.Https;
import cn.moe.wxcourse.User;
import cn.moe.service.Service;


public class SigninServiceImpl implements Service {


    @ResponseBody
    public String execute(
            @RequestParam(value = "username",require = true) String username
            ,@RequestParam(value = "cid",require = true) String cid
            ,@RequestParam(value = "cname",require = true) String cname) {

       return Https.get(Https.signCourse(new User(username,1),new CurrentCourse(cid,cname)));

    }

    public void clean() {

    }
}
