package com.wtcrm.wa;

import net.sf.json.JSONObject;

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
	
	public WATask() {
		System.setProperty("webdriver.ie.driver", "lib/IEDriverServer.exe");
		AEGuard.getInstance().start();
	}

	@Override
	public void onMsg(FjServer server, FjMsg msg) {
		if (!(msg instanceof FjJsonMsg)
				|| !((FjJsonMsg) msg).json().containsKey("fs")
				|| !((FjJsonMsg) msg).json().containsKey("ts")
				|| !((FjJsonMsg) msg).json().containsKey("sid")) {
			logger.error("message not come from wtcrm server, discard: " + msg);
			return;
		}
		FjJsonMsg req = (FjJsonMsg) msg;
		if (!req.json().containsKey("ae-cmd") || !req.json().containsKey("ae-arg")) {
			logger.error("invalid ae request, request does not contain \"ae-cmd\" or \"ae-arg\" parameter: " + req);
			response(server, req, AE.CODE_INCORRECT_ARGUMENT, JSONObject.fromObject("{\"ae-err\":\"invalid ae request\"}"));
			return;
		}
		String ae_cmd = req.json().getString("ae-cmd");
		JSONObject ae_arg = req.json().getJSONObject("ae-arg");
		AE ae = AEGuard.getInstance().getAe(ae_cmd);
		if (null == ae) {
			logger.error("can not find an automation executor for ae-cmd: " + ae_cmd);
			response(server, req, AE.CODE_AE_NOT_FOUND, JSONObject.fromObject("{\"ae-err\":\"can not find any ae for ae-cmd: " + ae_cmd + "\"}"));
			return;
		}
		WebDriver driver = new InternetExplorerDriver(); // 每次重启窗口，因为IE会内存泄漏
		try {
			ae.execute(driver, ae_arg);
		} catch (Exception e) {
			logger.error("error occurs when execute ae-cmd: " + ae_cmd, e);
			response(server, req, AE.CODE_UNKNOWN_ERROR, JSONObject.fromObject("{\"ae-err\":\"unknown error during execute ae-cmd: " + ae_cmd + "\"}"));
			return;
		} finally {
			driver.quit();
		}
		response(server, req, ae.code(), ae.desc());
	}
	
	private static void response(FjServer server, FjJsonMsg req, int ae_code, JSONObject ae_desc) {
		FjJsonMsg rsp = new FjJsonMsg();
		rsp.json().put("fs", server.name());
		rsp.json().put("ts", req.json().getString("fs"));
		rsp.json().put("sid", req.json().getString("sid"));
		rsp.json().put("ae-code", ae_code);
		rsp.json().put("ae-desc", ae_desc);
		FjToolkit.getSender(server.name()).send(rsp);
	}
}
