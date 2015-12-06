package com.wtcrm.wa.ae.taobao;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.wtcrm.wa.AE;

public class OrderListNew implements AE {
	
//	private static final Logger logger = Logger.getLogger(OrderListNew.class);
	
	private int ae_code = -1;
	private JSONArray ae_desc;

	@Override
	public void execute(WebDriver driver, JSONObject ae_arg) {
		new Login().execute(driver, ae_arg);
		new MySeller().execute(driver, ae_arg);
		driver.findElement(By.linkText("发货")).click();
		try {driver.findElement(By.className("J_TriggerAll")).click();} // 批量发货勾选
		catch (NoSuchElementException e) { // 没有订单
			ae_code = 0;
			ae_desc = null;
			return;
		}
		driver.findElement(By.className("logis:batchSend")).click(); // 批量发货
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
			String toid        = order_table.findElement(By.className("order-number")).getText().split("：")[1].trim();
			String tuid        = order_table.findElement(By.className("ww")).getText().trim();
			String fpid        = order_table.findElement(By.className("des")).findElement(By.className("desc")).getText().split(":")[1].trim();
			String tp_name     = order_table.findElement(By.className("des")).findElement(By.tagName("a")).getText().trim();
			String tp_attr     = order_table.findElement(By.className("attr")).findElement(By.tagName("span")).getText().trim();
			String tp_price    = order_table.findElement(By.className("total")).findElement(By.tagName("span")).getText().trim().split(" ")[0].trim();
			String tp_count    = order_table.findElement(By.className("total")).findElement(By.tagName("em")).getText().trim();
			order.put("buyer-addr", buyer_addr);
			order.put("buyer-zip",  buyer_zip);
			order.put("buyer-name", buyer_name);
			order.put("buyer-tel",  buyer_tel);
			order.put("toid",       toid);
			order.put("tuid",       tuid);
			order.put("fpid",       fpid);
			order.put("tp-name",    tp_name);
			order.put("tp-attr",    tp_attr);
			order.put("tp-price",   tp_price);
			order.put("tp-count",   tp_count);
			orders.add(order);
		}
		ae_code = 0;
		ae_desc = JSONArray.fromObject(orders);
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
