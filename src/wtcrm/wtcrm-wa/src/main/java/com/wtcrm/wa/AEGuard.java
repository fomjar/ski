package com.wtcrm.wa;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;

import fomjar.server.FjServerToolkit;
import fomjar.util.FjLoopTask;

public class AEGuard extends FjLoopTask {
	
	private static AEGuard instance = null;
	public static AEGuard getInstance() {
		if (null == instance) instance = new AEGuard();
		return instance;
	}
	private AEGuard() {}
	
	private static final Logger logger = Logger.getLogger(AEGuard.class);
	
	private ClassLoader loader;
	
	public void start() {
		if (isRun()) {
			logger.warn("ae-guard has already started");
			return;
		}
		new Thread(this, "ae-guard").start();
	}
	
	@Override
	public void perform() {
		String aepkg = FjServerToolkit.getServerConfig("wa.ae-package");
		try {loader = new URLClassLoader(new URL[]{new File(aepkg).toURI().toURL()});}
		catch (MalformedURLException e) {logger.error("ae package is bad: " + aepkg, e);}
		
		long interval = Long.parseLong(FjServerToolkit.getServerConfig("wa.reload-ae-interval"));
		setInterval(interval * 1000);
	}
	
	public AE getAe(String ae_cmd) {
		if (null == loader) {
			logger.error("ae package is not available");
			return null;
		}
		String className = FjServerToolkit.getServerConfig(ae_cmd);
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
