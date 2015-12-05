package com.wtcrm.wa.ae.taobao;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.wtcrm.wa.AE;

import fomjar.server.FjJsonMsg;
import fomjar.server.FjToolkit;

public class Login implements AE {

	@Override
	public void execute(WebDriver driver) {
		driver.get("https://login.taobao.com/member/login.jhtml");
		driver.findElement(By.id("J_Quick2Static")).click(); // 账户密码登陆
		WebElement we = driver.findElement(By.id("TPL_username_1"));
		if ("text".equals(we.getAttribute("type"))) {
			driver.findElement(By.id("TPL_username_1")).clear(); // 用户名
			driver.findElement(By.id("TPL_username_1")).sendKeys(FjToolkit.getServerConfig("wa.taobao.account"));
		}
		driver.findElement(By.id("TPL_password_1")).clear(); // 密码
		driver.findElement(By.id("TPL_password_1")).sendKeys(FjToolkit.getServerConfig("wa.taobao.password"));
		driver.findElement(By.id("J_SubmitStatic")).click(); // 登陆
	}

	@Override
	public FjJsonMsg getResponse() {
		return new FjJsonMsg("{retcode:0}");
	}

}
