package com.ski.frs.isis;

public class ISIS {
    
    public static final int FIELD_PIC_SIZE_LARGE    = 0;
    public static final int FIELD_PIC_SIZE_MIDDLE   = 1;
    public static final int FIELD_PIC_SIZE_SMALL    = 2;
    public static final int FIELD_TYPE_MAN          = 0;
    public static final int FIELD_TYPE_CAR          = 1;
    public static final int FIELD_DEV_ONLINE        = 0;
    public static final int FIELD_DEV_OFFLINE       = 1;
    
    
    
    public static final int INST_SET_PIC        = 0x00001000;
    public static final int INST_DEL_PIC        = 0x00001001;
    public static final int INST_GET_PIC        = 0x00001002;
    public static final int INST_GET_PIC_FV     = 0x00001003;
    public static final int INST_SET_SUB        = 0x00001010;
    public static final int INST_DEL_SUB        = 0x00001011;
    public static final int INST_MOD_SUB        = 0x00001012;
    public static final int INST_GET_SUB        = 0x00001013;
    public static final int INST_SET_SUB_ITEM   = 0x00001014;
    public static final int INST_DEL_SUB_ITEM   = 0x00001015;
    public static final int INST_GET_SUB_ITEM   = 0x00001016;
    public static final int INST_SET_DEV        = 0x00001020;
    public static final int INST_DEL_DEV        = 0x00001021;
    public static final int INST_GET_DEV        = 0x00001022;
    public static final int INST_GET_OPP        = 0x00001030;
    
    
    public static final int INST_APPLY_SUB_IMPORT       = 0x00003000;
    public static final int INST_APPLY_SUB_IMPORT_CHECK = 0x00003001;
    public static final int INST_APPLY_SUB_IMPORT_STATE = 0x00003002;
    public static final int INST_APPLY_DEV_IMPORT       = 0x00003010;
    public static final int INST_APPLY_DEV_IMPORT_STOP  = 0x00003011;
    public static final int INST_APPLY_DEV_IMPORT_STATE = 0x00003012;

}
