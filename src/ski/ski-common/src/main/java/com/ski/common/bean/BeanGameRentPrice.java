package com.ski.common.bean;

public class BeanGameRentPrice {
    
    public BeanGameRentPrice(String line) {
        String[] fields = line.split("\t", -1);
        this.i_gid      = Integer.parseInt(fields[0], 16);
        this.i_type     = Integer.parseInt(fields[1], 16);
        this.i_price    = Float.parseFloat(fields[2]);
    }

    public int      i_gid;
    public int      i_type;
    public float    i_price;
}
