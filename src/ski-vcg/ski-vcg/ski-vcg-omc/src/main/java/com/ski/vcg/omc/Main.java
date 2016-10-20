package com.ski.vcg.omc;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import com.ski.vcg.common.CommonService;
import com.ski.vcg.omc.comp.MainFrame;

public class Main {
    
    public static void main(String[] args) {
        CommonService.setWsiHost("vcg.craftvoid.com");
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
        
        if (!UIToolkit.login()) return;
        
        initLog();
        UIToolkit.initUI();
        MainFrame.getInstance().setVisible(true);
    }
    
    private static void initLog() {
        try {System.setErr(new PrintStream(new FileOutputStream("ski-omc.err", true)));}
        catch (FileNotFoundException e) {e.printStackTrace();}
    }
}
