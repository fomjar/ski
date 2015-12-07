package com.wtcrm.wa.ae.psn;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.wtcrm.wa.AE;

public class Login implements AE {
	
	private int ae_code = CODE_UNKNOWN_ERROR;
	private JSONArray ae_desc;

	@Override
	public void execute(WebDriver driver, JSONObject ae_arg) {
		driver.get("https://account.sonyentertainmentnetwork.com/login.action");
		if (!ae_arg.containsKey("psna") || !ae_arg.containsKey("psnp")) { // 没有psn账号或密码
			ae_code = CODE_INCORRECT_ARGUMENT;
			ae_desc = JSONArray.fromObject("[\"no parameter: psna or psnp\"]");
			return;
		}
		driver.findElement(By.id("signInInput_SignInID")).clear();
		driver.findElement(By.id("signInInput_SignInID")).sendKeys(ae_arg.getString("psna")); // 账号
		driver.findElement(By.id("signInInput_Password")).clear();
		driver.findElement(By.id("signInInput_Password")).sendKeys(ae_arg.getString("psna")); // 密码
		driver.findElement(By.id("signInButton")).click(); // 登陆
		try {Thread.sleep(1000L);}
		catch (InterruptedException e) {e.printStackTrace();}
		try {
			driver.findElement(By.id("signInInput_SignInID"));	// 账号输入框存在即说明用户名密码错误
			ae_code = CODE_PSN_LOGIN_ACCOUNT_INCORRECT;
			ae_desc = JSONArray.fromObject("[\"psna or psnp is incorrect\"]");
			return;
		} catch (NoSuchElementException e) {}
		ae_code = CODE_SUCCESS;
		ae_desc = JSONArray.fromObject(null);
	}

	@Override
	public int code() {
		return ae_code;
	}

	@Override
	public JSONArray desc() {
		return ae_desc;
	}

}
