package com.ski.game.session;

import java.util.Map;

import org.apache.log4j.Logger;

import com.ski.common.SkiCommon;

import fomjar.server.FjMessageWrapper;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.session.FjSessionContext;
import fomjar.server.session.FjSessionPath;
import fomjar.server.session.FjSessionTask;
import net.sf.json.JSONObject;

public class SessionTaskUserRequest implements FjSessionTask {
    
    private static final Logger logger = Logger.getLogger(SessionTaskUserRequest.class);
    
    @Override
    public boolean onSession(FjSessionPath path, FjMessageWrapper wrapper) {
        FjSessionContext context = path.context();
        FjDscpMessage msg = (FjDscpMessage) wrapper.message();
        JSONObject args = msg.argsToJsonObject();
        String content = args.getString("content");
        
        switch (context.getInteger("business.type")) {
        case SkiCommon.ISIS.INST_ECOM_APPLY_RETURN:
            switch (content.toLowerCase()) {
            case "da":
                processEnsureDeactive(context);
                break;
            default:
                logger.error("unrecognized user request: " + content);
                return false;
            }
            break;
        default:
            logger.error("unsupported business type: " + context.getInteger("business.type"));
            return false;
        }
        return true;
    }
    
    private static void processEnsureDeactive(FjSessionContext context) {
        @SuppressWarnings("unchecked")
        Map<String, String> account = (Map<String, String>) context.get("account");
        JSONObject args2wa = new JSONObject();
        args2wa.put("type", "psn");
        args2wa.put("user", account.get("user"));
        args2wa.put("pass", account.get("pass.current"));
        args2wa.put("state", "notuse");
        FjDscpMessage msg2wa = new FjDscpMessage();
        msg2wa.json().put("fs",   context.server());
        msg2wa.json().put("ts",   "wa");
        msg2wa.json().put("sid",  context.sid());
        msg2wa.json().put("inst", SkiCommon.ISIS.INST_ECOM_VERIFY_ACCOUNT);
        msg2wa.json().put("args", args2wa);
    }

}
