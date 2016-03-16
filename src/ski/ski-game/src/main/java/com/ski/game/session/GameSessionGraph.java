package com.ski.game.session;

import com.ski.common.SkiCommon;

import fomjar.server.session.FjSessionGraph;
import fomjar.server.session.FjSessionTask;

public class GameSessionGraph extends FjSessionGraph {
    
    public GameSessionGraph() {
        
        FjSessionTask query_order    = new SessionTaskQueryOrder();
        FjSessionTask apply_return   = new SessionTaskApplyReturn();
        FjSessionTask lock_account   = new SessionTaskLockAccount();
        FjSessionTask update_account = new SessionTaskUpdateAccount();
        FjSessionTask user_request   = new SessionTaskUserRequest();
        FjSessionTask verify_account = new SessionTaskVerifyAccount();
        FjSessionTask apply_transfer = new SessionTaskApplyTransfer();
        
                createHeadNode(SkiCommon.ISIS.INST_ECOM_QUERY_ORDER, query_order)
        .append(    createNode(SkiCommon.ISIS.INST_ECOM_QUERY_ORDER, query_order));
        
                createHeadNode(SkiCommon.ISIS.INST_ECOM_APPLY_RETURN, apply_return)
        .append(    createNode(SkiCommon.ISIS.INST_ECOM_QUERY_ORDER, query_order))
        .append(        createNode(SkiCommon.ISIS.INST_ECOM_APPLY_RETURN, apply_return))
        .append(            createNode(SkiCommon.ISIS.INST_ECOM_LOCK_ACCOUNT, lock_account))
        .append(                createNode(SkiCommon.ISIS.INST_ECOM_UPDATE_ACCOUNT, update_account))
        .append(                    createNode(SkiCommon.ISIS.INST_ECOM_UPDATE_ACCOUNT, update_account))
        .append(                        createNode(SkiCommon.ISIS.INST_USER_REQUEST, user_request))
        .append(                            createNode(SkiCommon.ISIS.INST_ECOM_VERIFY_ACCOUNT, verify_account))
        .append(                                createNode(SkiCommon.ISIS.INST_ECOM_UPDATE_ACCOUNT, update_account))
        .append(                                    createNode(SkiCommon.ISIS.INST_ECOM_UPDATE_ACCOUNT, update_account))
        .append(                                        createNode(SkiCommon.ISIS.INST_ECOM_APPLY_RETURN, apply_return))
        .append(                                            createNode(SkiCommon.ISIS.INST_ECOM_APPLY_TRANSFER, apply_transfer))
        .append(                                                createNode(SkiCommon.ISIS.INST_ECOM_APPLY_TRANSFER, apply_transfer))
        .append(                                                    createNode(SkiCommon.ISIS.INST_ECOM_APPLY_TRANSFER, apply_transfer));
    }

}
