package com.ski.omc;

import com.ski.omc.comp.MainFrame;

public class Main {
    
    public static void main(String[] args) {
        if (0 != args.length) Service.setWsiHost(args[0]);
        
        Service.initLog();
        MainFrame.getInstance().setVisible(true);
    }

}
