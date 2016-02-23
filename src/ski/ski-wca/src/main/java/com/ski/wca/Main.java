package com.ski.wca;

import fomjar.server.FjServerToolkit;

public class Main {

    /**
     * @param args[0] server name
     */
    public static void main(String[] args) {
        FjServerToolkit.startConfigMonitor();
        FjServerToolkit.startServer("wca").addServerTask(new WcaTask());
        FjServerToolkit.startServer("wcweb").addServerTask(new WcWebTask());
    }

}
