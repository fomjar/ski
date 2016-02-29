package com.ski.game.session;

import com.ski.common.SkiCommon;
import com.ski.game.session.query.SessionTaskQueryOrderFromCDB;
import com.ski.game.session.query.SessionTaskQueryOrderFromWCA;

import fomjar.server.session.FjSessionGraph;

public class GameSessionGraphQuery extends FjSessionGraph {
    
    public GameSessionGraphQuery() {
                createNode(SkiCommon.ISIS.INST_ECOM_QUERY_ORDER, new SessionTaskQueryOrderFromWCA())
        .append(createNode(SkiCommon.ISIS.INST_ECOM_QUERY_ORDER, new SessionTaskQueryOrderFromCDB()));
    }

}
