package com.ski.omc;

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
        
        UIToolkit.initFont();
        MainFrame.getInstance().setVisible(true);
    }
}
