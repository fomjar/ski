package com.ski.wa;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import com.ski.common.CommonDefinition;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class WaTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(WaTask.class);
    private WebDriver driver;
    
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
        logger.info(String.format("INSTRUCTION - %s:%s:0x%08X", req.fs(), req.sid(), inst));
        
        AE ae = AEMonitor.getInstance().getAe(inst);
        if (null == ae) {
            logger.error(String.format("can not find an AE for instuction: 0x%08X", inst));
            response(server.name(), req, String.format("{'code':%d, 'desc':'can not find any ae for instuction: 0x%08X'}", CommonDefinition.CODE.CODE_WEB_AE_NOT_FOUND, inst));
            return;
        }
        try {
            if (null == driver) driver = new InternetExplorerDriver(); // 每次重启窗口，因为IE会内存泄漏
            ae.execute(driver, args);
        } catch (Exception e) {
            logger.error(String.format("execute ae failed for instuction: 0x%08X", inst), e);
            String desc = e.getMessage();
            if (desc.contains(" (WARNING:")) desc = desc.substring(0, desc.indexOf(" (WARNING:"));
            response(server.name(), req, String.format("{'code':%d, 'desc':'execute ae failed for instuction(0x%08X): %s'}", CommonDefinition.CODE.CODE_WEB_AE_EXECUTE_FAILED, inst, desc));
            return;
        } finally {
            driver.quit();
            driver = null;
        }
        JSONObject args_rsp = new JSONObject();
        args_rsp.put("code", ae.code());
        args_rsp.put("desc", ae.desc());
        response(server.name(), req, args_rsp);
        
        if (0 == server.mq().size()) {
            String home = FjServerToolkit.getServerConfig("wa.home");
            if (null != home) {
                driver = new InternetExplorerDriver();
                driver.get(home);
            }
        }
    }
    
    private static void response(String server, FjDscpMessage req, Object args) {
        FjDscpMessage rsp = new FjDscpMessage();
        rsp.json().put("fs",   server);
        rsp.json().put("ts",   req.fs());
        rsp.json().put("sid",  req.sid());
        rsp.json().put("inst", req.inst());
        rsp.json().put("args", args);
        FjServerToolkit.getAnySender().send(rsp);
        logger.debug("response message: " + rsp);
    }
}
