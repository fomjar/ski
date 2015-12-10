package com.wtcrm.wa;

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
	JSONObject desc();
	
	static final int CODE_SUCCESS                        = 0x00000000; // 成功
	static final int CODE_AE_NOT_FOUND                   = 0x00000001; // 找不到对应的AE对象
	static final int CODE_TAOBAO_LOGIN_ACCOUNT_INCORRECT = 0x00001001; // 登陆淘宝用户名或密码错误
	static final int CODE_TAOBAO_ORDER_NO_NEW            = 0x00001002; // 没有新的淘宝订单
	static final int CODE_TAOBAO_ORDER_NOT_FOUND         = 0x00001003; // 没有找到订单
	static final int CODE_PSN_LOGIN_ACCOUNT_INCORRECT    = 0x00002001; // 登陆PSN用户名或密码错误
	static final int CODE_PSN_ACCOUNT_INUSE              = 0x00002002; // 账号被占用
	static final int CODE_PSN_CHANGE_PASSWORD_FAILED     = 0x00002003; // 修改密码失败
	static final int CODE_INCORRECT_ARGUMENT             = 0xfffffffe; // 错误的参数
	static final int CODE_UNKNOWN_ERROR                  = 0xffffffff; // 未知错误
}
