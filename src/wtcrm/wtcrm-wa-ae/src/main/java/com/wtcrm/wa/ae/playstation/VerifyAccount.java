package com.wtcrm.wa.ae.playstation;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.openqa.selenium.WebDriver;

import com.wtcrm.wa.AE;

public class VerifyAccount implements AE {

	@Override
	public void execute(WebDriver driver, JSONObject ae_arg) {
		driver.get("https://account.sonyentertainmentnetwork.com/login.action");
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
