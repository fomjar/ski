package com.ski.omc.bean;

public class BeanPlatformAccount {
    
    public BeanPlatformAccount(String line) {
        String[] fields = line.split("\t", -1);
        i_paid      = Integer.parseInt(fields[0], 16);
        c_user      = fields[1];
        c_pass      = fields[2];
        c_name      = fields[3];
        c_mobile    = fields[4];
        c_email     = fields[5];
        t_birth     = fields[6];
        i_balance   = Float.parseFloat(fields[7]);
        i_coupon    = Float.parseFloat(fields[8]);
        t_create    = fields[9];
    }
    
    public int      i_paid;
    public String   c_user;
    public String   c_pass;
    public String   c_name;
    public String   c_mobile;
    public String   c_email;
    public String   t_birth;
    public float    i_balance;
    public float    i_coupon;
    public String   t_create;

}
