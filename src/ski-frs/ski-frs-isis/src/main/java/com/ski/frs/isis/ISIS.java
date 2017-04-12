package com.ski.frs.isis;

public class ISIS {
    
    public static final int FIELD_PIC_SIZE_LARGE    = 0;
    public static final int FIELD_PIC_SIZE_MIDDLE   = 1;
    public static final int FIELD_PIC_SIZE_SMALL    = 2;
    public static final int FIELD_PIC_TYPE_MAN      = 0;
    public static final int FIELD_PIC_TYPE_CAR      = 1;
    
    public static final int INST_UPDATE_PIC             = 0x00001001;
    public static final int INST_UPDATE_SUB_LIB         = 0x00001002;
    public static final int INST_UPDATE_SUB_LIB_DEL     = 0x00001003;
    
    public static final int INST_QUERY_PIC              = 0x00002001;
    public static final int INST_QUERY_PIC_BY_FV_I      = 0x00002002;
    public static final int INST_QUERY_PIC_BY_FV        = 0x00002003;
    public static final int INST_QUERY_SUB_LIB          = 0x00002010;
    public static final int INST_QUERY_SUB_LIB_IMPORT   = 0x00002011;
    
    public static final int INST_APPLY_SUB_LIB_CHECK    = 0x00003001;
    public static final int INST_APPLY_SUB_LIB_IMPORT   = 0x00003002;

}
