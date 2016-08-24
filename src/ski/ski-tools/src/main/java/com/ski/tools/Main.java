package com.ski.tools;

import java.util.LinkedHashMap;
import java.util.Map;

import com.ski.common.CommonService;

public class Main {
    
    public static void main(String[] args) {
        String      cmd = args[0];
        Map<String, String> arg = new LinkedHashMap<String, String>();
        for (int i = 1; i < args.length; i++) {
            String[] kv = args[i].split("=");
            String k = kv[0];
            String v = args[i].substring(k.length() + 1);
            arg.put(k, v);
        }
        ToolExecutor  e = null;
        switch (cmd) {
        case "mc":
            e = new ExecutorMakeCover();
            break;
        case "mi":
            e = new ExecutorMakeIntroduction();
            break;
        case "cga":
            e = new ExecutorCheckGameAccount();
            break;
        }
        
        CommonService.setWsiHost("ski.craftvoid.com");
        e.execute(arg);
    }

}
