package com.wtcrm.wa.ae.psn;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.wtcrm.wa.AE;

public class Verify implements AE {
	
	private int ae_code = CODE_UNKNOWN_ERROR;
	private JSONArray ae_desc;

	@Override
	public void execute(WebDriver driver, JSONObject ae_arg) {
		AE login = new Login();
		login.execute(driver, ae_arg);
		if (CODE_SUCCESS != login.code()) {
			ae_code = login.code();
			ae_desc = login.desc();
			return;
		}
		driver.get("https://account.sonyentertainmentnetwork.com/liquid/cam/devices/device-list.action?category=psn&displayNavigation=false"); // 设备->PlayStation系统
		try {
			driver.findElement(By.id("device-0")); // 存在设备绑定
			ae_code = CODE_PSN_ACCOUNT_INUSE;
			ae_desc = JSONArray.fromObject("[\"psn account is inuse\"]");
		} catch (NoSuchElementException e) { // 不存在设备绑定
			ae_code = CODE_SUCCESS;
			ae_desc = JSONArray.fromObject(null);
		}
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
