package com.ski.game.session.query;

import org.apache.log4j.Logger;

import com.ski.common.SkiCommon;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.session.FjSessionContext;
import fomjar.server.session.FjSessionTask;

/**
 * 请求来自WCA，向CDB请求产品详单
 * 
 * @author fomjar
 */
public class SessionTaskQueryOrderFromWCA implements FjSessionTask {
    
    private static final Logger logger = Logger.getLogger(SessionTaskQueryOrderFromWCA.class);

    @Override
    public void onSession(FjServer server, FjSessionContext context, FjMessageWrapper wrapper) {
        String serverName = server.name();
        FjDscpMessage msg = (FjDscpMessage) wrapper.message();
        if (!msg.fs().startsWith("wca")) {
            logger.error("invalid message, not come from wca: " + msg);
            return;
        }
        
        context.put("caid", msg.argsToJsonObject().getString("user"));
        
        FjDscpMessage msg_cdb = new FjDscpMessage();
        msg_cdb.json().put("fs",   serverName);
        msg_cdb.json().put("ts",   "cdb");
        msg_cdb.json().put("sid",  context.sid());
        msg_cdb.json().put("inst", SkiCommon.ISIS.INST_ECOM_QUERY_RETURN);
        msg_cdb.json().put("args", String.format("{'c_caid':\"%s\"}", msg.argsToJsonObject().getString("user")));
        FjServerToolkit.getSender(serverName).send(msg_cdb);
    }

}
