package com.ski.common;

/**
 * SKI通用定义
 * 
 * @author fomjar
 */
public final class SkiCommon {
    
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
        //////////////////////////////// 用户指令 ////////////////////////////////
        /** 发往用户的响应 */
        public static final int INST_USER_RESPONSE      = 0x00001001;
        /** 来自用户的请求 */
        public static final int INST_USER_REQUEST       = 0x00001002;
        /** 用户订阅/关注 */
        public static final int INST_USER_SUBSCRIBE     = 0x00001003;
        /** 用户取消订阅/取消关注 */
        public static final int INST_USER_UNSUBSCRIBE   = 0x00001004;
        /** 用户跳转界面/网页 */
        public static final int INST_USER_GOTO          = 0x00001005;
        /** 用户地理位置 */
        public static final int INST_USER_LOCATION      = 0x00001006;
        
        //////////////////////////////// 电商指令 ////////////////////////////////
        // QUERY
        /** 查询账户 */
        public static final int INST_ECOM_QUERY_ACCOUNT     = 0x00002001;
        /** 查询订单 */
        public static final int INST_ECOM_QUERY_ORDER       = 0x00002002;
        /** 查询退货单 */
        public static final int INST_ECOM_QUERY_RETURN      = 0x00002003;
        // APPLY
        /** 申请（处理）订单，返回提货单 */
        public static final int INST_ECOM_APPLY_ORDER       = 0x00002101;
        /** 申请发货 */
        public static final int INST_ECOM_APPLY_DELIVER     = 0x00002102;
        /** 申请提货，返回真实产品信息 */
        public static final int INST_ECOM_APPLY_TAKE        = 0x00002103;
        /** 申请退货 */
        public static final int INST_ECOM_APPLY_RETURN      = 0x00002104;
        /** 申请转账，包含对方账户是确认转账，否则只获取对方账户信息并生成转账提示 */
        public static final int INST_ECOM_APPLY_TRANSFER    = 0x00002105;
        // LOCK
        /** 锁定账户，锁定后无法变更数据 */
        public static final int INST_ECOM_LOCK_ACCOUNT      = 0x00002201;
        // VERIFY
        /** 验证账户、密码等的正确性 */
        public static final int INST_ECOM_VERIFY_ACCOUNT    = 0x00002301;
        // UPDATE
        /** 更新账户 */
        public static final int INST_ECOM_UPDATE_ACCOUNT    = 0x00002401;
        /** 更新/创建订单 */
        public static final int INST_ECOM_UPDATE_ORDER      = 0x00002402;
    }
    
    /**
     * 结果码定义
     * 
     * @author fomjar
     */
    public static final class CODE {
        // 系统错误码
        public static final int CODE_SYS_UNKNOWN_ERROR     = 0xFFFFFFFF; // 未知错误
        public static final int CODE_SYS_SUCCESS           = 0x00000000; // 成功
        public static final int CODE_SYS_ILLEGAL_INST      = 0x00000001; // 非法指令
        public static final int CODE_SYS_ILLEGAL_ARGS      = 0x00000002; // 非法参数
        // DB
        public static final int CODE_DB_STATE_ABNORMAL     = 0x00001001; // 数据库状态异常
        public static final int CODE_DB_OPERATE_FAILED     = 0x00001002; // 数据库操作失败
        public static final int CODE_DB_INTERNAL_ERROR     = 0x00001003; // 数据库内部错误
        // WEB
        public static final int CODE_WEB_AE_NOT_FOUND                  = 0x00002001; // 找不到对应的AE对象
        public static final int CODE_WEB_AE_EXECUTE_FAILED             = 0x00002002; // AE执行失败
        public static final int CODE_WEB_TAOBAO_ACCOUNT_INCORRECT      = 0x00002011; // 淘宝登陆用户名或密码错误
        public static final int CODE_WEB_TAOBAO_ORDER_NO_NEW           = 0x00002012; // 没有新的淘宝订单
        public static final int CODE_WEB_TAOBAO_ORDER_NOT_FOUND        = 0x00002013; // 没有找到指定订单
        public static final int CODE_WEB_PSN_ACCOUNT_INCORRECT         = 0x00002031; // PSN登陆用户名或密码错误
        public static final int CODE_WEB_PSN_ACCOUNT_INUSE             = 0x00002032; // PNS账号被占用
        public static final int CODE_WEB_PSN_CHANGE_PASSWORD_FAILED    = 0x00002033; // PSN修改密码失败
    }

}
