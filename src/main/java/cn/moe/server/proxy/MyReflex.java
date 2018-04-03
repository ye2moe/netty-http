package cn.moe.server.proxy;

import cn.moe.annotation.RequestParam;
import cn.moe.annotation.ResponseBody;
import cn.moe.wxcourse.Parse;
import cn.moe.server.loader.ClassNotFindError;
import cn.moe.server.loader.HotCodeServer;
import cn.moe.service.Service;
import cn.moe.service.impl.QueryServiceImpl;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MyReflex {

    private final static String SERVICE_PACKAGE_PATH = "cn.moe.service.";
    private final static String SERVICE_SUFFIX = "ServiceImpl";


    public static void invokeService(HttpRequest request, FullHttpResponse response) {
        HotCodeServer hc = HotCodeServer.getInstance();

        Object objs[] = hc.getHandle(request.uri());

        if (objs == null) {
            System.out.println(request.uri() + ": no mapping");
            return;
        }
        Method m = (Method) objs[1];
        try {
            invoke(objs[0], m, response, request);
        } catch (NoSuchParameter noSuchParameter) {
            noSuchParameter.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        //m.invoke(objs[1])
    }

    @Deprecated
    public static Service getServiceByReflex(String serviceName) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFindError {

        serviceName = toUpperFristChar(serviceName);

        return HotCodeServer.getInstance().getService(serviceName + SERVICE_SUFFIX);
        //Class serviceClazz = Class.forName(SERVICE_PACKAGE_PATH + serviceName + SERVICE_SUFFIX);

        //return (Service) serviceClazz.getDeclaredConstructor().newInstance();
    }

    @Deprecated
    public static void invokeService(Service service, FullHttpResponse httpResponse, HttpRequest httpRequest) throws NoSuchParameter {
        try {

            Method methods[] = service.getClass().getDeclaredMethods();

            Method execute = null;
            for (Method m : methods) {
                if (m.getName().equals("execute"))
                    execute = m;
                //System.out.println(m.toGenericString());
            }
            invoke(service, execute, httpResponse, httpRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void invoke(Object service, Method execute, FullHttpResponse httpResponse, HttpRequest httpRequest) throws NoSuchParameter, IllegalAccessException, InvocationTargetException {
        /*
        String method = httpRequest.method().name();
        if (methodCheck(service.getClass(), method)) return;

        if(service.getClass().isAnnotationPresent(PostMapping.class)){
            if(method != "GET"){
                System.out.println(service.getClass().getSimpleName()+"："+ method +" method not support!");
                return;
            }
        }
        */
        Annotation ass[][] = execute.getParameterAnnotations();
        Class parameterTypes[] = execute.getParameterTypes();
        Object[] params = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            //参数是否有注解
            Class pt = parameterTypes[i];
            //System.out.println(pt.getName());
            for (Annotation p : ass[i]) {
                if (p.annotationType().equals(RequestParam.class)) {
                    RequestParam requestParam = (RequestParam) p;
                    //根据 requestParam.value() 获得参数
                    String username = Parse.getRequestParam(httpRequest.uri(), requestParam.value());
                    if (requestParam.require() && "".equals(username))
                        throw new NoSuchParameter(requestParam.value() + " param not contains");
                    params[i] = username;
                }
            }
            if (pt.equals(HttpResponse.class)) {
                params[i] = httpResponse;
            } else if (pt.equals(HttpRequest.class)) {
                params[i] = httpRequest;
            }
        }

        //throw new IllegalArgumentException("argument type mismatch");


        Object obj = execute.invoke(service, params);

        boolean isJsonBack = false;

        for (Annotation methodAnno : execute.getAnnotations()) {
            if (methodAnno.annotationType().equals(ResponseBody.class)) {
                httpResponse.content().writeBytes(obj.toString().getBytes());
                isJsonBack = true;
            }
        }
        if(!isJsonBack){
            httpResponse.headers().set(HttpHeaderNames.LOCATION, obj);
        }
    }


    public static void main(String argvs[]) throws NoSuchParameter {
        MyReflex.invokeService(new QueryServiceImpl(), null, null);
    }

    public static String toUpperFristChar(String string) {
        char[] charArray = string.toCharArray();
        if (charArray[0] >= 97 && charArray[0] <= 122)
            charArray[0] -= 32;
        return String.valueOf(charArray);
    }

}
