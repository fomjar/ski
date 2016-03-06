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

public class SessionTaskUpdateAccountFromWA implements FjSessionTask {
    
    private static final Logger logger = Logger.getLogger(SessionTaskUpdateAccountFromWA.class);

    @Override
    public boolean onSession(FjSessionPath path, FjMessageWrapper wrapper) {
        FjSessionContext context = path.context();
        String server = context.server();
        FjDscpMessage msg = (FjDscpMessage) wrapper.message();
        if (!msg.fs().startsWith("wa")) {
            logger.error("invalid message, not come from wa: " + msg);
            return false;
        }
        
        @SuppressWarnings("unchecked")
        Map<String, String> account = (Map<String, String>) context.get("account");
        JSONObject args2cdb = new JSONObject();
        args2cdb.put("user", account.get("user"));
        args2cdb.put("pass", account.get("pass.a"));
        FjDscpMessage msg2cdb = new FjDscpMessage();
        msg2cdb.json().put("fs",   server);
        msg2cdb.json().put("ts",   "cdb");
        msg2cdb.json().put("sid",  context.sid());
        msg2cdb.json().put("inst", SkiCommon.ISIS.INST_ECOM_UPDATE_ACCOUNT);
        msg2cdb.json().put("args", args2cdb);
        FjServerToolkit.getSender(server).send(msg2cdb);
        
        return true;
    }

}
