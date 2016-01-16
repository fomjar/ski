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
    
    private static final Logger logger = Logger.getLogger(AEMonitor.class);
    
    private ClassLoader loader;
    
    public void start() {
        if (isRun()) {
            logger.warn("ae-monitor has already started");
            return;
        }
        new Thread(this, "ae-monitor").start();
    }
    
    @Override
    public void perform() {
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
        
        long interval = Long.parseLong(FjServerToolkit.getServerConfig("wa.ae.reload-interval"));
        setInterval(interval * 1000);
    }
    
    public AE getAe(int cmd) {
        if (null == loader) {
            logger.error("ae package is not available");
            return null;
        }
        String cmd_str = Integer.toHexString(cmd).toUpperCase();
        while (8 > cmd_str.length()) cmd_str = "0" + cmd_str;
        String className = FjServerToolkit.getServerConfig("ae.0x" + cmd_str);
        try {
            Class<?> clazz = loader.loadClass(className);
            Object instance = clazz.newInstance();
            if (!(instance instanceof AE)) {
                logger.error("invalid ae class: " + clazz.getName());
                return null;
            }
            return (AE) instance;
        } catch (ReflectiveOperationException e) {logger.error("error occurs when load ae class: " + className);}
        return null;
    }

}
