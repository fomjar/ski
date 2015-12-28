package com.ski.wa.ae.taobao;

import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ski.common.DSCP;
import com.ski.wa.AE;

public class Login implements AE {
	
	private int        code = DSCP.CODE.SYSTEM_UNKNOWN_ERROR;
	private JSONObject desc = null;

	@Override
	public void execute(WebDriver driver, JSONObject arg) {
		driver.get("https://login.taobao.com/member/login.jhtml");
		try {Thread.sleep(1000L);}
		catch (InterruptedException e) {e.printStackTrace();}
		// try {driver.findElement(By.id("J_Quick2Static")).click();} catch (NoSuchElementException e){} // 账户密码登陆
		WebElement username = driver.findElement(By.id("TPL_username_1"));
		if ("text".equals(username.getAttribute("type"))) { // 用户名需要输入
			username.clear(); // 用户名
			username.sendKeys(arg.getString("user"));
		}
		WebElement password = driver.findElement(By.id("TPL_password_1"));
		password.clear(); // 密码
		password.sendKeys(arg.getString("pass"));
		driver.findElement(By.id("J_SubmitStatic")).click(); // 登陆
		try {Thread.sleep(1000L);}
		catch (InterruptedException e) {e.printStackTrace();}
		try {
			driver.findElement(By.id("TPL_username_1"));
			code = DSCP.CODE.WA_AE_TAOBAO_ACCOUNT_INCORRECT;
			desc = JSONObject.fromObject("{'error':'username or password is incorrect'}");
			return;
		} catch (NoSuchElementException e){
			code = DSCP.CODE.SYSTEM_SUCCESS;
			desc = JSONObject.fromObject(null);
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
