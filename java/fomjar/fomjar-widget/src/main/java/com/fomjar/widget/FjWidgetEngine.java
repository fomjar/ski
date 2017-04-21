package com.fomjar.widget;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fomjar.util.FjLoopTask;

/**
 * 组件刷新引擎，可以开启和关闭引擎
 *
 * @author fomja
 */
public abstract class FjWidgetEngine extends FjLoopTask {

    private static ExecutorService pool;

    public FjWidgetEngine() {
        setInterval(20L);
    }

    public synchronized void open() {
        if (isRun()) return;

        if (null == pool) pool = Executors.newCachedThreadPool();
        pool.submit(this);
    }

}
