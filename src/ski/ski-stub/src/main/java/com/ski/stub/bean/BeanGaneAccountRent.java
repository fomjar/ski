package com.ski.stub.bean;

public class BeanGaneAccountRent {
    
    public BeanGaneAccountRent(String line) {
        String[] fields = line.split("\t", -1);
        this.i_pid      = Integer.parseInt(fields[0], 16);
        this.i_gaid     = Integer.parseInt(fields[1], 16);
        this.c_caid     = fields[2];
        this.i_state    = Integer.parseInt(fields[3], 16);
    }
    
    public int      i_pid;
    public int      i_gaid;
    public String   c_caid;
    public int      i_state;

}
