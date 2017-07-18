package com.ski.vcg.wa.ae.psn;

import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ski.vcg.common.CommonDefinition;
import com.ski.vcg.wa.AE;

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
        try {   // 发现验证码
            driver.findElement(By.id("captchaContainer"));
            try {Thread.sleep(1000L * 15);} // 等待输入验证码
            catch (InterruptedException e1) {e1.printStackTrace();}
        } catch (NoSuchElementException e) {}
        driver.findElement(By.id("signInButton")).click(); // 登陆
        try {
            // 电子邮件地址或密码不正确。
            // 如需登录，必须提供您的帐户信息更新。一封含有说明的 Email 已发送给 q0266@vcg.pub。您最多可能需要 24 小时收到该邮件。
            WebElement element = driver.findElement(By.id("errorDivMessage"));
            desc = element.getText();
            if (desc.contains("不正确"))   code = CommonDefinition.CODE.CODE_WEB_PSN_USER_OR_PASS_INCORRECT;
            else                        code = CommonDefinition.CODE.CODE_WEB_PSN_ACCOUNT_STATE_ABNORMAL;
            return;
        } catch (NoSuchElementException e) {}
        try {
            //
            driver.findElement(By.id("signInInput_SignInID"));
            code = CommonDefinition.CODE.CODE_WEB_PSN_ACCOUNT_STATE_ABNORMAL;
            desc = "psn web site abnormal";
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
