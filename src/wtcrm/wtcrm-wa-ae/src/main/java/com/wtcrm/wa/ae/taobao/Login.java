package com.wtcrm.wa.ae.taobao;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.wtcrm.wa.AE;

import fomjar.server.FjToolkit;

public class Login implements AE {
	
	private int ae_code = CODE_UNKNOWN_ERROR;
	private JSONArray ae_desc;

	@Override
	public void execute(WebDriver driver, JSONObject ae_arg) {
		driver.get("https://login.taobao.com/member/login.jhtml");
		driver.findElement(By.id("J_Quick2Static")).click(); // 账户密码登陆
		WebElement we = driver.findElement(By.id("TPL_username_1"));
		if ("text".equals(we.getAttribute("type"))) { // 用户名需要输入
			driver.findElement(By.id("TPL_username_1")).clear(); // 用户名
			driver.findElement(By.id("TPL_username_1")).sendKeys(FjToolkit.getServerConfig("wa.taobao.account"));
		}
		driver.findElement(By.id("TPL_password_1")).clear(); // 密码
		driver.findElement(By.id("TPL_password_1")).sendKeys(FjToolkit.getServerConfig("wa.taobao.password"));
		driver.findElement(By.id("J_SubmitStatic")).click(); // 登陆
		try {
			driver.findElement(By.id("TPL_username_1")); // 存在账号输入框即说明用户名或密码错误
			ae_code = CODE_TAOBAO_LOGIN_ACCOUNT_INCORRECT;
			ae_desc = JSONArray.fromObject("[\"username or password is incorrect\"]");
			return;
		} catch (NoSuchElementException e){}
		ae_code = CODE_SUCCESS;
		ae_desc = null;
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
