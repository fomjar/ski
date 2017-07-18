package com.ski.vcg.common.bean;

public class BeanPlatformAccountMoney {

    public BeanPlatformAccountMoney(String line) {
        String[] fields = line.split("\t", -1);
        this.i_paid     = Integer.parseInt(fields[0], 16);
        this.c_remark   = fields[1];
        this.t_time     = fields[2];
        this.i_type     = Integer.parseInt(fields[3], 16);
        this.i_base     = Float.parseFloat(fields[4]);
        this.i_money    = Float.parseFloat(fields[5]);
    }

    public int      i_paid;
    public String   c_remark;
    public String   t_time;
    public int      i_type;
    public float    i_base;
    public float    i_money;

}
