package com.ski.wa.ae.taobao;

import java.util.List;
import java.util.NoSuchElementException;

import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.ski.wa.AE;

public class OrderDeliver implements AE{
	
	private int        code = CODE_UNKNOWN_ERROR;
	private JSONObject desc = null;

	@Override
	public void execute(WebDriver driver, JSONObject arg) {
		if (!arg.containsKey("toid")) { // 参数没有订单ID
			code = CODE_ILLEGAL_MESSAGE;
			desc = JSONObject.fromObject("{'error':'no parameter: toid'}");
			return;
		}
		
		AE login = new Login();
		login.execute(driver, arg);
		if (CODE_SUCCESS != login.code()) {
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
		List<WebElement> order_tables = null;
		try {order_tables = driver.findElements(By.className("j_expressTbody"));}
		catch (NoSuchElementException e) { // 没有任何订单
			code = CODE_TAOBAO_ORDER_NOT_FOUND;
			desc = JSONObject.fromObject("{'error':'can not find any orders'}");
			return;
		}
		String toid = arg.getString("toid");
		WebElement deliver = null;
		for (WebElement order_table : order_tables) {
			String order_id = order_table.findElement(By.className("order-number")).getText().split("：")[1].trim();
			if (toid.equals(order_id)) {
				deliver = order_table.findElement(By.linkText("发货"));
				break;
			}
		}
		if (null == deliver) { // 没有找到对应订单
			code = CODE_TAOBAO_ORDER_NOT_FOUND;
			desc = JSONObject.fromObject(String.format("{'error':'can not find such an order: %s'}", toid));
			return;
		}
		deliver.click(); // 发货
		try {Thread.sleep(1000L);}
		catch (InterruptedException e) {e.printStackTrace();}
		driver.findElement(By.id("dummyTab")).findElement(By.tagName("a")).click(); // 无需物流
		driver.findElement(By.id("logis:noLogis")).click(); // 确认
		code = CODE_SUCCESS;
		desc = JSONObject.fromObject(null);
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
