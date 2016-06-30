package com.ski.wca;

import com.ski.wca.biz.WcaBusiness;

import fomjar.server.FjServerToolkit;

public class Main {

    /**
     * @param args[0] server name
     */
    public static void main(String[] args) {
        FjServerToolkit.startConfigMonitor();
        FjServerToolkit.startServer(args[0]).addServerTask(new WcaTask());
        WcaBusiness.getInstance().setServer(args[0]);
    }

}
