package com.ski.vcg.common.bean;

public class BeanPlatformAccountMap {

    public BeanPlatformAccountMap(String line) {
        String[] fields = line.split("\t", -1);
        i_paid = Integer.parseInt(fields[0], 16);
        i_caid = Integer.parseInt(fields[1], 16);
    }

    public int i_paid;
    public int i_caid;

}
