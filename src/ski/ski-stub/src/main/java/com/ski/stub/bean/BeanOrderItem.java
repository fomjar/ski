package com.ski.stub.bean;

public class BeanOrderItem {
    
    public int      i_oid;
    public int      i_oisn;
    public String   t_oper_time;
    public int      i_oper_type;
    public int      i_oper_object;
    public String   c_remark;
    public String   c_oper_arg0;
    public String   c_oper_arg1;
    public String   c_oper_arg2;
    
    public BeanOrderItem(String line) {
        String[] fields = line.split("\t", -1);
        i_oid           = Integer.parseInt(fields[0], 16);
        i_oisn          = Integer.parseInt(fields[1], 16);
        t_oper_time     = fields[2];
        i_oper_type     = Integer.parseInt(fields[3], 16);
        i_oper_object   = 0 == fields[4].length() ? -1 : Integer.parseInt(fields[4], 16);
        c_remark        = fields[5];
        c_oper_arg0     = fields[6];
        c_oper_arg1     = fields[7];
        c_oper_arg2     = fields[8];
    }
    
}