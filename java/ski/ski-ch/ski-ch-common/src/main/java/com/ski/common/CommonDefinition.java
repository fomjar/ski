package com.ski.common;


public class CommonDefinition {
    
    public static final class ISIS {
        /** 认证 */
        public static final int INST_USER_AUTHORIZE       = 0x00002001;
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
