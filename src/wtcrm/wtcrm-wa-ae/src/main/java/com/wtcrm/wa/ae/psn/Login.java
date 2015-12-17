package com.wtcrm.wa.ae.psn;

import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.wtcrm.wa.AE;

public class Login implements AE {
	
	private int        code = CODE_UNKNOWN_ERROR;
	private JSONObject desc = null;

	@Override
	public void execute(WebDriver driver, JSONObject arg) {
		driver.get("https://account.sonyentertainmentnetwork.com/login.action");
		if (!arg.containsKey("psna") || !arg.containsKey("psnp")) { // 没有psn账号或密码
			code = CODE_ILLEGAL_MESSAGE;
			desc = JSONObject.fromObject("{\"error\":\"no parameter: psna or psnp\"}");
			return;
		}
		driver.findElement(By.id("signInInput_SignInID")).clear();
		driver.findElement(By.id("signInInput_SignInID")).sendKeys(arg.getString("psna")); // 账号
		driver.findElement(By.id("signInInput_Password")).clear();
		driver.findElement(By.id("signInInput_Password")).sendKeys(arg.getString("psna")); // 密码
		driver.findElement(By.id("signInButton")).click(); // 登陆
		try {Thread.sleep(1000L);}
		catch (InterruptedException e) {e.printStackTrace();}
		try {
			driver.findElement(By.id("signInInput_SignInID"));	// 账号输入框存在即说明用户名密码错误
			code = CODE_PSN_LOGIN_ACCOUNT_INCORRECT;
			desc = JSONObject.fromObject("{\"error\":\"psna or psnp is incorrect\"}");
			return;
		} catch (NoSuchElementException e) {}
		code = CODE_SUCCESS;
		desc = JSONObject.fromObject(null);
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
