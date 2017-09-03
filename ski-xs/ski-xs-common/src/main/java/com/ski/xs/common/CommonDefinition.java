package com.ski.xs.common;

public class CommonDefinition {
    
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
        public static final int INST_USER_COMMAND       = 0x00001005;
        /** 用户跳转界面/网页 */
        public static final int INST_USER_VIEW          = 0x00001006;
        /** 用户地理位置 */
        public static final int INST_USER_LOCATION      = 0x00001007;
        /** 用户(结果)通知 */
        public static final int INST_USER_NOTIFY        = 0x00001008;
        
        //////////////////////////////// 业务指令 ////////////////////////////////
        /** 认证 */
        public static final int INST_BUSI_APPLY_AUTHORIZE       = 0x00002001;
        
        /** 更新用户 */
        public static final int INST_BUSI_UPDATE_USER           = 0x00003001;
        /** 更新文章 */
        public static final int INST_BUSI_UPDATE_ARTICLE        = 0x00003002;
        /** 更新段落 */
        public static final int INST_BUSI_UPDATE_PARAGRAPH      = 0x00003003;
        /** 删除段落 */
        public static final int INST_BUSI_UPDATE_PARAGRAPH_DEL  = 0x00003004;
        /** 更新元素 */
        public static final int INST_BUSI_UPDATE_ELEMENT        = 0x00003005;
        /** 删除元素 */
        public static final int INST_BUSI_UPDATE_ELEMENT_DEL    = 0x00003006;
        /** 更新TAG */
        public static final int INST_BUSI_UPDATE_TAG            = 0x00003007;
        /** 删除TAG */
        public static final int INST_BUSI_UPDATE_TAG_DEL        = 0x00003008;
        /** 更新文件夹 */
        public static final int INST_BUSI_UPDATE_FOLDER         = 0x00003009;
        /** 删除文件夹 */
        public static final int INST_BUSI_UPDATE_FOLDER_DEL     = 0x0000300A;
        /** 更新文件夹文章映射关系 */
        public static final int INST_BUSI_UPDATE_FOLDER_ARTICLE = 0x0000300B;
        
        /** 查询用户 */
        public static final int INST_BUSI_QUERY_USER            = 0x00004001;
        /** 查询文章 */
        public static final int INST_BUSI_QUERY_ARTICLE_BY_AID  = 0x00004002;
        /** 查询文件夹 */
        public static final int INST_BUSI_QUERY_FOLDER          = 0x00004003;
    }
    
    public static final class CODE {
        public static final int CODE_SUCCESS            = 0x00000000;
        public static final int CODE_ERROR              = 0xFFFFFFFF;
        public static final int CODE_INTERNAL_ERROR     = 0x00000001;
        public static final int CODE_ILLEGAL_INST       = 0x00000002;
        public static final int CODE_ILLEGAL_ARGS       = 0x00000003;
        public static final int CODE_UNAUTHORIZED       = 0x00000004;
    }

}
