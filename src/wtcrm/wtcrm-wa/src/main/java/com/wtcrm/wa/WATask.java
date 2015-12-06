package com.wtcrm.wa;

import net.sf.json.JSONArray;
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
	
	private WebDriver driver;
	
	public WATask() {
		System.setProperty("webdriver.ie.driver", "lib/IEDriverServer.exe");
		driver = new InternetExplorerDriver();
		AEGuard.getInstance().start();
	}

	@Override
	public void onMsg(FjServer server, FjMsg msg) {
		if (!(msg instanceof FjJsonMsg)
				|| !((FjJsonMsg) msg).json().containsKey("fs")
				|| !((FjJsonMsg) msg).json().containsKey("ts")) {
			logger.error("message does not come from wtcrm server, discard: " + msg);
			return;
		}
		FjJsonMsg req = (FjJsonMsg) msg;
		if (!req.json().containsKey("ae-cmd") || !req.json().containsKey("ae-arg")) {
			logger.error("invalid ae request, request does not contain \"ae-cmd\" or \"ae-arg\" parameter: " + req);
			response(server, req, -1, JSONArray.fromObject("[\"invalid ae request\"]"));
			return;
		}
		String ae_cmd = req.json().getString("ae-cmd");
		JSONObject ae_arg = req.json().getJSONObject("ae-arg");
		AE ae = AEGuard.getInstance().getAe(ae_cmd);
		if (null == ae) {
			logger.error("can not find an automation executor for ae command: " + ae_cmd);
			response(server, req, -1, JSONArray.fromObject("[\"can not find any ae for ae-cmd: " + ae_cmd + "\"]"));
			return;
		}
		try {
			ae.execute(driver, ae_arg);
			response(server, req, ae.code(), ae.desc());
		} catch (Exception e) {
			logger.error("error occurs when execute ae: " + ae_cmd, e);
			response(server, req, -1, JSONArray.fromObject("[\"unknown error during execute ae: " + ae_cmd + "\"]"));
		}
	}
	
	private static void response(FjServer server, FjJsonMsg req, int ae_code, JSONArray ae_desc) {
		FjJsonMsg rsp = new FjJsonMsg();
		rsp.json().put("fs", server.name());
		rsp.json().put("ts", req.json().getString("fs"));
		rsp.json().put("sid", req.json().getString("sid"));
		rsp.json().put("ae-code", ae_code);
		rsp.json().put("ae-desc", ae_desc);
		FjToolkit.getSender(server.name()).send(rsp);
	}
}
