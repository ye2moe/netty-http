package cn.moe.server.aop;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class CGLIBProxy implements MethodInterceptor {
    private Object target;
    private ProxyUtil proxyUtil;

    public CGLIBProxy(Object target) throws ClassNotFoundException {
        this.target = target;
        proxyUtil = new ProxyUtil();
    }

    public <T> T getProxy() {
        return (T) new Enhancer().create(this.target.getClass(), this);
    }

    public <T> T getProxy(Class<?> clazz) {
        return (T) new Enhancer().create(this.target.getClass(), this);
    }

    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        ProxyEntity proxyEntity = new ProxyEntity(proxy, this.target.getClass(), obj, method, args);
        return proxyUtil.generateEntity(proxyEntity);
    }
}