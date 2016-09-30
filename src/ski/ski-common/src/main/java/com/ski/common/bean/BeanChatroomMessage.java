package com.ski.common.bean;

public class BeanChatroomMessage {
    
    public BeanChatroomMessage(String line) {
        String[] fields = line.split("\t", -1);
        i_crid      = Integer.parseInt(fields[0], 16);
        i_mid       = Integer.parseInt(fields[1], 16);
        i_member    = "FFFFFFFFFFFFFFFF".equals(fields[2]) ? -1 : Integer.parseInt(fields[2], 16);
        i_type      = Integer.parseInt(fields[3], 16);
        t_time      = fields[4];
        c_message   = fields[5];
        c_arg0      = fields[6];
        c_arg1      = fields[7];
        c_arg2      = fields[8];
        c_arg3      = fields[9];
        c_arg4      = fields[10];
    }
    
    public int      i_crid;
    public int      i_mid;
    public int      i_member;
    public int      i_type;
    public String   t_time;
    public String   c_message;
    public String   c_arg0;
    public String   c_arg1;
    public String   c_arg2;
    public String   c_arg3;
    public String   c_arg4;

}
