package com.ski.stub;

public class Main {
    
    public static void main(String[] args) {
        if (0 != args.length) Service.setWsiHost(args[0]);
        
        MainFrame.getInstance().setVisible(true);
    }

}
