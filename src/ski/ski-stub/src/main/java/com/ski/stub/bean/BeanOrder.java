package com.ski.stub.bean;

import java.util.LinkedHashMap;
import java.util.Map;

public class BeanOrder {
    
    public int      i_oid;
    public int      i_platform;
    public int      i_caid;
    public String   t_create;
    public Map<Integer, BeanOrderItem> order_items;
    
    public BeanOrder(String line) {
        String[] fields = line.split("\t", -1);
        i_oid       = Integer.parseInt(fields[0], 16);
        i_platform  = Integer.parseInt(fields[1], 16);
        i_caid      = Integer.parseInt(fields[2], 16);
        t_create    = fields[3];
        order_items = new LinkedHashMap<Integer, BeanOrderItem>();
    }

}
