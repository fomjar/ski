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
	
	int       code();
	JSONArray desc();
	
}
