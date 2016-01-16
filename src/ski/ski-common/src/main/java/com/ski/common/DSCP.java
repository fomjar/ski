package com.ski.common;

public final class DSCP {
	
	public static final class CMD {
		
		//////////////////////////// 指令字 ////////////////////////////
		// 用户指令
		public static final int USER_RESPONSE             = 0x00000100; // 用户响应
		public static final int USER_REQUEST              = 0x00000101; // 用户请求
		public static final int USER_SUBSCRIBE            = 0x00000102; // 用户订阅/关注
		public static final int USER_UNSUBSCRIBE          = 0x00000103; // 用户取消订阅/取消关注
		public static final int USER_GOTO                 = 0x00000104; // 用户跳转
		public static final int USER_LOCATION             = 0x00000105; // 用户位置
		// 电商指令
		// APPLY
		public static final int ECOM_APPLY_ORDER          = 0x00000300; // 申请订单
		public static final int ECOM_APPLY_RETURN         = 0x00000301; // 申请退货
		// SPECIFY
		public static final int ECOM_SPECIFY_ORDER        = 0x00000400; // 指定订单
		public static final int ECOM_SPECIFY_RETURN       = 0x00000401; // 指定退货
		// FINISH
		public static final int ECOM_FINISH_ORDER         = 0x00000500; // 完成订单
		public static final int ECOM_FINISH_RETURN        = 0x00000501; // 完成退货
		// 系统指令
		public static final int SYSTEM_UNKNOWN_COMMAND    = 0x0FFFFFFF; // 未知命令
		
		//////////////////////////// 错误码 ////////////////////////////
		// 系统错误码
		public static final int ERROR_SYSTEM_UNKNOWN_ERROR            = 0xF0000000; // 未知错误
		public static final int ERROR_SYSTEM_SUCCESS                  = 0xF0000001; // 成功
		public static final int ERROR_SYSTEM_ILLEGAL_COMMAND          = 0xF0000002; // 非法指令
		public static final int ERROR_SYSTEM_ILLEGAL_ARGUMENT         = 0xF0000003; // 非法参数
		// DB
		public static final int ERROR_DB_STATE_ABNORMAL               = 0xF0000100; // 数据库状态异常
		public static final int ERROR_DB_OPERATE_FAILED               = 0xF0000101; // 数据库操作失败
		// WEB
		public static final int ERROR_WEB_AE_NOT_FOUND                = 0xF0002000; // 找不到对应的AE对象
		public static final int ERROR_WEB_AE_EXECUTE_FAILED           = 0xF0002001; // AE执行失败
		public static final int ERROR_WEB_TAOBAO_ACCOUNT_INCORRECT    = 0xF0002010; // 淘宝登陆用户名或密码错误
		public static final int ERROR_WEB_TAOBAO_ORDER_NO_NEW         = 0xF0002011; // 没有新的淘宝订单
		public static final int ERROR_WEB_TAOBAO_ORDER_NOT_FOUND      = 0xF0002012; // 没有找到指定订单
		public static final int ERROR_WEB_PSN_ACCOUNT_INCORRECT       = 0xF0002030; // PSN登陆用户名或密码错误
		public static final int ERROR_WEB_PSN_ACCOUNT_INUSE           = 0xF0002031; // PNS账号被占用
		public static final int ERROR_WEB_PSN_CHANGE_PASSWORD_FAILED  = 0xF0002032; // PSN修改密码失败
	}

}
