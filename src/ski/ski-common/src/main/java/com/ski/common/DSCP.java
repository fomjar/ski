package com.ski.common;

public final class DSCP {
	
	public static final class CMD {
		
		public static final int TAOBAO_ORDER_LIST_NEW               = 0x00001000; // 淘宝订单查询
		public static final int TAOBAO_ORDER_PROC_NEW               = 0x00001001; // 淘宝订单处理
		public static final int TAOBAO_ORDER_DELIVER                = 0x00001002; // 淘宝订单发货
		public static final int PSN_VERIFY                          = 0x00001100; // PSN校验帐户
		public static final int PSN_CHANGE_PASSWORD                 = 0x00001101; // PSN修改密码
		
		public static final int WECHAT_USER_SUBSCRIBE               = 0x00002000; // 微信用户订阅
		public static final int WECHAT_USER_UNSUBSCRIBE             = 0x00002001; // 微信用户取消订阅
		public static final int WECHAT_USER_LOCATION                = 0x00002002; // 微信用户上报位置
		public static final int WECHAT_USER_CLICK                   = 0x00002003; // 微信用户点击菜单按钮
		public static final int WECHAT_USER_VIEW                    = 0x00002004; // 微信用户点击菜单按钮跳转页面
		public static final int WECHAT_SYSTEM_MENU_UPDATE           = 0x00002600; // 微信系统菜单更新
		
		public static final int SYSTEM_UNKNOWN_COMMAND              = 0xFFFFFFFF; // 未知命令
		
	}
	
	public static final class CODE {
		
		public static final int SYSTEM_SUCCESS                      = 0x00000000;
		
		public static final int CDB_DB_STATE_ABNORMAL               = 0x00010000;
		public static final int CDB_CMD_NOT_REGISTERED              = 0x00010001;
		public static final int CDB_EXECUTE_FAILED                  = 0x00010003;
		
		public static final int WA_AE_NOT_FOUND                     = 0x00015001; // 找不到对应的AE对象
		public static final int WA_AE_EXECUTE_FAILED                = 0x00015002; // AE执行失败
		public static final int WA_AE_TAOBAO_ACCOUNT_INCORRECT      = 0x00016001; // 淘宝登陆用户名或密码错误
		public static final int WA_AE_TAOBAO_ORDER_NO_NEW           = 0x00016002; // 没有新的淘宝订单
		public static final int WA_AE_TAOBAO_ORDER_NOT_FOUND        = 0x00016003; // 没有找到指定订单
		public static final int WA_AE_PSN_ACCOUNT_INCORRECT         = 0x00016101; // PSN登陆用户名或密码错误
		public static final int WA_AE_PSN_ACCOUNT_INUSE             = 0x00016102; // PNS账号被占用
		public static final int WA_AE_PSN_CHANGE_PASSWORD_FAILED    = 0x00016103; // PSN修改密码失败
		
		public static final int SYSTEM_ILLEGAL_ARGUMENT             = 0xF0000000;
		public static final int SYSTEM_UNKNOWN_ERROR                = 0xFFFFFFFF;
		
	}

}
