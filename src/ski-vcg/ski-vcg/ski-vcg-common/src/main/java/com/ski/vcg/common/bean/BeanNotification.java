package com.ski.vcg.common.bean;

public class BeanNotification {
    
    public int      i_nid;
    public int      i_caid;
    public String   c_content;
    public String   t_create;
    
    public BeanNotification(String line) {
        String[] fields = line.split("\t", -1);
        i_nid       = Integer.parseInt(fields[0], 16);
        i_caid      = Integer.parseInt(fields[1], 16);
        c_content   = fields[2];
        t_create    = fields[3];
    }

}
