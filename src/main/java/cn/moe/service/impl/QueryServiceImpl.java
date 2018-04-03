package cn.moe.service.impl;

import cn.moe.annotation.Controller;
import cn.moe.annotation.RequestMapping;
import cn.moe.annotation.RequestParam;
import cn.moe.annotation.ResponseBody;
import cn.moe.wxcourse.Https;
import cn.moe.wxcourse.User;
import cn.moe.service.Service;

@Controller
public class QueryServiceImpl implements Service {

    public QueryServiceImpl() {
    }

    @RequestMapping("query")
    @ResponseBody
    public String query(@RequestParam(value = "username",require = true) String username) {

        return Https.get(Https.currentAndDayCourses(new User(username,1)));

    }

    public void clean() {

    }
}
