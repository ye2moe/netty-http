package cn.moe.server.aop;

import cn.moe.annotation.aop.After;
import cn.moe.annotation.aop.Aspect;
import cn.moe.annotation.aop.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Reflect {
    Map<String, String> map;   //存入的是方法名以及其注解
    Map<String, String> clazzMap;

    public Reflect() throws ClassNotFoundException {
        map = new HashMap<String, String>();
        clazzMap = new HashMap<String, String>();
        getAnnotationClass();
    }

    public Map<String, String> getMap() {  // 这里返回的是已经全部存好的map方面ProxyUtil使用
        return map;
    }

    @Test
    public void getAnnotationClass() throws ClassNotFoundException {
        String clazzName = "cn.moe.service.aop.QueryAop";
        Class<?> clazz = Class.forName(clazzName, false, Thread.currentThread().getContextClassLoader());
        // 这里为了省事直接动态加载了该类
        if (clazz.isAnnotationPresent(Aspect.class)) {  //假设是注解类
            Method[] methods = clazz.getDeclaredMethods();   //遍历方法
            for (Method method : methods) {
                if (method.isAnnotationPresent(Before.class)) {
                    // 获取注解
                    Before before = method.getAnnotation(Before.class);
                    String beforeValue = before.value();
                    // 获取注解的值以及当前类的名字方面调用方法
                    map.put(method.getName() + "-" + clazzName + "-" + "before", beforeValue.substring(0, beforeValue.length() - 2));
                    // 存入的是方法名和注解名以及执行的顺序，这里为了省事直接就在后面写了
                    if (method.isAnnotationPresent(After.class)) {
                        After after = method.getAnnotation(After.class);
                        String afterValue = after.value();
                        map.put(method.getName() + "-" + clazzName + "-" + "after", afterValue.substring(0, afterValue.length() - 2));
                    }
                }
            }
        }
    }
}