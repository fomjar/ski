package com.ski.wa;

import net.sf.json.JSONObject;

import org.openqa.selenium.WebDriver;

public interface AE {
    
    /**
     * 
     * @param driver
     * @param arg fomjar.server.msg.FjDscpMessage.arg()
     */
    void   execute(WebDriver driver, JSONObject arg);
    
    /**
     * @return ae执行情况
     */
    int    code();
    
    /**
     * @return ae执行结果，如果有数据返回，则从此处返回
     */
    String desc();
    
}
