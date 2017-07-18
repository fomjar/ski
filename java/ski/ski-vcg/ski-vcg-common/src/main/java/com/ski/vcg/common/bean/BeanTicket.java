package com.ski.vcg.common.bean;

public class BeanTicket {

    public BeanTicket(String line) {
        String[] fields = line.split("\t", -1);
        i_tid       = Integer.parseInt(fields[0], 16);
        i_caid      = Integer.parseInt(fields[1], 16);
        i_type      = Integer.parseInt(fields[2], 16);
        t_open      = fields[3];
        t_close     = fields[4];
        c_title     = fields[5];
        c_content   = fields[6];
        i_state     = Integer.parseInt(fields[7], 16);
        c_result    = fields[8];
    }

    public int      i_tid;
    public int      i_caid;
    public int      i_type;
    public String   t_open;
    public String   t_close;
    public String   c_title;
    public String   c_content;
    public int      i_state;
    public String   c_result;

    public boolean isClose() {
        return 0 < t_close.length();
    }

}
