package com.ski.frs.isis;

public class ISIS {
    
    public static final int FIELD_PIC_SIZE_LARGE    = 0;
    public static final int FIELD_PIC_SIZE_MIDDLE   = 1;
    public static final int FIELD_PIC_SIZE_SMALL    = 2;
    
    public static final int FIELD_TYPE_MAN          = 0;
    public static final int FIELD_TYPE_CAR          = 1;
    
    public static final int FIELD_DEV_ONLINE        = 0;
    public static final int FIELD_DEV_OFFLINE       = 1;
    
    public static final int FIELD_GENDER_FEMALE     = 0;
    public static final int FIELD_GENDER_MALE       = 1;
    public static final int FIELD_GENDER_UNKNOWN    = -2;
    
    public static final int FIELD_HAT_NO            = 0;
    public static final int FIELD_HAT_YES           = 1;
    public static final int FIELD_HAT_HELMET        = 2;
    public static final int FIELD_HAT_UNKNOWN       = -2;
    
    public static final int FIELD_GLASS_NO          = 0;
    public static final int FIELD_GLASS_YES         = 1;
    public static final int FIELD_GLASS_UNKNOWN     = -2;
    
    public static final int FIELD_MASK_NO           = 0;
    public static final int FIELD_MASK_YES          = 1;
    public static final int FIELD_MASK_UNKNOWN      = -2;
    
    public static final int FIELD_COLOR_BLACK       = 1 << 0;
    public static final int FIELD_COLOR_GRAY        = 1 << 1;
    public static final int FIELD_COLOR_WHITE       = 1 << 2;
    public static final int FIELD_COLOR_RED         = 1 << 3;
    public static final int FIELD_COLOR_BROWN       = 1 << 4;
    public static final int FIELD_COLOR_ORANGE      = 1 << 5;
    public static final int FIELD_COLOR_YELLOW      = 1 << 6;
    public static final int FIELD_COLOR_GREEN       = 1 << 7;
    public static final int FIELD_COLOR_BLUE        = 1 << 8;
    public static final int FIELD_COLOR_PURPLE      = 1 << 9;
    public static final int FIELD_COLOR_PINK        = 1 << 10;
    public static final int FIELD_COLOR_UNKNOWN     = -2;
    
    public static final int FIELD_NATION_HAN        = 0;
    public static final int FIELD_NATION_UYGER      = 1;
    public static final int FIELD_NATION_UNKNOWN    = -2;
    
    
    
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
