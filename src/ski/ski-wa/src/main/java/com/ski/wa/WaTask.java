package com.ski.wa;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import com.ski.common.SkiCommon;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;

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
        int        inst = req.inst();
        JSONObject args = req.argsToJsonObject();
        String     inststring = Integer.toHexString(inst);
        while (8 > inststring.length()) inststring = "0" + inststring;
        logger.info(String.format("INSTRUCTION - %s:%s:0x%s", req.fs(), req.sid(), inststring));
        
        AE ae = AEMonitor.getInstance().getAe(inst);
        if (null == ae) {
            logger.error("can not find an AE for instuction: 0x" + inststring);
            response(server.name(), req, String.format("{'code':%d, 'desc':'can not find any ae for instuction: 0x%s'}", SkiCommon.CODE.CODE_WEB_AE_NOT_FOUND, inststring));
            return;
        }
        WebDriver driver = null;
        try {
            driver = new InternetExplorerDriver(); // 每次重启窗口，因为IE会内存泄漏
            ae.execute(driver, args);
        } catch (Exception e) {
            logger.error("execute ae failed for instuction: 0x" + inststring, e);
            response(server.name(), req, String.format("{'code':%d, 'desc':\"execute ae failed for instuction(0x%s): %s\"}", SkiCommon.CODE.CODE_WEB_AE_EXECUTE_FAILED, inststring, e.getMessage()));
            return;
        } finally {
            if (null != driver) driver.quit();
        }
        String desc = ae.desc();
        // string type for json
        if (null != desc && !desc.startsWith("{") && !desc.endsWith("}") && !desc.startsWith("[") && !desc.endsWith("]")) desc = "\"" + desc + "\"";
        response(server.name(), req, String.format("{'code':%d, 'desc':%s}", ae.code(), desc));
    }
    
    private static void response(String serverName, FjDscpMessage req, Object args) {
        FjDscpMessage rsp = new FjDscpMessage();
        rsp.json().put("fs",   serverName);
        rsp.json().put("ts",   req.fs());
        rsp.json().put("sid",  req.sid());
        rsp.json().put("inst", req.inst());
        rsp.json().put("args", args);
        FjServerToolkit.getSender(serverName).send(rsp);
    }
}
