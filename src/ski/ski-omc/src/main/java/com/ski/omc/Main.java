package com.ski.omc;

import fomjar.server.FjServerToolkit;

public class Main {
    
    public static void main(String[] args) {
        FjServerToolkit.startConfigMonitor();
        FjServerToolkit.startServer("omc").addServerTask(new OmcTask());
    }

}
