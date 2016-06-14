package com.ski.omc.bean;

public class BeanOrderItem {
    
    public int      i_oid;
    public int      i_oisn;
    public String   t_oper_time;
    public int      i_oper_type;
    public String   c_remark;
    public String   c_oper_arg0;
    public String   c_oper_arg1;
    public String   c_oper_arg2;
    public String   c_oper_arg3;
    public String   c_oper_arg4;
    public String   c_oper_arg5;
    public String   c_oper_arg6;
    public String   c_oper_arg7;
    public String   c_oper_arg8;
    public String   c_oper_arg9;
    
    public BeanOrderItem(String line) {
        String[] fields = line.split("\t", -1);
        i_oid           = Integer.parseInt(fields[0], 16);
        i_oisn          = Integer.parseInt(fields[1], 16);
        t_oper_time     = fields[2];
        i_oper_type     = Integer.parseInt(fields[3], 16);
        c_remark        = fields[4];
        c_oper_arg0     = fields[5];
        c_oper_arg1     = fields[6];
        c_oper_arg2     = fields[7];
        c_oper_arg3     = fields[8];
        c_oper_arg4     = fields[9];
        c_oper_arg5     = fields[10];
        c_oper_arg6     = fields[11];
        c_oper_arg7     = fields[12];
        c_oper_arg8     = fields[13];
        c_oper_arg9     = fields[14];
    }
    
}