package cn.moe.server.loader;

import cn.moe.annotation.Controller;
import cn.moe.annotation.RequestMapping;
import cn.moe.service.Service;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HotCodeServer implements Runnable {

    public static final String PACKAGE_DOT = ".";
    String baseDirPath = System.getProperty("user.dir") + File.separatorChar + "hotcode" + File.separatorChar;
    String serviceClazzPackage = "cn.moe.service.impl";
    File fpackage;
    HashMap<String, LoadInfo> loadInfoMap = new HashMap<String, LoadInfo>();
    HashMap<String, Object> instanceMap = new HashMap<String, Object>();
    HashMap<String, Method> handleMap = new HashMap<String, Method>();
    List<String> classNames = new ArrayList<String>();


    public Service getService(String clazzName) throws ClassNotFindError {
        if (loadInfoMap.containsKey(clazzName))
            return loadInfoMap.get(clazzName).getService();
        throw new ClassNotFindError(clazzName + " class not find");
    }

    MyClassLoader myClassLoader;
    //单例模式
    public final static HotCodeServer instance = new HotCodeServer();

    private HotCodeServer() {
        init();
    }

    private void init() {
        scanPackage(serviceClazzPackage);
        filterAndNewInstance();

        //热加载 初始化
        hotCodeInit();

    }

    private void filterAndNewInstance() {
        for (String clzName : classNames) {
            Class clz = null;
            try {
                clz = Class.forName(clzName);
                if (clz.isAnnotationPresent(Controller.class)) {
                    instanceMap.put(clzName, clz.getConstructor().newInstance());
                    String cm = "";//路径
                    if (clz.isAnnotationPresent(RequestMapping.class)) {
                        cm = ((RequestMapping) clz.getAnnotation(RequestMapping.class)).value();
                    }
                    Method methods[] = clz.getDeclaredMethods();
                    for (Method m : methods) {
                        if (m.isAnnotationPresent(RequestMapping.class)) {
                            String rm = ((RequestMapping) m.getAnnotation(RequestMapping.class)).value();

                            handleMap.put(addUri(cm, rm), m);
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private String addUri(String cm, String rm) {
        if (!cm.startsWith("/"))
            cm = "/" + cm;
        if (!rm.startsWith("/"))
            rm = "/" + rm;
        return cm + rm;
    }

    private void hotCodeInit() {
        fpackage = new File(baseDirPath + File.separatorChar + serviceClazzPackage.replaceAll("\\.", File.separator));

        if (!fpackage.isDirectory()) {

            fpackage.mkdirs();
            //dir.mkdir();
        }

        try {
            myClassLoader = new MyClassLoader(new URL[]{
                    new URL("file:" + baseDirPath)});
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 递归扫描包路径
     * 取出所有满足条件的 全名类
     *
     * @param pack
     */
    private void scanPackage(String pack) {
        URL url = this.getClass().getResource(File.separatorChar + pack.replaceAll("//.", File.separator));

        File fs = new File(url.getFile());

        for (File f : fs.listFiles()) {
            if (!f.getName().endsWith(".class")) continue;
            if (f.isDirectory()) {
                scanPackage(pack + PACKAGE_DOT + f.getName());
            } else {
                classNames.add(pack + PACKAGE_DOT + f.getName().replace(".class", ""));
            }

        }
    }

    public void run() {
        while (true) {
            scanClass(fpackage);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void scanClass(File file) {
        for (File clzFile : file.listFiles()) {
            if (!clzFile.getName().endsWith(".class")) continue;
            String clzName = clzFile.getName().replace(".class", "");

            //是否已加载过
            try {
                if (loadInfoMap.containsKey(clzName)) {
                    LoadInfo li = loadInfoMap.get(clzName);
                    //是否修改过
                    if (li.getLoadTime() != clzFile.lastModified()) {
                        System.out.println("reload ==== " + clzName);
                        load(clzName, clzFile.lastModified(), li);
                        loadInfoMap.put(clzName, li);
                    }
                } else {
                    //新的class
                    LoadInfo li = load(clzName, clzFile.lastModified(), null);
                    loadInfoMap.put(clzName, li);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private LoadInfo load(String name, long l, LoadInfo li) throws Exception {
        System.out.println("load " + name);

        if (li == null)
            li = new LoadInfo();
        else if (li.getService() != null) {
            li.getService().clean();
            ClassLoader c = li.getService().getClass().getClassLoader();
            if (c instanceof URLClassLoader) {
                //TODO close
                //((URLClassLoader) c).close();
            }
        }

        //myClassLoader.addDir(baseDirPath);

        Class clazz = myClassLoader.loadClass(serviceClazzPackage + PACKAGE_DOT + name);

        li.setService((Service) clazz.getConstructor().newInstance());

        li.setLoadTime(l);


        return li;
    }

    public void test() {
        for (LoadInfo li : loadInfoMap.values()) {
            //li.getService().execute(null, null);
        }
    }

    public Method getHandle(String uri) {
        return handleMap.get(uri);
    }
}
