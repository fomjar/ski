package com.ski.wa.ae.psn;

import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.ski.common.DSCP;
import com.ski.wa.AE;

public class Login implements AE {
    
    private int        cmd = DSCP.CMD.ERROR_SYSTEM_UNKNOWN_ERROR;
    private JSONObject arg = null;

    @Override
    public void execute(WebDriver driver, JSONObject arg) {
        if (!arg.containsKey("user") || !arg.containsKey("pass")) { // 没有账号或密码
            cmd = DSCP.CMD.ERROR_SYSTEM_ILLEGAL_ARGUMENT;
            this.arg = JSONObject.fromObject("{'error':'no parameter: user or pass'}");
            return;
        }
        driver.get("https://account.sonyentertainmentnetwork.com/login.action");
        try {Thread.sleep(1000L);}
        catch (InterruptedException e) {e.printStackTrace();}
        driver.findElement(By.id("signInInput_SignInID")).clear();
        driver.findElement(By.id("signInInput_SignInID")).sendKeys(arg.getString("user")); // 账号
        driver.findElement(By.id("signInInput_Password")).clear();
        driver.findElement(By.id("signInInput_Password")).sendKeys(arg.getString("pass")); // 密码
        driver.findElement(By.id("signInButton")).click(); // 登陆
        try {Thread.sleep(1000L);}
        catch (InterruptedException e) {e.printStackTrace();}
        try {
            driver.findElement(By.id("signInInput_SignInID"));    // 账号输入框存在即说明用户名密码错误
            cmd = DSCP.CMD.ERROR_WEB_PSN_ACCOUNT_INCORRECT;
            this.arg = JSONObject.fromObject("{'error':'user or pass is incorrect'}");
            return;
        } catch (NoSuchElementException e) {}
        cmd = DSCP.CMD.ERROR_SYSTEM_SUCCESS;
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
