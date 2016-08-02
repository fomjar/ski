package com.ski.common.bean;

import java.util.Arrays;
import java.util.stream.Collectors;

public class BeanGame {
    
    public BeanGame(String line) {
        String[] fields = line.split("\t", -1);
        this.i_gid          = Integer.parseInt(fields[0], 16);
        this.c_name_zh_cn   = fields[1];
        this.c_name_zh_hk   = fields[2];
        this.c_name_en      = fields[3];
        this.c_name_ja      = fields[4];
        this.c_name_ko      = fields[5];
        this.c_name_other   = fields[6];
        this.c_platform     = fields[7];
        this.c_category     = fields[8];
        this.c_language     = fields[9];
        this.c_size			= fields[10];
        this.c_vendor		= fields[11];
        this.t_sale         = fields[12];
        this.c_url_icon     = fields[13];
        this.c_url_cover    = fields[14];
        this.c_url_poster   = fields[15];
        this.c_introduction = fields[16];
        this.c_version   	= fields[17];
    }
    
    public int      i_gid;
    public String   c_name_zh_cn;
    public String   c_name_zh_hk;
    public String   c_name_en;
    public String   c_name_ja;
    public String   c_name_ko;
    public String   c_name_other;
    public String   c_platform;
    public String   c_category;
    public String   c_language;
    public String	c_size;
    public String	c_vendor;
    public String   t_sale;
    public String   c_url_icon;
    public String   c_url_cover;
    public String   c_url_poster;
    public String	c_introduction;
    public String 	c_version;
    
    public String getDisplayName() {
    	return String.format("%s %s %s",
    			c_name_zh_cn,
    			0 < c_name_en.length() ? String.format("(%s)", c_name_en) : "",
    			0 < c_name_other.length() ? String.format("(%s)", Arrays.asList(c_name_other.split(" ")).stream().collect(Collectors.joining("/"))) : "");
    }

}
