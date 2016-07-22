package com.ski.wa.ae.psn;

import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ski.common.CommonDefinition;
import com.ski.wa.AE;

public class Login implements AE {
    
    private int     code = CommonDefinition.CODE.CODE_SYS_UNKNOWN_ERROR;
    private String  desc = null;

    @Override
    public void execute(WebDriver driver, JSONObject args) {
        if (!args.containsKey("user") || !args.containsKey("pass")) { // 没有账号或密码
            code = CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS;
            desc = "no parameter: user or pass";
            return;
        }
        driver.get("https://account.sonyentertainmentnetwork.com/login.action");
        driver.findElement(By.id("signInInput_SignInID")).clear();
        driver.findElement(By.id("signInInput_SignInID")).sendKeys(args.getString("user")); // 账号
        driver.findElement(By.id("signInInput_Password")).clear();
        driver.findElement(By.id("signInInput_Password")).sendKeys(args.getString("pass")); // 密码
        driver.findElement(By.id("signInButton")).click(); // 登陆
        try {   // 账号输入框存在即说明用户名密码错误
            driver.findElement(By.id("signInInput_SignInID"));
            code = CommonDefinition.CODE.CODE_WEB_PSN_ACCOUNT_INCORRECT;
            desc = "user or pass is incorrect";
            return;
        } catch (NoSuchElementException e) {}
        try {   // 如需登录，必须提供您的帐户信息更新。一封含有说明的 Email 已发送给 q0266@vcg.pub。您最多可能需要 24 小时收到该邮件。
            WebElement element = driver.findElement(By.id("errorDivMsgDiv"));
            code = CommonDefinition.CODE.CODE_WEB_PSN_ACCOUNT_STATE_ABNORMAL;
            desc = element.getText();
            return;
        } catch (NoSuchElementException e) {}
        try {   // 发现验证码
            driver.findElement(By.id("captchaContainer"));
            code = CommonDefinition.CODE.CODE_SYS_UNAVAILABLE;
            desc = "unable to login for there is verify code";
            return;
        } catch (NoSuchElementException e) {}
        code = CommonDefinition.CODE.CODE_SYS_SUCCESS;
        desc = "login success";
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
