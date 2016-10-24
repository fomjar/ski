package com.ski.sn.common;

public class CommonDefinition {

    public static final class ISIS {
        
        /** 认证 */
        public static final int INST_APPLY_AUTHORIZE            = 0x00001001;
        
        /** 查询用户 */
        public static final int INST_QUERY_USER                 = 0x00002001;
        /** 查询用户配置 */
        public static final int INST_QUERY_USER_CONFIG          = 0x00002002;
        /** 查询消息 */
        public static final int INST_QUERY_MESSAGE              = 0x00002003;
        /** 查询用户状态 */
        public static final int INST_QUERY_USER_STATE           = 0x00002004;
        /** 查询用户状态历史 */
        public static final int INST_QUERY_USER_STATE_HISTORY   = 0X00002005;
        
        /** 更新用户 */
        public static final int INST_UPDATE_USER                = 0x00003001;
        /** 更新用户配置 */
        public static final int INST_UPDATE_USER_CONFIG         = 0x00003002;
        /** 更新消息 */
        public static final int INST_UPDATE_MESSAGE             = 0x00003003;
        /** 更新用户状态 */
        public static final int INST_UPDATE_USER_STATE          = 0x00003004;

    }
    
    public static final class CODE {
        
        public static final int CODE_SUCCESS            = 0x00000000;
        public static final int CODE_ERROR              = 0xFFFFFFFF;
        public static final int CODE_INTERNAL_ERROR     = 0x00000001;
        public static final int CODE_ILLEGAL_INST       = 0x00000002;
        public static final int CODE_ILLEGAL_ARGS       = 0x00000003;
        
    }

}
