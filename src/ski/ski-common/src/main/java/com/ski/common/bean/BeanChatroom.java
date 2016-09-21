package com.ski.common.bean;

public class BeanChatroom {
    
    public BeanChatroom(String line) {
        String[] fields = line.split("\t", -1);
        i_crid      = Integer.parseInt(fields[0], 16);
        c_name      = fields[1];
        t_create    = fields[2];
    }
    
    public int      i_crid;
    public String   c_name;
    public String   t_create;

}
