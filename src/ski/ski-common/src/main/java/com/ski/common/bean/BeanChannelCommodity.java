package com.ski.common.bean;

public class BeanChannelCommodity {
    
    public int      i_osn;
    public int      i_cid;
    public String   t_time;
    public int      i_channel;
    public String   c_item_url;
    public String   c_item_cover;
    public String   c_item_name;
    public String   c_item_remark;
    public int      i_item_sold;
    public String   c_item_price;
    public float    i_express_price;
    public String   c_shop_url;
    public String   c_shop_name;
    public String   c_shop_owner;
    public int      i_shop_rate;
    public String   c_shop_score;
    public String   c_shop_addr;
    
    public BeanChannelCommodity(String line) {
        String[] fields = line.split("\t", -1);
        this.i_osn              = Integer.parseInt(fields[0], 16);
        this.i_cid              = Integer.parseInt(fields[1], 16);
        this.t_time             = fields[2];
        this.i_channel          = Integer.parseInt(fields[3], 16);
        this.c_item_url         = fields[4];
        this.c_item_cover       = fields[5];
        this.c_item_name        = fields[6];
        this.c_item_remark      = fields[7];
        this.i_item_sold        = Integer.parseInt(fields[8], 16);
        this.c_item_price       = fields[9];
        this.i_express_price    = Float.parseFloat(fields[10]);
        this.c_shop_url         = fields[11];
        this.c_shop_name        = fields[12];
        this.c_shop_owner       = fields[13];
        this.i_shop_rate        = Integer.parseInt(fields[14], 16);
        this.c_shop_score       = fields[15];
        this.c_shop_addr        = fields[16];
    }
    
}
