package com.ski.wa;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import fomjar.server.FjJsonMsg;
import fomjar.server.FjMsg;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;

public class WATask implements FjServerTask {
	
	private static final Logger logger = Logger.getLogger(WATask.class);
	
	public WATask() {
		System.setProperty("webdriver.ie.driver", "lib/IEDriverServer.exe");
		AEGuard.getInstance().start();
	}

	@Override
	public void onMsg(FjServer server, FjMsg msg) {
		if (!FjServerToolkit.isLegalRequest(msg)) {
			logger.error("illegal request: " + msg);
			if (FjServerToolkit.isLegalMsg(msg)) response(server, (FjJsonMsg) msg, AE.CODE_ILLEGAL_MESSAGE, JSONObject.fromObject("{\"error\":\"illegal request\"}"));
			return;
		}
		FjJsonMsg req = (FjJsonMsg) msg;
		String cmd = req.json().getString("cmd");
		JSONObject arg = req.json().getJSONObject("arg");
		AE ae = AEGuard.getInstance().getAe(cmd);
		if (null == ae) {
			logger.error("can not find an automation executor for cmd: " + cmd);
			response(server, req, AE.CODE_AE_NOT_FOUND, JSONObject.fromObject("{\"error\":\"can not find any ae for cmd: " + cmd + "\"}"));
			return;
		}
		WebDriver driver = null;
		try {
			driver = new InternetExplorerDriver(); // 每次重启窗口，因为IE会内存泄漏
			ae.execute(driver, arg);
		} catch (Exception e) {
			logger.error("error occurs when execute cmd: " + cmd, e);
			response(server, req, AE.CODE_UNKNOWN_ERROR, JSONObject.fromObject("{\"error\":\"unknown error during execute cmd: " + cmd + "\"}"));
			return;
		} finally {
			if (null != driver) driver.quit();
		}
		response(server, req, ae.code(), ae.desc());
	}
	
	private static void response(FjServer server, FjJsonMsg req, int code, JSONObject desc) {
		FjJsonMsg rsp = new FjJsonMsg();
		rsp.json().put("fs", server.name());
		rsp.json().put("ts", req.json().getString("fs"));
		rsp.json().put("sid", req.json().getString("sid"));
		rsp.json().put("code", code);
		rsp.json().put("desc", desc);
		FjServerToolkit.getSender(server.name()).send(rsp);
	}
}
