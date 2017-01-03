package com.ski.xs.bcs;

import com.ski.xs.common.CommonDefinition;
import com.ski.xs.common.CommonService;

import fomjar.server.FjMessageWrapper;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.session.FjSessionContext;
import fomjar.server.session.FjSessionGraph;
import fomjar.server.session.FjSessionPath;
import fomjar.server.session.FjSessionTask;
import net.sf.json.JSONObject;

public class BcsSession extends FjSessionGraph {
    
    private FjSessionTask st_apply_authorize;
    
    public BcsSession() {
        st_apply_authorize = new StApplyAuthorize();
        
        // authorize
        createHeadNode(CommonDefinition.ISIS.INST_BUSI_APPLY_AUTHORIZE, st_apply_authorize)
            .append(createNode(CommonDefinition.ISIS.INST_BUSI_APPLY_AUTHORIZE, st_apply_authorize));
        
        
    }
    
    private static class StApplyAuthorize implements FjSessionTask {
        @Override
        public boolean onSession(FjSessionContext context, FjSessionPath path, FjMessageWrapper wrapper) {
            FjDscpMessage msg = (FjDscpMessage) wrapper.message();
            switch (context.ssn()) {
            case 0: { // request
                JSONObject args = msg.argsToJsonObject();
                if ((args.has("phone") && args.has("pass"))
                        || (args.has("phone") && args.has("token"))) {
                    CommonService.request("cdb", context.sid(), CommonDefinition.ISIS.INST_BUSI_APPLY_AUTHORIZE, args);
                    return true;
                } else {
                    CommonService.response(msg, CommonDefinition.CODE.CODE_ILLEGAL_ARGS, "illegal arguments for authorize: " + args);
                    return false;
                }
            }
            case 1: { // response
                CommonService.response(context.msg(0), CommonService.getResponseCode(msg), CommonService.getResponseDescToString(msg));
                return true;
            }
            default: {
                return true;
            }
            }
        }
    }
}

