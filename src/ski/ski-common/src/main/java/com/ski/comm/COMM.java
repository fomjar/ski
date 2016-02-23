package com.ski.comm;

/**
 * 通用定义。
 * 
 * @author fomjar
 */
public final class COMM {
    
    /**
     * <p>
     * ISIS - 互联网服务指令集，Internet Service Instruction Set
     * </p>
     * <p>
     * 注意：指令定义必须要符合协议规范
     * </p>
     * 
     * @author fomjar
     */
    public static final class ISIS {
        // 用户指令
        public static final int INST_USER_RESPONSE      = 0x00000100; // 用户响应
        public static final int INST_USER_REQUEST       = 0x00000101; // 用户请求
        public static final int INST_USER_SUBSCRIBE     = 0x00000102; // 用户订阅/关注
        public static final int INST_USER_UNSUBSCRIBE   = 0x00000103; // 用户取消订阅/取消关注
        public static final int INST_USER_GOTO          = 0x00000104; // 用户跳转
        public static final int INST_USER_LOCATION      = 0x00000105; // 用户位置
        // 电商指令
        public static final int INST_ECOM_QUERY_ORDER       = 0x00000300; // 查询订单
        public static final int INST_ECOM_QUERY_RETURN      = 0x00000301; // 查询退货
        public static final int INST_ECOM_APPLY_ORDER       = 0x00000400; // 申请订单
        public static final int INST_ECOM_APPLY_RETURN      = 0x00000401; // 申请退货
        public static final int INST_ECOM_FINISH_ORDER      = 0x00000500; // 完成订单
        public static final int INST_ECOM_FINISH_RETURN     = 0x00000501; // 完成退货
        public static final int INST_ECOM_VERIFY_ACCOUNT    = 0x00000600; // 验证账户
        public static final int INST_ECOM_UPDATE_ACCOUNT    = 0x00000600; // 更新账户
        // 系统指令
        public static final int INST_SYS_HEART_BEAT     = 0xF0000000; // 心跳
        public static final int INST_SYS_UNKNOWN_INST   = 0xFFFFFFFF; // 未知命令
    }
    
    /**
     * 结果码定义
     * 
     * @author fomjar
     */
    public static final class CODE {
        
        // 系统错误码
        public static final int ERROR_SYSTEM_UNKNOWN_ERROR              = 0xFFFFFFFF; // 未知错误
        public static final int ERROR_SYSTEM_SUCCESS                    = 0x00000000; // 成功
        public static final int ERROR_SYSTEM_ILLEGAL_INSTRUCTION            = 0x00000002; // 非法指令
        public static final int ERROR_SYSTEM_ILLEGAL_ARGUMENT           = 0x00000003; // 非法参数
        // DB
        public static final int ERROR_DB_STATE_ABNORMAL                 = 0x00000100; // 数据库状态异常
        public static final int ERROR_DB_OPERATE_FAILED                 = 0x00000101; // 数据库操作失败
        public static final int ERROR_DB_INTERNAL_ERROR                 = 0x00000102; // 数据库内部错误
        // WEB
        public static final int ERROR_WEB_AE_NOT_FOUND                  = 0x00002000; // 找不到对应的AE对象
        public static final int ERROR_WEB_AE_EXECUTE_FAILED             = 0x00002001; // AE执行失败
        public static final int ERROR_WEB_TAOBAO_ACCOUNT_INCORRECT      = 0x00002010; // 淘宝登陆用户名或密码错误
        public static final int ERROR_WEB_TAOBAO_ORDER_NO_NEW           = 0x00002011; // 没有新的淘宝订单
        public static final int ERROR_WEB_TAOBAO_ORDER_NOT_FOUND        = 0x00002012; // 没有找到指定订单
        public static final int ERROR_WEB_PSN_ACCOUNT_INCORRECT         = 0x00002030; // PSN登陆用户名或密码错误
        public static final int ERROR_WEB_PSN_ACCOUNT_INUSE             = 0x00002031; // PNS账号被占用
        public static final int ERROR_WEB_PSN_CHANGE_PASSWORD_FAILED    = 0x00002032; // PSN修改密码失败
    }

}
