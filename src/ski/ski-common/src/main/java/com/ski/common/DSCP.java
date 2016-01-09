package com.ski.common;

public final class DSCP {
	
	public static final class CMD {
		
		//////////////////////////// 命令字 ////////////////////////////
		// 用户指令
		public static final int USER_RESPONSE                       = 0x00000100; // 用户响应
		public static final int USER_REQUEST                        = 0x00000101; // 用户请求
		public static final int USER_SUBSCRIBE                      = 0x00000102; // 用户订阅/关注
		public static final int USER_UNSUBSCRIBE                    = 0x00000103; // 用户取消订阅/取消关注
		public static final int USER_GOTO                           = 0x00000104; // 用户跳转
		public static final int USER_LOCATION                       = 0x00000105; // 用户位置
		// 电商指令
		public static final int ECOM_ORDER_APPLY                    = 0x00000300; // 订单申请（新的）
		public static final int ECOM_ORDER_QUERY                    = 0x00000301; // 订单查询（老的）
		public static final int ECOM_RETURN_APPLY                   = 0x00000320; // 退货申请
		public static final int ECOM_RETURN_SPECIFY                 = 0x00000321; // 退货指定（具体目标对象）
		// 系统指令
		public static final int SYSTEM_UNKNOWN_COMMAND              = 0x0FFFFFFF; // 未知命令
		
		//////////////////////////// 错误码 ////////////////////////////
		// 系统错误码
		public static final int ERROR_SYSTEM_UNKNOWN_ERROR          = 0xF0000000; // 未知错误
		public static final int ERROR_SYSTEM_SUCCESS                = 0xF0000001; // 成功
		public static final int ERROR_SYSTEM_ILLEGAL_COMMAND        = 0xF0000002; // 非法指令
		public static final int ERROR_SYSTEM_ILLEGAL_ARGUMENT       = 0xF0000003; // 非法参数
		// 数据库错误码
		public static final int ERROR_DB_STATE_ABNORMAL             = 0xF0000100; // 数据库状态异常
		public static final int ERROR_DB_OPERATE_FAILED             = 0xF0000101; // 数据库操作失败
		// WEB错误码
		public static final int ERROR_AE_NOT_FOUND                  = 0xF0000500; // 找不到对应的AE对象
		public static final int ERROR_AE_EXECUTE_FAILED             = 0xF0000501; // AE执行失败
		public static final int ERROR_TAOBAO_ACCOUNT_INCORRECT      = 0xF0000510; // 淘宝登陆用户名或密码错误
		public static final int ERROR_TAOBAO_ORDER_NO_NEW           = 0xF0000511; // 没有新的淘宝订单
		public static final int ERROR_TAOBAO_ORDER_NOT_FOUND        = 0xF0000512; // 没有找到指定订单
		public static final int ERROR_PSN_ACCOUNT_INCORRECT         = 0xF0000530; // PSN登陆用户名或密码错误
		public static final int ERROR_PSN_ACCOUNT_INUSE             = 0xF0000531; // PNS账号被占用
		public static final int ERROR_PSN_CHANGE_PASSWORD_FAILED    = 0xF0000532; // PSN修改密码失败
	}

}
