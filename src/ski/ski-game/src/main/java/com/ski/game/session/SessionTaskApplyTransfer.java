package com.ski.game.session;

import java.util.Map;

import org.apache.log4j.Logger;

import com.ski.common.SkiCommon;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.session.FjSessionContext;
import fomjar.server.session.FjSessionPath;
import fomjar.server.session.FjSessionTask;
import net.sf.json.JSONObject;

public class SessionTaskApplyTransfer implements FjSessionTask {
    
    private static final Logger logger = Logger.getLogger(SessionTaskApplyTransfer.class);

    @Override
    public boolean onSession(FjSessionPath path, FjMessageWrapper wrapper) {
        FjSessionContext context = path.context();
        String server = context.server();
        FjDscpMessage msg = (FjDscpMessage) wrapper.message();

        @SuppressWarnings("unchecked")
        Map<String, String> account = (Map<String, String>) context.get("account");
        
        if (msg.fs().startsWith("wa")) {
            JSONObject args = msg.argsToJsonObject();
            switch (args.getString("desc")) {
            case "finish":
            {
                JSONObject args2wca = new JSONObject();
                args2wca.put("user", account.get("user"));
                args2wca.put("content", "transfer money success");
                FjDscpMessage msg2wca = new FjDscpMessage();
                msg2wca.json().put("fs",   server);
                msg2wca.json().put("ts",   "wca");
                msg2wca.json().put("sid",  context.sid());
                msg2wca.json().put("inst", SkiCommon.ISIS.INST_USER_RESPONSE);
                msg2wca.json().put("args", args2wca);
                FjServerToolkit.getSender(server).send(msg2wca);
            }
                return true;
            case "check":
            {
                JSONObject args2wca = new JSONObject();
                args2wca.put("user", context.get("user"));
                args2wca.put("content", "please ensure tranfer");
                FjDscpMessage msg2wca = new FjDscpMessage();
                msg2wca.json().put("fs",   server);
                msg2wca.json().put("ts",   "wca");
                msg2wca.json().put("sid",  context.sid());
                msg2wca.json().put("inst", SkiCommon.ISIS.INST_USER_RESPONSE);
                msg2wca.json().put("args", args2wca);
                FjServerToolkit.getSender(server).send(msg2wca);
            }
                return true;
            default:
                logger.error("invalid message, unsupported description:" + msg);
                return false;
            }
        } else if (msg.fs().startsWith("wca")) {
            JSONObject args = msg.argsToJsonObject();
            // TODO: check account info
            JSONObject args2wa = new JSONObject();
            args2wa.put("user",  args.getString("user"));
            args2wa.put("name",  account.get("alipay.name"));
            args2wa.put("money", account.get("money.rest"));
            FjDscpMessage msg2wa = new FjDscpMessage();
            msg2wa.json().put("fs",   server);
            msg2wa.json().put("ts",   "wa");
            msg2wa.json().put("sid",  context.sid());
            msg2wa.json().put("inst", SkiCommon.ISIS.INST_ECOM_APPLY_TRANSFER);
            msg2wa.json().put("args", args2wa);
            FjServerToolkit.getSender(server).send(msg2wa);
            
            return true;
        } else {
            logger.error("invalid message, unsupported from: " + msg);
            return false;
        }
    }

}
