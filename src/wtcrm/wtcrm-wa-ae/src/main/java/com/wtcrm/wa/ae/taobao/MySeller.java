package com.wtcrm.wa.ae.taobao;

import org.openqa.selenium.WebDriver;

import com.wtcrm.wa.AE;

import fomjar.server.FjJsonMsg;

public class MySeller implements AE {

	@Override
	public void execute(WebDriver driver) {
		driver.get("https://myseller.taobao.com/seller_admin.htm");
	}

	@Override
	public FjJsonMsg getResponse() {
		return null;
	}

}
