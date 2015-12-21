package com.wtcrm.wa.ae.psn;

import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.wtcrm.wa.AE;

public class Verify implements AE {
	
	private int        code = CODE_UNKNOWN_ERROR;
	private JSONObject desc = null;

	@Override
	public void execute(WebDriver driver, JSONObject arg) {
		AE login = new Login();
		login.execute(driver, arg);
		if (CODE_SUCCESS != login.code()) {
			code = login.code();
			desc = login.desc();
			return;
		}
		driver.get("https://account.sonyentertainmentnetwork.com/liquid/cam/devices/device-list.action?category=psn&displayNavigation=false"); // 设备->PlayStation系统
		try {Thread.sleep(1000L);}
		catch (InterruptedException e) {e.printStackTrace();}
		try {
			driver.findElement(By.id("device-0")); // 存在设备绑定
			code = CODE_PSN_ACCOUNT_INUSE;
			desc = JSONObject.fromObject("{'error':'psn account is inuse'}");
		} catch (NoSuchElementException e) { // 不存在设备绑定
			code = CODE_SUCCESS;
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
