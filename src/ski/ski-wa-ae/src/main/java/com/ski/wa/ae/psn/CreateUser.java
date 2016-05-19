package com.ski.wa.ae.psn;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;

import com.ski.common.SkiCommon;
import com.ski.wa.AE;

import net.sf.json.JSONObject;

public class CreateUser implements AE {
    
    private int     code = SkiCommon.CODE.CODE_SYS_UNKNOWN_ERROR;
    private String  desc = null;

    @Override
    public void execute(WebDriver driver, JSONObject args) {
        if (!args.containsKey("email") || !args.containsKey("pass") || !args.containsKey("year") || !args.containsKey("month") || !args.containsKey("day")) { // 没有账号或密码
            code = SkiCommon.CODE.CODE_SYS_ILLEGAL_ARGS;
            desc = "no parameter: email or birth or pass";
            return;
        }
        driver.get("https://account.sonyentertainmentnetwork.com/liquid/reg/account/create-account!input.action?request_locale=zh_HK");
        try {Thread.sleep(1000L);}
        catch (InterruptedException e) {e.printStackTrace();}
        driver.findElement(By.id("account_loginNameFieldInput")).sendKeys(args.getString("email")); // 账号
        driver.findElement(By.id("account_password")).click();
        driver.findElement(By.name("account.password")).sendKeys(args.getString("pass")); // 密码
        driver.findElement(By.id("confirmPasswordField")).sendKeys(args.getString("pass")); // 重新输入密码
        new Select(driver.findElement(By.id("yearDropDown"))).selectByValue(args.getString("year")); // 生日年份
        new Select(driver.findElement(By.id("monthDropDown"))).selectByValue(args.getString("month")); // 生日月份
        new Select(driver.findElement(By.id("dayDropDown"))).selectByValue(args.getString("day")); // 生日月份
        driver.findElement(By.id("regInput_MaleGender")).click();		// 选择性别
        new Select(driver.findElement(By.id("account_address_provinceField"))).selectByVisibleText("香港"); // 地址       
        driver.findElement(By.id("createAccountButton")).click();  // 注册按钮点击

        if (driver.getCurrentUrl().endsWith("passwordSaved")) { // 密码保存成功
            code = SkiCommon.CODE.CODE_SYS_SUCCESS;
        } else { // 密码保存失败
            code = SkiCommon.CODE.CODE_WEB_PSN_CHANGE_PASSWORD_FAILED;
            desc = driver.findElement(By.id("confirmPasswordFieldError")).getText();
        }
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
