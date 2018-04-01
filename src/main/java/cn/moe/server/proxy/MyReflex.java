package cn.moe.server.proxy;

import cn.moe.server.loader.ClassNotFindError;
import cn.moe.server.loader.LoaderServer;
import cn.moe.service.Service;

import java.lang.reflect.InvocationTargetException;

public class MyReflex {

    private final static String SERVICE_PACKAGE_PATH = "cn.moe.service.";
    private final static String SERVICE_SUFFIX = "ServiceImpl";
    public static Service getServiceByReflex(String serviceName) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFindError {

        serviceName = toUpperFristChar(serviceName);

        return LoaderServer.getInstance().getService(serviceName + SERVICE_SUFFIX);
        //Class serviceClazz = Class.forName(SERVICE_PACKAGE_PATH + serviceName + SERVICE_SUFFIX);

        //return (Service) serviceClazz.getDeclaredConstructor().newInstance();
    }

    public static String toUpperFristChar(String string) {
        char[] charArray = string.toCharArray();
        if(charArray[0] >=97 &&charArray[0]<=122)
            charArray[0] -= 32;
        return String.valueOf(charArray);
    }
}
