package com.ski.wa;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import com.ski.common.DSCP;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjMessage;

public class WATask implements FjServerTask {
	
	private static final Logger logger = Logger.getLogger(WATask.class);
	
	public WATask() {
		System.setProperty("webdriver.ie.driver", "lib/IEDriverServer.exe");
		AEGuard.getInstance().start();
	}

	@Override
	public void onMessage(FjServer server, FjMessageWrapper wrapper) {
		FjMessage msg = wrapper.message();
		if (!(msg instanceof FjDscpMessage)) {
			logger.error("unsupported format message: " + msg);
			return;
		}
		FjDscpMessage req = (FjDscpMessage) msg;
		int        cmd = req.cmd();
		JSONObject arg = (JSONObject) req.arg();
		AE ae = AEGuard.getInstance().getAe(cmd);
		if (null == ae) {
			logger.error("can not find an AE for cmd: " + cmd);
			response(server.name(), req, DSCP.CMD.ERROR_WEB_AE_NOT_FOUND, JSONObject.fromObject("{'error':'can not find any ae for cmd: " + cmd + "'}"));
			return;
		}
		WebDriver driver = null;
		try {
			driver = new InternetExplorerDriver(); // 每次重启窗口，因为IE会内存泄漏
			ae.execute(driver, arg);
		} catch (Exception e) {
			logger.error("execute ae failed for cmd: " + cmd, e);
			response(server.name(), req, DSCP.CMD.ERROR_WEB_AE_EXECUTE_FAILED, JSONObject.fromObject(String.format("{'error':\"execute ae failed for cmd(%s): %s\"}", cmd, e.getMessage())));
			return;
		} finally {
			if (null != driver) driver.quit();
		}
		response(server.name(), req, ae.cmd(), ae.arg());
	}
	
	private static void response(String serverName, FjDscpMessage req, int cmd, JSONObject arg) {
		FjDscpMessage rsp = new FjDscpMessage();
		rsp.json().put("fs",  serverName);
		rsp.json().put("ts",  req.fs());
		rsp.json().put("sid", req.sid());
		rsp.json().put("cmd", cmd);
		rsp.json().put("arg", arg);
		FjServerToolkit.getSender(serverName).send(rsp);
	}
}
