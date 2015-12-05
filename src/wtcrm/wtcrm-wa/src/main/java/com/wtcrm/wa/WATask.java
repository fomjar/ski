package com.wtcrm.wa;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import fomjar.server.FjJsonMsg;
import fomjar.server.FjMsg;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjToolkit;

public class WATask implements FjServerTask {
	
	private static final Logger logger = Logger.getLogger(WATask.class);
	
	private WebDriver driver;
	
	public WATask() {
		System.setProperty("webdriver.ie.driver", "lib/IEDriverServer.exe");
		driver = new InternetExplorerDriver();
		driver.get("about:blank");
		AEGuard.getInstance().start();
	}

	@Override
	public void onMsg(FjServer server, FjMsg msg) {
		if (!(msg instanceof FjJsonMsg)
				|| !((FjJsonMsg) msg).json().containsKey("fs")
				|| !((FjJsonMsg) msg).json().containsKey("ts")) {
			logger.warn("message does not come from wtcrm server, discard: " + msg);
			return;
		}
		FjJsonMsg req = (FjJsonMsg) msg;
		String aeName = req.json().getString("ae");
		AE ae = AEGuard.getInstance().getAe(aeName);
		if (null == ae) {
			logger.error("can not find an automation executor for ae name: " + aeName);
			return;
		}
		try {ae.execute(driver);}
		catch (Exception ee) {logger.warn("error occurs when execute web automation", ee);}
		FjJsonMsg rsp = ae.getResponse();
		rsp.json().put("fs", server.name());
		rsp.json().put("ts", req.json().getString("fs"));
		rsp.json().put("sid", req.json().getString("sid"));
		FjToolkit.getSender(server.name()).send(rsp);
	}

}
