package com.fomjar.widget;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FjDaemonPool {
    
    private static ExecutorService pool = null;
    
    public static ExecutorService pool() {
        if (null == pool) pool = Executors.newCachedThreadPool();
        return pool;
    }

}
