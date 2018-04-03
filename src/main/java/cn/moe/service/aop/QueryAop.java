package cn.moe.service.aop;


import cn.moe.annotation.aop.After;
import cn.moe.annotation.aop.Aspect;
import cn.moe.annotation.aop.Before;

@Aspect
public class QueryAop {

    @Before("cn.moe.service.impl.QueryServiceImpl.query()")
    public void logQuery(){

    }


    @After("cn.moe.service.impl.QueryServiceImpl.query()")
    public void after(){

    }
}
