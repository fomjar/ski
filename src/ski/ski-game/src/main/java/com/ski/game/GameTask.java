package com.ski.game;

import com.ski.game.monitor.OrderMonitor;
import com.ski.game.session.GameSessionGraphLogic;
import com.ski.game.session.GameSessionGraphQuery;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.session.FjSessionGraph;

public class GameTask implements FjServerTask {
    
    private FjSessionGraph graph_query;
    private FjSessionGraph graph_logic;
    
    public GameTask(String serverName) {
        new OrderMonitor(serverName).start();
        graph_query = new GameSessionGraphQuery();
        graph_logic = new GameSessionGraphLogic();
        graph_query.prepare();
        graph_logic.prepare();
    }

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        graph_query.dispatch(server, wrapper);
        graph_logic.dispatch(server, wrapper);
    }

}
