package com.wtcrm.wa;

import org.openqa.selenium.WebDriver;

import fomjar.server.FjJsonMsg;

public interface AE {
	
	void execute(WebDriver driver);
	
	FjJsonMsg getResponse();

}
