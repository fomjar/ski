package com.ski.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import com.ski.common.CommonService;

public class ExecutorScanChannelCommodity implements ToolExecutor {
    
    private static String       g_conf          = "template/scc.conf";
    private static Properties   config          = new Properties();

    @Override
    public void execute(Map<String, String> args) {
        args.forEach((k, v)->{
            switch (k) {
            case "conf":    g_conf = v;   break;
            default:
                System.out.println(String.format("unknown argument: %s:%s", k, v));
                break;
            }
        });
        
        System.out.print(String.format("%-40s", "loading config..."));
        loadConfig();
        System.out.println(" done!");
        
        System.out.print(String.format("%-40s", "fetching game data..."));
        CommonService.updateGame();
        System.out.println(" done!");
        
        CommonService.getGameAll().values().forEach(game->{
            
        });
    }
    
    private void loadConfig() {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(g_conf);
            config.load(fis);
        } catch (IOException e) {
            System.out.println("load config failed: " + g_conf);
            e.printStackTrace();
        } finally {
            if (null != fis) {
                try {fis.close();}
                catch (IOException e) {e.printStackTrace();}
            }
        }
    }
}
