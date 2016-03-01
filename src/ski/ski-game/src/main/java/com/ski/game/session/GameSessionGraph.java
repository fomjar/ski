package com.ski.game.session;

import com.ski.common.SkiCommon;

import fomjar.server.session.FjSessionGraph;

public class GameSessionGraph extends FjSessionGraph {
    
    public GameSessionGraph() {
                createHeadNode(SkiCommon.ISIS.INST_ECOM_QUERY_ORDER, new SessionTaskQueryOrderFromUser())
        .append(    createNode(SkiCommon.ISIS.INST_ECOM_QUERY_ORDER, new SessionTaskQueryOrderFromCDB()));
        
                createHeadNode(SkiCommon.ISIS.INST_ECOM_APPLY_RETURN, new SessionTaskApplyOrderFromUser())
        .append(    createNode(SkiCommon.ISIS.INST_ECOM_QUERY_ORDER, new SessionTaskQueryOrderFromCDB()))
        .append(        createNode(SkiCommon.ISIS.INST_ECOM_APPLY_RETURN, new SessionTaskApplyOrderFromUser()))
        .append(            createNode(SkiCommon.ISIS.INST_ECOM_LOCK_ACCOUNT, new SessionTaskLockAccountFromCDB()))
        .append(                createNode(SkiCommon.ISIS.INST_ECOM_UPDATE_ACCOUNT, new SessionTaskUpdateAccountFromWA()))
        .append(                    createNode(SkiCommon.ISIS.INST_ECOM_UPDATE_ACCOUNT, new SessionTaskUpdateAccountFromCDB()))
        .append(                        createNode(SkiCommon.ISIS.INST_USER_RESPONSE, new SessionTaskUserRequestFromWCA()));
    }

}
