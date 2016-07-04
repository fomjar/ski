package com.ski.common.bean;

public class BeanChannelAccount {
    
    public BeanChannelAccount(String line) {
        String[] fields = line.split("\t", -1);
        this.i_caid     = Integer.parseInt(fields[0], 16);
        this.c_user     = fields[1];
        this.i_channel  = Integer.parseInt(fields[2], 16);
        this.c_name     = fields[3];
        this.i_gender   = Integer.parseInt(fields[4], 16);
        this.c_phone    = fields[5];
        this.c_address  = fields[6];
        this.c_zipcode  = fields[7];
        this.t_birth    = fields[8];
        this.t_create   = fields[9];
    }
    
    public int      i_caid;
    public String   c_user;
    public int      i_channel;
    public String   c_name;
    public int      i_gender;
    public String   c_phone;
    public String   c_address;
    public String   c_zipcode;
    public String   t_birth;
    public String   t_create;
    
    public String getDisplayName() {return 0 < c_name.length() ? String.format("%s(%s)", c_name, c_user) : c_user;}

}
