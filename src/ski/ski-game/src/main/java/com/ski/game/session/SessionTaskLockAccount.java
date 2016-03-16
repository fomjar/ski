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

public class SessionTaskLockAccount implements FjSessionTask {
    
    private static final Logger logger = Logger.getLogger(SessionTaskLockAccount.class);

    @Override
    public boolean onSession(FjSessionPath path, FjMessageWrapper wrapper) {
        FjSessionContext context = path.context();
        String server = context.server();
        FjDscpMessage msg = (FjDscpMessage) wrapper.message();
        
        @SuppressWarnings("unchecked")
        Map<String, String> account = (Map<String, String>) context.get("account");
        
        if (msg.fs().startsWith("cdb")) {
            JSONObject args2wa = new JSONObject();
            args2wa.put("user", account.get("user"));
            args2wa.put("pass.old", account.get("pass.current"));
            args2wa.put("pass.new", account.get("pass.a"));
            FjDscpMessage msg2wa = new FjDscpMessage();
            msg2wa.json().put("fs",   server);
            msg2wa.json().put("ts",   "wa");
            msg2wa.json().put("sid",  context.sid());
            msg2wa.json().put("inst", SkiCommon.ISIS.INST_ECOM_UPDATE_ACCOUNT);
            msg2wa.json().put("args", args2wa);
            FjServerToolkit.getSender(server).send(msg2wa);
            
            return true;
        } else {
            logger.error("invalid message, unsupported from: " + msg);
            return false;
        }
    }

}
