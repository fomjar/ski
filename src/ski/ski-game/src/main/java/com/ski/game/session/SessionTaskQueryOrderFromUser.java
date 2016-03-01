package com.ski.game.session;

import org.apache.log4j.Logger;

import com.ski.common.SkiCommon;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.session.FjSessionContext;
import fomjar.server.session.FjSessionPath;
import fomjar.server.session.FjSessionTask;

/**
 * 请求来自WCA，向CDB请求产品详单
 * 
 * @author fomjar
 */
public class SessionTaskQueryOrderFromUser implements FjSessionTask {
    
    private static final Logger logger = Logger.getLogger(SessionTaskQueryOrderFromUser.class);

    @Override
    public void onSession(FjSessionPath path, FjMessageWrapper wrapper) {
        FjSessionContext context = path.context();
        String server = context.server();
        FjDscpMessage msg = (FjDscpMessage) wrapper.message();
        if (!msg.fs().startsWith("wca")) {
            logger.error("invalid message, not come from wca: " + msg);
            return;
        }
        
        context.put("caid", msg.argsToJsonObject().getString("user"));
        
        FjDscpMessage msg_cdb = new FjDscpMessage();
        msg_cdb.json().put("fs",   server);
        msg_cdb.json().put("ts",   "cdb");
        msg_cdb.json().put("sid",  context.sid());
        msg_cdb.json().put("inst", SkiCommon.ISIS.INST_ECOM_QUERY_RETURN);
        msg_cdb.json().put("args", String.format("{'c_caid':\"%s\"}", msg.argsToJsonObject().getString("user")));
        FjServerToolkit.getSender(server).send(msg_cdb);
    }

}
