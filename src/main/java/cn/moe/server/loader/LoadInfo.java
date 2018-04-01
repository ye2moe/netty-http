package cn.moe.server.loader;

import cn.moe.service.Service;

public class LoadInfo {
    private long loadTime ;
    private Service service;


    public long getLoadTime() {
        return loadTime;
    }

    public void setLoadTime(long loadTime) {
        this.loadTime = loadTime;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}
