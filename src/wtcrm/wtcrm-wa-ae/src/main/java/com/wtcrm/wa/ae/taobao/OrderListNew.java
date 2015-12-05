package com.wtcrm.wa.ae.taobao;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.wtcrm.wa.AE;

import fomjar.server.FjJsonMsg;

public class OrderListNew implements AE {
	
	private static final Logger logger = Logger.getLogger(OrderListNew.class);

	@Override
	public void execute(WebDriver driver) {
		new Login().execute(driver);
		new MySeller().execute(driver);
		driver.findElement(By.linkText("发货")).click();
		logger.error("order-list-new" + driver.findElement(By.className("order-number")).getText());
	}

	@Override
	public FjJsonMsg getResponse() {
		return null;
	}

}
