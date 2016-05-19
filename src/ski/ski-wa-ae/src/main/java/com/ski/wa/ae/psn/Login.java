package com.ski.wa.ae.psn;

import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.ski.common.SkiCommon;
import com.ski.wa.AE;

public class Login implements AE {
    
    private int     code = SkiCommon.CODE.CODE_SYS_UNKNOWN_ERROR;
    private String  desc = null;

    @Override
    public void execute(WebDriver driver, JSONObject args) {
        if (!args.containsKey("user") || !args.containsKey("pass")) { // 没有账号或密码
            code = SkiCommon.CODE.CODE_SYS_ILLEGAL_ARGS;
            desc = "no parameter: user or pass";
            return;
        }
//        driver.get("https://account.sonyentertainmentnetwork.com/login.action");
//        try {Thread.sleep(1000L);}
//        catch (InterruptedException e) {e.printStackTrace();}
        try {
            driver.findElement(By.id("notYouLink")).click();
            try {Thread.sleep(1000L);}
            catch (InterruptedException e) {e.printStackTrace();}
        } catch (NoSuchElementException e){
        }

        driver.findElement(By.id("signInInput_SignInID")).clear();
        driver.findElement(By.id("signInInput_SignInID")).sendKeys(args.getString("user")); // 账号
        driver.findElement(By.id("signInInput_Password")).clear();
        driver.findElement(By.id("signInInput_Password")).sendKeys(args.getString("pass")); // 密码
        driver.findElement(By.id("signInButton")).click(); // 登陆
        try {Thread.sleep(1000L);}
        catch (InterruptedException e) {e.printStackTrace();}
        try {
            driver.findElement(By.id("signInInput_SignInID"));    // 账号输入框存在即说明用户名密码错误
            code = SkiCommon.CODE.CODE_WEB_PSN_ACCOUNT_INCORRECT;
            desc = "user or pass is incorrect";
            return;
        } catch (NoSuchElementException e) {}
        code = SkiCommon.CODE.CODE_SYS_SUCCESS;
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
