package com.wtcrm.wa;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.openqa.selenium.WebDriver;

public interface AE {
	
	/**
	 * 
	 * @param driver
	 * @param arg FjJsonMsg.json().getJSONObject("ae-arg")
	 */
	void execute(WebDriver driver, JSONObject ae_arg);
	
	/**
	 * @return ae执行情况，0表示成功，但不一定有结果返回
	 */
	int       code();
	
	/**
	 * @return ae执行结果，如果有数据返回，则从此处返回
	 */
	JSONArray desc();
	
}
