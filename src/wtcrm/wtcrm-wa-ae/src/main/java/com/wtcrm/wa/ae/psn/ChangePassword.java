package com.wtcrm.wa.ae.psn;

import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.wtcrm.wa.AE;

public class ChangePassword implements AE {
	
	private int ae_code = CODE_UNKNOWN_ERROR;
	private JSONObject ae_desc;

	@Override
	public void execute(WebDriver driver, JSONObject ae_arg) {
		if (!ae_arg.containsKey("psnp-old") || !ae_arg.containsKey("psnp-new")) { // 参数没有新老密码
			ae_code = CODE_INCORRECT_ARGUMENT;
			ae_desc = JSONObject.fromObject("{\"ae-err\":\"no parameter: psnp-old or psnp-new\"}");
			return;
		}
		
		AE login = new Login();
		login.execute(driver, ae_arg);
		if (CODE_SUCCESS != login.code()) {
			ae_code = login.code();
			ae_desc = login.desc();
			return;
		}
		String psnp_old = ae_arg.getString("psnp-old");
		String psnp_new = ae_arg.getString("psnp-new");
		driver.get("https://account.sonyentertainmentnetwork.com/liquid/cam/account/profile/edit-password!input.action");
		driver.findElement(By.id("currentPasswordField")).clear();
		driver.findElement(By.id("currentPasswordField")).sendKeys(psnp_old); // 旧密码
		driver.findElement(By.id("changePasswordInput")).clear();
		driver.findElement(By.id("changePasswordInput")).sendKeys(psnp_new);  // 新密码
		driver.findElement(By.id("confirmPasswordField")).clear();
		driver.findElement(By.id("confirmPasswordField")).sendKeys(psnp_new); // 重复新密码
		driver.findElement(By.id("changePasswordButton")).click();
		if (driver.getCurrentUrl().endsWith("passwordSaved")) { // 密码保存成功
			ae_code = CODE_SUCCESS;
			ae_desc = JSONObject.fromObject(null);
		} else { // 密码保存失败
			ae_code = CODE_PSN_CHANGE_PASSWORD_FAILED;
			ae_desc = JSONObject.fromObject("{\"ae-err\":\"" + driver.findElement(By.id("confirmPasswordFieldError")).getText() + "\"}");
		}
	}

	@Override
	public int code() {
		return ae_code;
	}

	@Override
	public JSONObject desc() {
		return ae_desc;
	}

}
