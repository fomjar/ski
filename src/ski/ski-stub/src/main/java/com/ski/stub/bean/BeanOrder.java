package com.ski.stub.bean;

import java.util.LinkedHashMap;
import java.util.Map;

public class BeanOrder {
    
    public static Map<Integer, BeanOrder> parse(String[] lines) {
        Map<Integer, BeanOrder> orders = new LinkedHashMap<Integer, BeanOrder>();
        for (String line : lines) {
            String[] fields = line.split("\t", -1);
            int oid = Integer.parseInt(fields[0], 16);
            BeanOrder order = null;
            if (orders.containsKey(oid)) {
                order = orders.get(oid);
            } else {
                order = new BeanOrder();
                order.i_oid         = oid;
                order.i_platform    = Integer.parseInt(fields[1], 16);
                order.i_caid        = Integer.parseInt(fields[2], 16);
                order.order_items   = new LinkedHashMap<Integer, BeanOrderItem>();
                orders.put(oid, order);
            }
            BeanOrderItem item = new BeanOrderItem();
            item.i_oid          = oid;
            item.i_oisn         = Integer.parseInt(fields[3], 16);
            item.t_oper_time    = fields[4];
            item.i_oper_type    = Integer.parseInt(fields[5], 16);
            item.i_oper_object  = Integer.parseInt(fields[6], 16);
            item.i_money        = Float.parseFloat(fields[7]);
            item.c_remark       = fields[8];
            order.order_items.put(item.i_oisn, item);
        }
        return orders;
    }
    
    public int  i_oid;
    public int  i_platform;
    public int  i_caid;
    public Map<Integer, BeanOrderItem> order_items;
    
    public static class BeanOrderItem {
        public int      i_oid;
        public int      i_oisn;
        public String   t_oper_time;
        public int      i_oper_type;
        public int      i_oper_object;
        public float    i_money;
        public String   c_remark;
    }

}
