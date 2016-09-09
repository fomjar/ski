package com.ski.wa;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import com.ski.common.CommonDefinition;
import com.ski.wa.ae.psn.Login;

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
    
    @Override
    public void initialize(FjServer server) {
        System.setProperty("webdriver.ie.driver", "lib/IEDriverServer.exe");
    }

    @Override
    public void destroy(FjServer server) {
        if (null != driver) driver.quit();
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
        
        AE ae = getAe(inst);
        if (null == ae) {
            logger.error(String.format("can not find an AE for instuction: 0x%08X", inst));
            response(server.name(), req, String.format("{'code':%d, 'desc':'can not find any ae for instuction: 0x%08X'}", CommonDefinition.CODE.CODE_WEB_AE_NOT_FOUND, inst));
            return;
        }
        try {
            if (null == driver) driver = new InternetExplorerDriver(); // 每次重启窗口，因为IE会内存泄漏
            ae.execute(driver, args);
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", ae.code());
            args_rsp.put("desc", ae.desc());
            response(server.name(), req, args_rsp);
            if (CommonDefinition.CODE.CODE_SYS_SUCCESS != ae.code()) {
                logger.error(String.format("execute ae failed 0x%08X:%s", ae.code(), ae.desc()));
                recordFail();
            }
            if (ae.code() == CommonDefinition.CODE.CODE_WEB_PSN_USER_OR_PASS_INCORRECT) {
                logger.error("user or pass is incorrent: " + args + ", now reset");
                reset(driver);
            }
        } catch (Exception e) {
            logger.error(String.format("execute ae failed for instuction: 0x%08X", inst), e);
            String desc = e.getMessage();
            if (desc.contains(" (WARNING:"))    desc = desc.substring(0, desc.indexOf(" (WARNING:"));
            if (desc.contains("\n"))            desc = desc.substring(0, desc.indexOf("\n"));
            response(server.name(), req, String.format("{'code':%d, 'desc':'execute ae failed for instuction(0x%08X): %s'}", CommonDefinition.CODE.CODE_WEB_AE_EXECUTE_FAILED, inst, desc));
            recordFail();
        } finally {
            driver.quit();
            driver = null;
        }
        
        if (0 == server.mq().size()) {
            String home = FjServerToolkit.getServerConfig("wa.home");
            if (null != home) {
                driver = new InternetExplorerDriver();
                driver.get(home);
            }
        }
    }
    
    private static void reset(WebDriver driver) {
        String[] reset = FjServerToolkit.getServerConfig("wa.reset").split("/");
        String user = reset[0];
        String pass = reset[1];
        JSONObject args = new JSONObject();
        args.put("user", user);
        args.put("pass", pass);
        new Login().execute(driver, args);
    }
    
    private static Robot robot = null;
    
    private static void recordFail() {
        try {
            if (null == robot) robot = new Robot();
            BufferedImage image = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            ImageIO.write(image, "png", new File(String.format("log/%s.png", new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()))));
        } catch (HeadlessException | AWTException | IOException e) {
            logger.error("record fail failed", e);
        }
    }
    
    public AE getAe(int inst) {
        String className = FjServerToolkit.getServerConfig(String.format("ae.0x%08X", inst));
        try {
            Class<?> clazz = Class.forName(className);
            Object instance = clazz.newInstance();
            if (!(instance instanceof AE)) {
                logger.error("invalid ae class: " + clazz.getName());
                return null;
            }
            return (AE) instance;
        } catch (Exception e) {logger.error("error occurs when load ae class: " + className, e);}
        return null;
    }
    
    private static void response(String server, FjDscpMessage req, Object args) {
        FjDscpMessage rsp = new FjDscpMessage();
        rsp.json().put("fs",   server);
        rsp.json().put("ts",   req.fs());
        rsp.json().put("sid",  req.sid());
        rsp.json().put("inst", req.inst());
        rsp.json().put("args", args);
        FjServerToolkit.getAnySender().send(rsp);
        logger.info("response message: " + rsp);
    }
}
