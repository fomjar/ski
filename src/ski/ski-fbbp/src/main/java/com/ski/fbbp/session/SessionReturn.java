package com.ski.fbbp.session;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.ski.common.DSCP;

import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.session.FjSCB;
import fomjar.server.session.FjSessionController;

public class SessionReturn extends FjSessionController {
    
    private static final Logger logger = Logger.getLogger(SessionReturn.class);

    @Override
    public void onSession(FjServer server, FjSCB scb, FjDscpMessage msg) {
        switch (msg.cmd()) {
        case DSCP.CMD.ECOM_APPLY_RETURN:
            logger.info(String.format("ECOM_APPLY_RETURN - %s:%s", msg.fs(), scb.sid()));
            processApplyReturn(server.name(), scb, msg);
            break;
        case DSCP.CMD.ECOM_SPECIFY_RETURN:
            logger.info(String.format("ECOM_SPECIFY_RETURN - %s:%s", msg.fs(), scb.sid()));
            processSpecifyReturn(server.name(), scb, msg);
            break;
        }
    }
    
    private static void processApplyReturn(String serverName, FjSCB scb, FjDscpMessage msg) {
        if (msg.fs().startsWith("wca")) { // 请求来自WCA，向CDB请求产品详单
            scb.put("caid", ((JSONObject) msg.arg()).getString("user"));
            
            FjDscpMessage msg_cdb = new FjDscpMessage();
            msg_cdb.json().put("fs",  serverName);
            msg_cdb.json().put("ts",  "cdb");
            msg_cdb.json().put("sid", scb.sid());
            msg_cdb.json().put("cmd", DSCP.CMD.ECOM_APPLY_RETURN);
            msg_cdb.json().put("arg", String.format("{'c_caid':\"%s\"}", ((JSONObject) msg.arg()).getString("user")));
            FjServerToolkit.getSender(serverName).send(msg_cdb);
        } else if (msg.fs().startsWith("cdb")) { // 请求来自CDB，转发产品详单至WCA
            JSONObject arg = new JSONObject();
            arg.put("user", scb.getString("caid"));
            String content = null;
            if (null != msg.arg())
                for (Object line : ((JSONArray) msg.arg()))
                    if (null != line)
                        if (null == content) content = ((JSONArray) line).getString(2);
                        else content += "\n" + ((JSONArray) line).getString(2);
            arg.put("content", content);
            
            FjDscpMessage msg_wca = new FjDscpMessage();
            msg_wca.json().put("fs",  serverName);
            msg_wca.json().put("ts",  "wca");
            msg_wca.json().put("sid", scb.sid());
            msg_wca.json().put("cmd", DSCP.CMD.USER_RESPONSE);
            msg_wca.json().put("arg", arg);
            FjServerToolkit.getSender(serverName).send(msg_wca);
            logger.error("[test] content=" + msg.arg());
            scb.end();
        }
    }
    
    private static void processSpecifyReturn(String serverName, FjSCB scb, FjDscpMessage msg) {
    }

}
