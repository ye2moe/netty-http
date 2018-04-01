package cn.moe.server.loader;

import cn.moe.service.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

public class LoaderServer implements Runnable {

    String baseDirPath = System.getProperty("user.dir") + File.separatorChar + "hotcode" + File.separatorChar;
    String serviceClazzPackage = "cn.moe.service.impl.";
    File fpackage;
    HashMap<String, LoadInfo> loadInfoMap = new HashMap<String, LoadInfo>();

    public Service getService(String clazzName) throws ClassNotFindError {
        if (loadInfoMap.containsKey(clazzName))
            return loadInfoMap.get(clazzName).getService();
        throw new ClassNotFindError(clazzName + " class not find");
    }

    static LoaderServer instance;

    public static synchronized LoaderServer getInstance() {
        if (LoaderServer.instance == null) {
            instance = new LoaderServer();
        }
        return instance;
    }

    private LoaderServer() {
        init();
    }
    MyClassLoader myClassLoader;
    private void init() {
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

        Class clazz = myClassLoader.loadClass(serviceClazzPackage + name);

        li.setService((Service) clazz.getConstructor().newInstance());

        li.setLoadTime(l);


        return li;
    }

    public void test() {
        for (LoadInfo li : loadInfoMap.values()) {
            li.getService().execute(null, null);
        }
    }
}
