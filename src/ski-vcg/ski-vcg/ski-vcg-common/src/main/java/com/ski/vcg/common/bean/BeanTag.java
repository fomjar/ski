package com.ski.vcg.common.bean;

public class BeanTag {
    
    public BeanTag(String line) {
        String[] fields = line.split("\t", -1);
        i_type      = Integer.parseInt(fields[0], 16);
        i_instance  = Integer.parseInt(fields[1], 16);
        c_tag       = fields[2];
    }
    
    public int      i_type;
    public int      i_instance;
    public String   c_tag;

}
