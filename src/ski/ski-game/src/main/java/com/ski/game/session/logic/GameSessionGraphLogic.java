package com.ski.game.session.logic;

import com.ski.common.SkiCommon;

import fomjar.server.session.FjSessionGraph;

public class GameSessionGraphLogic extends FjSessionGraph {
    
    public GameSessionGraphLogic() {
                createNode(SkiCommon.ISIS.INST_ECOM_APPLY_RETURN, new SessionTaskApplyOrderFromWCA())
        .append(createNode(SkiCommon.ISIS.INST_ECOM_APPLY_RETURN, new SessionTaskApplyOrderFromCDB()));
    }

}
