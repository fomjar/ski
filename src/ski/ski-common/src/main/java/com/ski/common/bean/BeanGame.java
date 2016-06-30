package com.ski.common.bean;

public class BeanGame {
    
    public BeanGame(String line) {
        String[] fields = line.split("\t", -1);
        this.i_gid          = Integer.parseInt(fields[0], 16);
        this.c_platform     = fields[1];
        this.c_country      = fields[2];
        this.c_url_icon     = fields[3];
        this.c_url_poster   = fields[4];
        this.c_url_buy      = fields[5];
        this.t_sale         = fields[6];
        this.c_name_zh      = fields[7];
        this.c_name_en      = fields[8];
    }
    
    public int      i_gid;
    public String   c_platform;
    public String   c_country;
    public String   c_url_icon;
    public String   c_url_poster;
    public String   c_url_buy;
    public String   t_sale;
    public String   c_name_zh;
    public String   c_name_en;

}
