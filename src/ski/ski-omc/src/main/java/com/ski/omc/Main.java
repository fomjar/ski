package com.ski.omc;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import com.ski.common.CommonService;
import com.ski.omc.comp.MainFrame;

public class Main {
    
    public static void main(String[] args) {
        CommonService.setWsiHost("ski.craftvoid.com");
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
            case "skip-wa":
                UIToolkit.skip_wa = true;
                break;
            case "host":
                CommonService.setWsiHost(args[++i]);
                break;
            }
        }
        
//        initLog();
//        MainFrame.getInstance().setVisible(true);
        MainFrame.getInstance().setVisible(true);
    }
    
    public static void initLog() {
        try {
            PrintStream ps = new PrintStream(new FileOutputStream("ski-omc.log", true));
            System.setOut(ps);
            System.setErr(ps);
        } catch (FileNotFoundException e) {e.printStackTrace();}
    }
}
