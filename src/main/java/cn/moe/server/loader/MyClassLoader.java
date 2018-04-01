package cn.moe.server.loader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;

public class MyClassLoader extends URLClassLoader {
    public MyClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    public MyClassLoader(URL[] urls) {
        super(urls);
    }

    Date startDate = new Date();

    public void addJar(String jarFile) throws MalformedURLException {
        addURL(new URL("file:"+jarFile));
    }

    public void addDir(String path) throws MalformedURLException {
        addURL(new URL("file:"+path));
    }


    @Override
    public String toString() {
        return super.toString() + ",time:"+startDate.getTime();
    }
}
