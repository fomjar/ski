package com.ski.game;

import com.ski.game.monitor.OrderMonitor;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;

public class GameTask implements FjServerTask {
    
    public GameTask(String serverName) {
        new OrderMonitor(serverName).start();
    }

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
    }

}
