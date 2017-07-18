package com.ski.vcg.common.bean;

public class BeanGameAccount {

    public BeanGameAccount(String line) {
        String[] fields = line.split("\t", -1);
        this.i_gaid     = Integer.parseInt(fields[0], 16);
        this.c_remark    = fields[1];
        this.c_user     = fields[2];
        this.c_pass        = fields[3];
        this.c_name        = fields[4];
        this.t_birth    = fields[5];
        this.t_create   = fields[6];
    }

    public int      i_gaid;
    public String    c_remark;
    public String   c_user;
    public String   c_pass;
    public String    c_name;
    public String   t_birth;
    public String   t_create;

}
