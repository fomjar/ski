package com.ski.stub.bean;

public class BeanGameAccountGame {
    
    public BeanGameAccountGame(String line) {
        String[] fields = line.split("\t", -1);
        this.i_gaid = Integer.parseInt(fields[0], 16);
        this.i_gid  = Integer.parseInt(fields[1], 16);
    }
    
    public int i_gaid;
    public int i_gid;

}
