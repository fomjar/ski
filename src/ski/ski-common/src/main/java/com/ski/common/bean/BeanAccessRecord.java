package com.ski.common.bean;

public class BeanAccessRecord {
    
    public BeanAccessRecord(String line) {
        String[] fields = line.split("\t", -1);
        i_caid  	= Integer.parseInt(fields[0], 16);
        c_remote    = fields[1];
        c_local     = fields[2];
        t_time     	= fields[3];
    }
    
    public int      i_caid;
    public String   c_remote;
    public String   c_local;
    public String   t_time;

}
