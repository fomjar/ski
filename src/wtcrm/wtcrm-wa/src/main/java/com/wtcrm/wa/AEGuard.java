package com.wtcrm.wa;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;

import fomjar.server.FjLoopTask;
import fomjar.server.FjToolkit;

public class AEGuard extends FjLoopTask {
	
	public static AEGuard instance = null;
	public static AEGuard getInstance() {
		if (null == instance) instance = new AEGuard();
		return instance;
	}
	
	private static final Logger logger = Logger.getLogger(AEGuard.class);
	
	private ClassLoader loader;
	
	public void start() {
		if (isRun()) {
			logger.warn("ae-guard has already started");
			return;
		}
		Thread thread = new Thread(this);
		thread.setName("ae-guard");
		thread.start();
	}
	
	@Override
	public void perform() {
		String aepkg = FjToolkit.getServerConfig("wa.ae-package");
		try {loader = new URLClassLoader(new URL[]{new File(aepkg).toURI().toURL()});}
		catch (MalformedURLException e) {logger.error("ae package is bad: " + aepkg, e);}
		
		long defaultInterval = Long.parseLong(FjToolkit.getServerConfig("wa.reload-ae-interval"));
		try {Thread.sleep(defaultInterval * 1000L);}
		catch (InterruptedException e) {logger.warn("Thread sleep interupted", e);}
	}
	
	public AE getAe(String aeName) {
		if (null == loader) {
			logger.error("ae package is not available");
			return null;
		}
		String className = FjToolkit.getServerConfig(aeName);
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
