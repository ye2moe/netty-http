package cn.moe.service.impl;

import cn.moe.annotation.Controller;
import cn.moe.annotation.RequestMapping;
import cn.moe.annotation.ResponseBody;
import cn.moe.service.Service;

@Controller
@RequestMapping("help")
public class HelpServiceImpl implements Service{

    @RequestMapping("me")
    public String execute() {
        return "https://www.baidu.com/";
    }

    public void clean() {

    }
}
