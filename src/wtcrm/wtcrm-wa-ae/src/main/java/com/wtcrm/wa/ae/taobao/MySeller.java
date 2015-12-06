package com.wtcrm.wa.ae.taobao;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.openqa.selenium.WebDriver;

import com.wtcrm.wa.AE;

public class MySeller implements AE {

	@Override
	public void execute(WebDriver driver, JSONObject ae_arg) {
		driver.get("https://myseller.taobao.com/seller_admin.htm");
	}

	@Override
	public int code() {
		return CODE_SUCCESS;
	}

	@Override
	public JSONArray desc() {
		return null;
	}

}
