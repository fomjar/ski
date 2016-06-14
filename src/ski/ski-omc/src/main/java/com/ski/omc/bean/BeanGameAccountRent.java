package com.ski.omc.bean;

public class BeanGameAccountRent {
    
    public BeanGameAccountRent(String line) {
        String[] fields = line.split("\t", -1);
        this.i_gaid     = Integer.parseInt(fields[0], 16);
        this.i_type     = Integer.parseInt(fields[1], 16);
        this.i_caid     = Integer.parseInt(fields[2], 16);
        this.i_state    = Integer.parseInt(fields[3], 16);
        this.t_change   = fields[4];
    }
    
    public int      i_gaid;
    public int      i_type;
    public int      i_caid;
    public int      i_state;
    public String   t_change;

}
