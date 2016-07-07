package com.ski.common;

/**
 * SKI通用定义
 * 
 * @author fomjar
 */
public final class CommonDefinition {
    
    public static final String VERSION = "0.0.2";
    
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
        /** 用户通用命令 */
        public static final int INST_USER_COMMAND       = 0X00001005;
        /** 用户跳转界面/网页 */
        public static final int INST_USER_GOTO          = 0x00001006;
        /** 用户地理位置 */
        public static final int INST_USER_LOCATION      = 0x00001007;
        
        //////////////////////////////// 电商指令 ////////////////////////////////
        // QUERY
        /** 查询游戏 */
        public static final int INST_ECOM_QUERY_GAME                    = 0x00002001;
        /** 查询游戏账号 */
        public static final int INST_ECOM_QUERY_GAME_ACCOUNT            = 0x00002002;
        /** 查询游戏账户下的游戏 */
        public static final int INST_ECOM_QUERY_GAME_ACCOUNT_GAME       = 0x00002003;
        /** 查询游戏账户租赁状态 */
        public static final int INST_ECOM_QUERY_GAME_ACCOUNT_RENT       = 0x00002004;
        /** 查询渠道账号 */
        public static final int INST_ECOM_QUERY_CHANNEL_ACCOUNT         = 0x00002005;
        /** 查询订单 */
        public static final int INST_ECOM_QUERY_ORDER                   = 0x00002006;
        /** 查询订单商品 */
        public static final int INST_ECOM_QUERY_COMMODITY               = 0x00002007;
        /** 查询游戏租赁价格 */
        public static final int INST_ECOM_QUERY_GAME_RENT_PRICE         = 0x00002008;
        /** 查询平台账户 */
        public static final int INST_ECOM_QUERY_PLATFORM_ACCOUNT        = 0x00002009;
        /** 查询平台账户与渠道账户间的映射关系 */
        public static final int INST_ECOM_QUERY_PLATFORM_ACCOUNT_MAP    = 0x0000200A;
        /** 查询平台用户充值记录 */
        public static final int INST_ECOM_QUERY_PLATFORM_ACCOUNT_MONEY  = 0x0000200B;
        /** 查询TAG */
        public static final int INST_ECOM_QUERY_TAG                     = 0x0000200C;
        /** 查询工单 */
        public static final int INST_ECOM_QUERY_TICKET                  = 0x0000200D;
        // APPLY
        /** 验证账户、密码等的正确性 */
        public static final int INST_ECOM_APPLY_GAME_ACCOUNT_VERIFY     = 0x00002101;
        /** 锁定账户，锁定后无法变更数据 */
        public static final int INST_ECOM_APPLY_GAME_ACCOUNT_LOCK       = 0x00002102;
        /** 平台账户合并操作 */
        public static final int INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE  = 0x00002103;
        /** 平台账户充值 */
        public static final int INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY  = 0x00002104;
        /** 申请转账，包含对方账户是确认转账，否则只获取对方账户信息并生成转账提示 */
        public static final int INST_ECOM_APPLY_MONEY_TRANSFER          = 0x00002105;
        // UPDATE
        /** 更新游戏 */
        public static final int INST_ECOM_UPDATE_GAME                   = 0x00002401;
        /** 更新账号 */
        public static final int INST_ECOM_UPDATE_GAME_ACCOUNT           = 0x00002402;
        /** 更新游戏账户下的游戏 */
        public static final int INST_ECOM_UPDATE_GAME_ACCOUNT_GAME      = 0x00002403;
        /** 更新游戏帐户租赁状态 */
        public static final int INST_ECOM_UPDATE_GAME_ACCOUNT_RENT      = 0x00002404;
        /** 更新渠道账户 */
        public static final int INST_ECOM_UPDATE_CHANNEL_ACCOUNT        = 0x00002405;
        /** 更新订单 */
        public static final int INST_ECOM_UPDATE_ORDER                  = 0x00002406;
        /** 更新订单商品 */
        public static final int INST_ECOM_UPDATE_COMMODITY              = 0x00002407;
        /** 更新游戏租赁价格 */
        public static final int INST_ECOM_UPDATE_GAME_RENT_PRICE        = 0x00002408;
        /** 更新平台账户 */
        public static final int INST_ECOM_UPDATE_PLATFORM_ACCOUNT       = 0x00002409;
        /** 更新平台账户与渠道账户间的映射关系 */
        public static final int INST_ECOM_UPDATE_PLATFORM_ACCOUNT_MAP   = 0x0000240A;
        /** 更新TAG */
        public static final int INST_ECOM_UPDATE_TAG                    = 0x0000240B;
        /** 删除TAG */
        public static final int INST_ECOM_UPDATE_TAG_DEL                = 0x0000240C;
        /** 更新工单 */
        public static final int INST_ECOM_UPDATE_TICKET                 = 0x0000240D;
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
        public static final int CODE_WEB_PSN_CHANGE_PASSWORD_FAILED    = 0x00002033; // PSN修改密码失败
    }

}