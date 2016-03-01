package com.ski.game;

import com.ski.game.monitor.OrderMonitor;
import com.ski.game.session.GameSessionGraph;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.session.FjSessionGraph;

public class GameTask implements FjServerTask {
    
    private FjSessionGraph graph;
    
    public GameTask(String serverName) {
        new OrderMonitor(serverName).start();
        graph = new GameSessionGraph();
    }

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        graph.dispatch(server, wrapper);
    }

}
