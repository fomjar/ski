package com.ski.game.session.query;

import com.ski.common.SkiCommon;

import fomjar.server.session.FjSessionGraph;

public class GameSessionGraphQuery extends FjSessionGraph {
    
    public GameSessionGraphQuery() {
                createNode(SkiCommon.ISIS.INST_ECOM_QUERY_ORDER, new SessionTaskQueryOrderFromWCA())
        .append(createNode(SkiCommon.ISIS.INST_ECOM_QUERY_ORDER, new SessionTaskQueryOrderFromCDB()));
    }

}
