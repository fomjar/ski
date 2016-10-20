package com.ski.vcg.common.bean;

public class BeanCommodity {
    
    public int      i_oid;
    public int      i_csn;
    public String   c_remark;
    public float    i_price;
    public int      i_count;
    public String   t_begin;
    public String   t_end;
    public float    i_expense;
    public String   c_arg0;
    public String   c_arg1;
    public String   c_arg2;
    public String   c_arg3;
    public String   c_arg4;
    public String   c_arg5;
    public String   c_arg6;
    public String   c_arg7;
    public String   c_arg8;
    public String   c_arg9;
    
    public BeanCommodity(String line) {
        String[] fields = line.split("\t", -1);
        i_oid       = Integer.parseInt(fields[0], 16);
        i_csn       = Integer.parseInt(fields[1], 16);
        c_remark    = fields[2];
        i_price     = Float.parseFloat(fields[3]);
        i_count     = Integer.parseInt(fields[4], 16);
        t_begin     = fields[5];
        t_end       = fields[6];
        i_expense   = Float.parseFloat(fields[7]);
        c_arg0      = fields[8];
        c_arg1      = fields[9];
        c_arg2      = fields[10];
        c_arg3      = fields[11];
        c_arg4      = fields[12];
        c_arg5      = fields[13];
        c_arg6      = fields[14];
        c_arg7      = fields[15];
        c_arg8      = fields[16];
        c_arg9      = fields[17];
    }
    
    public boolean isClose() {return 0 < t_end.length();}
    
}