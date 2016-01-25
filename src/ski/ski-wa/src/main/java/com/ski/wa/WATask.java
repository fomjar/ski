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

public class WaTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(WaTask.class);
    
    public WaTask() {
        System.setProperty("webdriver.ie.driver", "lib/IEDriverServer.exe");
        AEMonitor.getInstance().start();
    }

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (!(msg instanceof FjDscpMessage)) {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
            return;
        }
        FjDscpMessage req = (FjDscpMessage) msg;
        int        cmd = req.cmd();
        JSONObject arg = req.argToJsonObject();
        String     cmdstring = Integer.toHexString(cmd);
        while (8 > cmdstring.length()) cmdstring = "0" + cmdstring;
        
        AE ae = AEMonitor.getInstance().getAe(cmd);
        if (null == ae) {
            logger.error("can not find an AE for cmd: 0x" + cmdstring);
            response(server.name(), req, String.format("{'code':%d, 'desc':'can not find any ae for cmd: 0x%s'}", DSCP.CODE.ERROR_WEB_AE_NOT_FOUND, cmdstring));
            return;
        }
        WebDriver driver = null;
        try {
            driver = new InternetExplorerDriver(); // 每次重启窗口，因为IE会内存泄漏
            ae.execute(driver, arg);
        } catch (Exception e) {
            logger.error("execute ae failed for cmd: 0x" + cmdstring, e);
            response(server.name(), req, String.format("{'code':%d, 'desc':\"execute ae failed for cmd(0x%s): %s\"}", DSCP.CODE.ERROR_WEB_AE_EXECUTE_FAILED, cmdstring, e.getMessage()));
            return;
        } finally {
            if (null != driver) driver.quit();
        }
        String desc = ae.desc();
        // string type for json
        if (null != desc && !desc.startsWith("{") && !desc.endsWith("}") && !desc.startsWith("[") && !desc.endsWith("]")) desc = "\"" + desc + "\"";
        response(server.name(), req, String.format("{'code':%d, 'desc':%s}", ae.code(), desc));
    }
    
    private static void response(String serverName, FjDscpMessage req, Object arg) {
        FjDscpMessage rsp = new FjDscpMessage();
        rsp.json().put("fs",  serverName);
        rsp.json().put("ts",  req.fs());
        rsp.json().put("sid", req.sid());
        rsp.json().put("cmd", req.cmd());
        rsp.json().put("arg", arg);
        FjServerToolkit.getSender(serverName).send(rsp);
    }
}
