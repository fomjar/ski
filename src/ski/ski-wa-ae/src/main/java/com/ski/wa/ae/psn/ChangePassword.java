package com.ski.wa.ae.psn;

import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.ski.wa.AE;

public class ChangePassword implements AE {
	
	private int        code = CODE_UNKNOWN_ERROR;
	private JSONObject desc = null;

	@Override
	public void execute(WebDriver driver, JSONObject arg) {
		if (!arg.containsKey("pass-old") || !arg.containsKey("pass-new")) { // 参数没有新老密码
			code = CODE_ILLEGAL_MESSAGE;
			desc = JSONObject.fromObject("{'error':'no parameter: pass-old or pass-new'}");
			return;
		}
		
		AE login = new Login();
		login.execute(driver, arg);
		if (CODE_SUCCESS != login.code()) {
			code = login.code();
			desc = login.desc();
			return;
		}
		String psnp_old = arg.getString("psnp-old");
		String psnp_new = arg.getString("psnp-new");
		driver.get("https://account.sonyentertainmentnetwork.com/liquid/cam/account/profile/edit-password!input.action");
		try {Thread.sleep(1000L);}
		catch (InterruptedException e) {e.printStackTrace();}
		driver.findElement(By.id("currentPasswordField")).clear();
		driver.findElement(By.id("currentPasswordField")).sendKeys(psnp_old); // 旧密码
		driver.findElement(By.id("changePasswordInput")).clear();
		driver.findElement(By.id("changePasswordInput")).sendKeys(psnp_new);  // 新密码
		driver.findElement(By.id("confirmPasswordField")).clear();
		driver.findElement(By.id("confirmPasswordField")).sendKeys(psnp_new); // 重复新密码
		driver.findElement(By.id("changePasswordButton")).click();
		if (driver.getCurrentUrl().endsWith("passwordSaved")) { // 密码保存成功
			code = CODE_SUCCESS;
			desc = JSONObject.fromObject(null);
		} else { // 密码保存失败
			code = CODE_PSN_CHANGE_PASSWORD_FAILED;
			desc = JSONObject.fromObject(String.format("{'error':'%s'}", driver.findElement(By.id("confirmPasswordFieldError")).getText()));
		}
	}

	@Override
	public int code() {
		return code;
	}

	@Override
	public JSONObject desc() {
		return desc;
	}

}
