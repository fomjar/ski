package com.ski.wa.ae.taobao;

import java.util.List;
import java.util.NoSuchElementException;

import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ski.common.DSCP;
import com.ski.wa.AE;

public class OrderListNew implements AE {
	
//	private static final Logger logger = Logger.getLogger(OrderListNew.class);
	
	private int        code = DSCP.CODE.SYSTEM_UNKNOWN_ERROR;
	private JSONObject desc = null;

	@Override
	public void execute(WebDriver driver, JSONObject arg) {
		AE login = new Login();
		login.execute(driver, arg);
		if (DSCP.CODE.SYSTEM_SUCCESS != login.code()) {
			code = login.code();
			desc = login.desc();
			return;
		}
		driver.get("https://myseller.taobao.com/seller_admin.htm");
		try {Thread.sleep(1000L);}
		catch (InterruptedException e) {e.printStackTrace();}
		driver.findElement(By.linkText("发货")).click();
		try {Thread.sleep(1000L);}
		catch (InterruptedException e) {e.printStackTrace();}
		try {driver.findElement(By.className("J_TriggerAll")).click();} // 批量发货勾选
		catch (NoSuchElementException e) { // 没有订单
			code = DSCP.CODE.WA_AE_TAOBAO_ORDER_NO_NEW;
			desc = JSONObject.fromObject("{'error':'no new order'}");
			return;
		}
		try {Thread.sleep(1000L);}
		catch (InterruptedException e) {e.printStackTrace();}
		driver.findElement(By.className("logis:batchSend")).click(); // 批量发货
		try {Thread.sleep(1000L);}
		catch (InterruptedException e) {e.printStackTrace();}
		List<WebElement> order_tables = driver.findElements(By.className("consign-detail"));
		String[] currentBuyerInfo = null;
		for (WebElement order_table : order_tables) {
			String tu_info   = order_table.findElement(By.tagName("span")).getText().trim();
			if (0 == tu_info.length()) continue;
			
			currentBuyerInfo = tu_info.split("，");
			String tu_addr  = currentBuyerInfo[0].trim();
			String tu_zip   = currentBuyerInfo[1].trim();
			String tu_name  = currentBuyerInfo[2].trim();
			String tu_tel   = currentBuyerInfo[3].trim();
			String toid     = order_table.findElement(By.className("order-number")).getText().split("：")[1].trim();
			String tuid     = order_table.findElement(By.className("ww")).getText().trim();
			String pid      = order_table.findElement(By.className("des")).findElement(By.className("desc")).getText().split(":")[1].trim();
			String tp_name  = order_table.findElement(By.className("des")).findElement(By.tagName("a")).getText().trim();
			String tp_attr  = order_table.findElement(By.className("attr")).findElement(By.tagName("span")).getText().trim();
			String tp_price = order_table.findElement(By.className("total")).findElement(By.tagName("span")).getText().trim().split(" ")[0].trim();
			String tp_count	= order_table.findElement(By.className("total")).findElement(By.tagName("em")).getText().trim();
			
			desc = new JSONObject();
			desc.put("toid",     toid);
			desc.put("tuid",     tuid);
			desc.put("pid",      pid);
			desc.put("tp-name",  tp_name);
			desc.put("tp-attr",  tp_attr);
			desc.put("tp-price", tp_price);
			desc.put("tp-count", tp_count);
			desc.put("tu-name",  tu_name);
			desc.put("tu-tel",   tu_tel);
			desc.put("tu-addr",  tu_addr);
			desc.put("tu-zip",   tu_zip);
			code = DSCP.CODE.SYSTEM_SUCCESS;
			return;
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
