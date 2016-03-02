package com.ski.game.session;

import org.apache.log4j.Logger;

import com.ski.common.SkiCommon;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.session.FjSessionContext;
import fomjar.server.session.FjSessionPath;
import fomjar.server.session.FjSessionTask;
import net.sf.json.JSONObject;

public class SessionTaskUpdateAccountFromCDB implements FjSessionTask {
    
    private static final Logger logger = Logger.getLogger(SessionTaskUpdateAccountFromCDB.class);

    @Override
    public boolean onSession(FjSessionPath path, FjMessageWrapper wrapper) {
        FjSessionContext context = path.context();
        String server = context.server();
        FjDscpMessage msg = (FjDscpMessage) wrapper.message();
        if (!msg.fs().startsWith("wa")) {
            logger.error("invalid message, not come from wa: " + msg);
            return false;
        }
        String user = context.getString("user");
        JSONObject args2wca = new JSONObject();
        args2wca.put("user", user);
        args2wca.put("content", "please deactive account from your device");
        FjDscpMessage msg2wca = new FjDscpMessage();
        msg2wca.json().put("fs",   server);
        msg2wca.json().put("ts",   "wca");
        msg2wca.json().put("sid",  context.sid());
        msg2wca.json().put("inst", SkiCommon.ISIS.INST_USER_RESPONSE);
        msg2wca.json().put("args", args2wca);
        FjServerToolkit.getSender(server).send(msg2wca);
        
        return true;
    }

}
