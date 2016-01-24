package com.ski.stub;

import fomjar.server.FjServerToolkit;

public class Main {
    
    public static void main(String[] args) {
        FjServerToolkit.startConfigMonitor();
        new UiSender().setVisible(true);
    }

}
