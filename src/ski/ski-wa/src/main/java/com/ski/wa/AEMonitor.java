package com.ski.wa;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;

import fomjar.server.FjServerToolkit;
import fomjar.util.FjLoopTask;

public class AEMonitor extends FjLoopTask {
    
    private static AEMonitor instance = null;
    public static AEMonitor getInstance() {
        if (null == instance) instance = new AEMonitor();
        return instance;
    }
    private AEMonitor() {}
    
    public void start() {
        if (isRun()) {
            logger.warn("monitor-ae has already started");
            return;
        }
        new Thread(this, "monitor-ae").start();
    }
    
    private static final Logger logger = Logger.getLogger(AEMonitor.class);
    
    private ClassLoader loader;
    
    @Override
    public void perform() {
        resetInterval();
        String aedir = FjServerToolkit.getServerConfig("wa.ae.directory");
        File faedir = new File(aedir);
        if (!faedir.isDirectory()) {
            logger.error("invalid AE directory: " + aedir);
            return;
        }
        File[] faes = faedir.listFiles();
        URL[] urls = new URL[faes.length];
        try {
            for (int i = 0; i < faes.length; i++) urls[i] = faes[i].toURI().toURL();
            loader = new URLClassLoader(urls);
        } catch (MalformedURLException e) {logger.error("some ae package is bad", e);}
    }

    private void resetInterval() {
        long second = Long.parseLong(FjServerToolkit.getServerConfig("wa.ae.reload-interval"));
        setInterval(second);
    }
    
    @Override
    public void setInterval(long second) {
        logger.debug("will try again after " + second + " seconds");
        super.setInterval(second * 1000);
    }
    
    public AE getAe(int inst) {
        if (null == loader) {
            logger.error("ae package is not available");
            return null;
        }
        String className = FjServerToolkit.getServerConfig(String.format("ae.0x%08X", inst));
        try {
            Class<?> clazz = loader.loadClass(className);
            Object instance = clazz.newInstance();
            if (!(instance instanceof AE)) {
                logger.error("invalid ae class: " + clazz.getName());
                return null;
            }
            return (AE) instance;
        } catch (Exception e) {logger.error("error occurs when load ae class: " + className, e);}
        return null;
    }
}
