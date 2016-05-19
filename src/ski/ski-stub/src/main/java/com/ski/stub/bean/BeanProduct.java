package com.ski.stub.bean;

public class BeanProduct {
    
    public BeanProduct(String line) {
        String[] fields = line.split("\t", -1);
        this.i_pid          = Integer.parseInt(fields[0], 16);
        this.i_prod_type    = Integer.parseInt(fields[1], 16);
        this.i_prod_inst    = Integer.parseInt(fields[2], 16);
    }
    
    public int  i_pid;
    public int  i_prod_type;
    public int  i_prod_inst;

}
