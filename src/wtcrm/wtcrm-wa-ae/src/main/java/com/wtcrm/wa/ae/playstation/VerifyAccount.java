package com.wtcrm.wa.ae.playstation;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.wtcrm.wa.AE;

public class VerifyAccount implements AE {
	
	private int ae_code = -1;
	private JSONArray ae_desc;

	@Override
	public void execute(WebDriver driver, JSONObject ae_arg) {
		driver.get("https://account.sonyentertainmentnetwork.com/login.action");
		if (!ae_arg.containsKey("psna") || !ae_arg.containsKey("psnp")) { // 没有psn账号或密码
			ae_code = 1;
			ae_desc = JSONArray.fromObject("[\"no parameter: psna or psnp\"]");
			return;
		}
		driver.findElement(By.id("signInInput_SignInID")).clear();
		driver.findElement(By.id("signInInput_SignInID")).sendKeys(ae_arg.getString("psna"));
		driver.findElement(By.id("signInInput_Password")).clear();
		driver.findElement(By.id("signInInput_Password")).sendKeys(ae_arg.getString("psna"));
		driver.findElement(By.id("signInButton")).click();
		try {
			driver.findElement(By.id("marketingTextArea"));	// "登陆"字段存在即说明用户名密码错误
			ae_code = 2;
			ae_desc = JSONArray.fromObject("[\"psna or psnp is incorrect\"]");
			return;
		} catch (NoSuchElementException e) {}
		
	}

	@Override
	public int code() {
		return 0;
	}

	@Override
	public JSONArray desc() {
		return null;
	}

}
