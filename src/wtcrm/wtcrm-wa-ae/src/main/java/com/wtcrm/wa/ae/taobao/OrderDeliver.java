package com.wtcrm.wa.ae.taobao;

import java.util.List;
import java.util.NoSuchElementException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.wtcrm.wa.AE;

public class OrderDeliver implements AE{
	
	private int ae_code = CODE_UNKNOWN_ERROR;
	private JSONArray ae_desc;

	@Override
	public void execute(WebDriver driver, JSONObject ae_arg) {
		if (!ae_arg.containsKey("toid")) { // 参数没有订单ID
			ae_code = CODE_INCORRECT_ARGUMENT;
			ae_desc = JSONArray.fromObject("[\"no parameter: toid\"]");
			return;
		}
		
		AE login = new Login();
		login.execute(driver, ae_arg);
		if (CODE_SUCCESS != login.code()) {
			ae_code = login.code();
			ae_desc = login.desc();
			return;
		}
		driver.get("https://myseller.taobao.com/seller_admin.htm");
		driver.findElement(By.linkText("发货")).click();
		List<WebElement> order_tables = null;
		try {order_tables = driver.findElements(By.className("j_expressTbody"));}
		catch (NoSuchElementException e) { // 没有任何订单
			ae_code = CODE_TAOBAO_ORDER_NOT_FOUND;
			ae_desc = JSONArray.fromObject("[\"can not find any orders\"]");
			return;
		}
		String toid = ae_arg.getString("toid");
		WebElement deliver = null;
		for (WebElement order_table : order_tables) {
			String order_id = order_table.findElement(By.className("order-number")).getText().split("：")[1].trim();
			if (toid.equals(order_id)) {
				deliver = order_table.findElement(By.linkText("发货"));
				break;
			}
		}
		if (null == deliver) { // 没有找到对应订单
			ae_code = CODE_TAOBAO_ORDER_NOT_FOUND;
			ae_desc = JSONArray.fromObject("[\"can not find such an order: " + toid + "\"]");
			return;
		}
		deliver.click(); // 发货
		driver.findElement(By.id("dummyTab")).findElement(By.tagName("a")).click(); // 无需物流
		driver.findElement(By.id("logis:noLogis")).click(); // 确认
		ae_code = CODE_SUCCESS;
		ae_desc = null;
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
