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

    volatile boolean isHotCode = true;
    public static final String PACKAGE_DOT = ".";
    String baseDirPath = System.getProperty("user.dir") + File.separatorChar + "hotcode" + File.separatorChar;
    String serviceClazzPackage = "cn.moe.service.impl";
    File fpackage;
    HashMap<String, LoadInfo> loadInfoMap = new HashMap<String, LoadInfo>();

    //Object[0] instance Object[1] method
    HashMap<String, Object[]> handleMap = new HashMap<String,  Object[]>();
    List<String> classNames = new ArrayList<String>();


    public Service getService(String clazzName) throws ClassNotFindError {
        if (loadInfoMap.containsKey(clazzName))
            return loadInfoMap.get(clazzName).getService();
        throw new ClassNotFindError(clazzName + " class not find");
    }

    MyClassLoader myClassLoader;
    //单例模式
    private static HotCodeServer instance;

    public static HotCodeServer getInstance(){
        if(instance == null) {
            instance = new HotCodeServer();
        }
        return instance;
    }
    private HotCodeServer() {
        init();
    }

    private void init() {
        scanPackage(serviceClazzPackage);
        filterAndNewInstance();

        //热加载 初始化
        if(isHotCode)
            hotCodeInit();

    }

    private void filterAndNewInstance() {
        for (String clzName : classNames) {
            handleMapping(clzName);
        }
    }

    /**
     * 全限定类名
     * @param clzName
     */
    private void handleMapping(String clzName) {
        try {
            Object instance = Class.forName(clzName).getConstructor().newInstance();
            doAnnotationMapping(instance);
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

    private void doAnnotationMapping(Object instance) {
        Class clz = instance.getClass();
        if (clz.isAnnotationPresent(Controller.class)) {
            String cm = "";//class路径
            if (clz.isAnnotationPresent(RequestMapping.class)) {
                cm = ((RequestMapping) clz.getAnnotation(RequestMapping.class)).value();
            }
            Method methods[] = clz.getDeclaredMethods();
            for (Method m : methods) {
                if (m.isAnnotationPresent(RequestMapping.class)) {
                    String rm = m.getAnnotation(RequestMapping.class).value();
                    handleMap.put(addUri(cm, rm), new Object[]{instance,m});
                }
            }
        }
    }

    private String addUri(String cm, String rm) {
        if (!"".equals(cm) && !cm.startsWith("/"))
            cm = "/" + cm;
        if (!rm.startsWith("/"))
            rm = "/" + rm;
        return cm + rm;
    }

    private void hotCodeInit() {

        fpackage = new File(baseDirPath + File.separatorChar + replaceDot2Separator(serviceClazzPackage));

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

    private String replaceDot2Separator(String path){
        return path.replace(".","/");
    }

    /**
     * 递归扫描包路径
     * 取出所有满足条件的 全名类
     *
     * @param pack
     */
    private void scanPackage(String pack) {

        //System.out.println(File.separatorChar + replaceDot2Separator(pack));

        URL url = ClassLoader.getSystemResource(replaceDot2Separator(pack));

        System.out.println(url);
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
            if(isHotCode)
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
                        addHotCode(clzName,clzFile.lastModified(),li);
                    }
                } else {
                    //新的class
                    addHotCode(clzName,clzFile.lastModified(),null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void addHotCode(String clzName,long time,LoadInfo li) throws Exception {
        if(li == null){
            System.out.println("load -> " + clzName);
        }else{
            System.out.println("reload -> " + clzName);
        }
        LoadInfo n = load(clzName,time, li);
        loadInfoMap.put(clzName, n);
        doAnnotationMapping(n.getService());
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

    public Object[] getHandle(String uri) {
        return handleMap.get(uri);
    }
}
