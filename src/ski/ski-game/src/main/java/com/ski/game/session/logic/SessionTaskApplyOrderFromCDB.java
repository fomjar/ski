package com.ski.game.session.logic;

import org.apache.log4j.Logger;

import com.ski.common.SkiCommon;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.session.FjSessionContext;
import fomjar.server.session.FjSessionTask;
import net.sf.json.JSONObject;

public class SessionTaskApplyOrderFromCDB implements FjSessionTask {
    
    private static final Logger logger = Logger.getLogger(SessionTaskApplyOrderFromCDB.class);

    @Override
    public void onSession(FjServer server, FjSessionContext context, FjMessageWrapper wrapper) {
        String serverName = server.name();
        FjDscpMessage msg = (FjDscpMessage) wrapper.message();
        if (!msg.fs().startsWith("cdb")) {
            logger.error("invalid message, not come from cdb: " + msg);
            return;
        }
        
        JSONObject args = new JSONObject();
        args.put("user",    context.getString("caid"));
        args.put("content", msg.args().toString().replace("\"", "'").replace("[", "").replace("]", ""));
        
        FjDscpMessage msg_wca = new FjDscpMessage();
        msg_wca.json().put("fs",   serverName);
        msg_wca.json().put("ts",   "wca");
        msg_wca.json().put("sid",  context.sid());
        msg_wca.json().put("inst", SkiCommon.ISIS.INST_USER_RESPONSE);
        msg_wca.json().put("args", args);
        FjServerToolkit.getSender(serverName).send(msg_wca);
     }

}
