package com.ski.vcg.wa.ae.psn;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.ski.vcg.common.CommonDefinition;
import com.ski.vcg.wa.AE;

import net.sf.json.JSONObject;

public class UpdateAccount implements AE {
    
    private int     code = CommonDefinition.CODE.CODE_SYS_UNKNOWN_ERROR;
    private String  desc = null;

    @Override
    public void execute(WebDriver driver, JSONObject args) {
        if (!args.containsKey("user") || !args.containsKey("pass")) { // 参数没有新老密码
            code = CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS;
            desc = "no parameter: user or pass";
            return;
        }
        
        AE login = new Login();
        JSONObject login_args = new JSONObject();
        login_args.put("user", args.getString("user"));
        login_args.put("pass", args.getString("pass"));
        login.execute(driver, login_args);
        if (CommonDefinition.CODE.CODE_SYS_SUCCESS != login.code()) {
            code = login.code();
            desc = login.desc();
            return;
        }
        
        if (!args.has("pass_new")) {
            code = CommonDefinition.CODE.CODE_SYS_SUCCESS;
            desc = "login success";
            return;
        }
        
        String psnp_old = args.getString("pass");
        String psnp_new = args.getString("pass_new");
        
        driver.get("https://account.sonyentertainmentnetwork.com/liquid/cam/account/profile/edit-password!input.action");
        driver.findElement(By.id("currentPasswordField")).clear();
        driver.findElement(By.id("currentPasswordField")).sendKeys(psnp_old); // 旧密码
        driver.findElement(By.id("changePasswordInput")).click();
        driver.findElement(By.name("password")).sendKeys(psnp_new);  // 新密码
        driver.findElement(By.id("confirmPasswordField")).clear();
        driver.findElement(By.id("confirmPasswordField")).sendKeys(psnp_new); // 重复新密码
        driver.findElement(By.id("changePasswordButton")).click();
        
        if (driver.getCurrentUrl().endsWith("passwordSaved")) { // 密码保存成功
            code = CommonDefinition.CODE.CODE_SYS_SUCCESS;
            desc = "update new password success";
        } else { // 密码保存失败
            code = CommonDefinition.CODE.CODE_WEB_PSN_CHANGE_PASSWORD_FAILED;
            try {desc = driver.findElement(By.id("passwordFieldError")).getText();}
            catch (NoSuchElementException e) {desc = driver.findElement(By.id("confirmPasswordFieldError")).getText();}
        }
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String desc() {
        return desc.toString();
    }

}
