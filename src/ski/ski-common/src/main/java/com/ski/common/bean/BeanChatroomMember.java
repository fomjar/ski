package com.ski.common.bean;

public class BeanChatroomMember {
    
    public BeanChatroomMember(String line) {
        String[] fields = line.split("\t", -1);
        i_crid      = Integer.parseInt(fields[0], 16);
        i_member    = Integer.parseInt(fields[1], 16);
    }
    
    public int  i_crid;
    public int  i_member;

}
