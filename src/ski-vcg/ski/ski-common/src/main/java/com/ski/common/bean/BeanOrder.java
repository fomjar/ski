package com.ski.common.bean;

import java.util.LinkedHashMap;
import java.util.Map;

public class BeanOrder {
    
    public int      i_oid;
    public int      i_platform;
    public int      i_caid;
    public String   t_open;
    public String   t_close;
    public Map<Integer, BeanCommodity> commodities;
    
    public BeanOrder(String line) {
        String[] fields = line.split("\t", -1);
        i_oid       = Integer.parseInt(fields[0], 16);
        i_platform  = Integer.parseInt(fields[1], 16);
        i_caid      = Integer.parseInt(fields[2], 16);
        t_open      = fields[3];
        t_close     = fields[4];
        commodities = new LinkedHashMap<Integer, BeanCommodity>();
    }
    
    public boolean isClose() {return 0 < t_close.length();}

}
