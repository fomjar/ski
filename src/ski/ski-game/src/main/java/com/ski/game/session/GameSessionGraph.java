package com.ski.game.session;

import com.ski.common.SkiCommon;

import fomjar.server.session.FjSessionGraph;

public class GameSessionGraph extends FjSessionGraph {
    
    public GameSessionGraph() {
        createNode(SkiCommon.ISIS.INST_ECOM_QUERY_ORDER, new SessionTaskQueryOrderFromUser())
            .append(createNode(SkiCommon.ISIS.INST_ECOM_QUERY_ORDER, new SessionTaskQueryOrderFromCDB()));
        
        createNode(SkiCommon.ISIS.INST_ECOM_APPLY_RETURN, new SessionTaskApplyOrderFromUser())
            .append(createNode(SkiCommon.ISIS.INST_ECOM_APPLY_RETURN, new SessionTaskApplyOrderFromCDB()));
    }

}
