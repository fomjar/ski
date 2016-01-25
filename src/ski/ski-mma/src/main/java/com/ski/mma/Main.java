package com.ski.mma;

import fomjar.server.FjServerToolkit;

public class Main {
    
    public static void main(String[] args) {
        FjServerToolkit.startConfigMonitor();
        FjServerToolkit.startServer("mma-sm").addServerTask(new MmaSmTask());
    }

}
