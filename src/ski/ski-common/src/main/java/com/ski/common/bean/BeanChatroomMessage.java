package com.ski.common.bean;

public class BeanChatroomMessage {
    
    public BeanChatroomMessage(String line) {
        String[] fields = line.split("\t", -1);
        i_crid      = Integer.parseInt(fields[0], 16);
        i_member    = Integer.parseInt(fields[1], 16);
        i_type      = Integer.parseInt(fields[2], 16);
        c_message   = fields[3];
        t_time      = fields[4];
    }
    
    public int      i_crid;
    public int      i_member;
    public int      i_type;
    public String   c_message;
    public String   t_time;

}
