package cn.moe.service.impl;

import cn.moe.annotation.Controller;
import cn.moe.annotation.RequestMapping;
import cn.moe.annotation.ResponseBody;
import cn.moe.service.Service;

@Controller
@RequestMapping("test")
public class MoneyServiceImpl implements Service{

    @RequestMapping("money")
    @ResponseBody
    public String execute() {
        return "1517";
    }

    public void clean() {

    }
}
