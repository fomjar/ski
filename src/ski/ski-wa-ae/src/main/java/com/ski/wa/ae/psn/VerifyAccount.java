package com.ski.wa.ae.psn;

import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.ski.common.SkiCommon;
import com.ski.wa.AE;

public class VerifyAccount implements AE {
    
    private int     code = SkiCommon.CODE.CODE_SYS_UNKNOWN_ERROR;
    private String  desc = null;

    @Override
    public void execute(WebDriver driver, JSONObject args) {
        AE login = new Login();
        login.execute(driver, args);
        if (SkiCommon.CODE.CODE_SYS_SUCCESS != login.code()) {
            code = login.code();
            desc = login.desc();
            return;
        }
        driver.get("https://account.sonyentertainmentnetwork.com/liquid/cam/devices/device-list.action?category=psn&displayNavigation=false"); // 设备->PlayStation系统
        try {Thread.sleep(1000L);}
        catch (InterruptedException e) {e.printStackTrace();}
        try {
            driver.findElement(By.id("device-0")); // 存在设备绑定
            code = SkiCommon.CODE.CODE_SYS_SUCCESS;
            desc = "psn account is binded";
        } catch (NoSuchElementException e) { // 不存在设备绑定
            code = SkiCommon.CODE.CODE_SYS_SUCCESS;
            desc = "psn account is unbinded";
        }
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String desc() {
        return desc;
    }

}
