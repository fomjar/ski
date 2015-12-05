package com.wtcrm.wa.ae.taobao;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.wtcrm.wa.AE;

import fomjar.server.FjJsonMsg;

public class OrderListNew implements AE {
	
	private static final Logger logger = Logger.getLogger(OrderListNew.class);
	
	private FjJsonMsg rsp;

	@Override
	public void execute(WebDriver driver) {
		new Login().execute(driver);
		new MySeller().execute(driver);
		driver.findElement(By.linkText("发货")).click();
		driver.findElement(By.className("J_TriggerAll")).click();
		driver.findElement(By.className("logis:batchSend")).click();
		List<WebElement> order_tables = driver.findElements(By.className("consign-detail"));
		List<Map<String, String>> orders = new LinkedList<Map<String,String>>();
		String[] currentBuyerInfo = null;
		for (WebElement order_table : order_tables) {
			Map<String, String> order = new HashMap<String, String>();
			String buyerInfo   = order_table.findElement(By.tagName("span")).getText().trim();
			if (0 != buyerInfo.length()) currentBuyerInfo = buyerInfo.split("，");
			String buyer_addr  = currentBuyerInfo[0].trim();
			String buyer_zip   = currentBuyerInfo[1].trim();
			String buyer_name  = currentBuyerInfo[2].trim();
			String buyer_tel   = currentBuyerInfo[3].trim();
			String toid        = order_table.findElement(By.className("order-number")).getText().split("：")[1];
			String tuid        = order_table.findElement(By.className("ww")).getText();
			String fpid        = order_table.findElement(By.className("des")).findElement(By.className("desc")).getText();
			String tp_name     = order_table.findElement(By.className("des")).findElement(By.tagName("a")).getText();
			String tp_attr     = order_table.findElement(By.className("attr")).findElement(By.tagName("span")).getText();
			String tp_count    = order_table.findElement(By.className("total")).findElement(By.tagName("em")).getText();
			order.put("buyer-addr", buyer_addr);
			order.put("buyer-zip",  buyer_zip);
			order.put("buyer-name", buyer_name);
			order.put("buyer-tel",  buyer_tel);
			order.put("toid",       toid);
			order.put("tuid",       tuid);
			order.put("fpid",       fpid);
			order.put("tp-name",    tp_name);
			order.put("tp-attr",    tp_attr);
			order.put("tp-count",   tp_count);
			orders.add(order);
		}
		rsp = new FjJsonMsg("{\"ae-code\":0, \"ae-desc\":" + JSONArray.fromObject(orders) + "}");
	}

	@Override
	public FjJsonMsg getResponse() {
		logger.error(rsp);
		return rsp;
	}

}
