package com.ski.wa.ae.psn;

import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.ski.common.DSCP;
import com.ski.wa.AE;

public class Verify implements AE {
    
    private int        cmd = DSCP.CMD.ERROR_SYSTEM_UNKNOWN_ERROR;
    private JSONObject arg = null;

    @Override
    public void execute(WebDriver driver, JSONObject arg) {
        AE login = new Login();
        login.execute(driver, arg);
        if (DSCP.CMD.ERROR_SYSTEM_SUCCESS != login.cmd()) {
            cmd = login.cmd();
            this.arg = login.arg();
            return;
        }
        driver.get("https://account.sonyentertainmentnetwork.com/liquid/cam/devices/device-list.action?category=psn&displayNavigation=false"); // 设备->PlayStation系统
        try {Thread.sleep(1000L);}
        catch (InterruptedException e) {e.printStackTrace();}
        try {
            driver.findElement(By.id("device-0")); // 存在设备绑定
            cmd = DSCP.CMD.ERROR_WEB_PSN_ACCOUNT_INUSE;
            this.arg = JSONObject.fromObject("{'error':'psn account is inuse'}");
        } catch (NoSuchElementException e) { // 不存在设备绑定
            cmd = DSCP.CMD.ERROR_SYSTEM_SUCCESS;
        }
    }

    @Override
    public int cmd() {
        return cmd;
    }

    @Override
    public JSONObject arg() {
        return arg;
    }

}
