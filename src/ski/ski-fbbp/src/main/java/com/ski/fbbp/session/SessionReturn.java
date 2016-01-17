package com.ski.fbbp.session;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.ski.common.DSCP;

import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.FjServerToolkit.FjAddress;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.session.FjSCB;
import fomjar.server.session.FjSessionController;

public class SessionReturn extends FjSessionController {
    
    private static final Logger logger = Logger.getLogger(SessionReturn.class);
    
    private static final int PHASE_APPLY   = 0;
    private static final int PHASE_SPECIFY = 1;
    private static final int PHASE_FINISH  = 2;

    @Override
    public void onSession(FjServer server, FjSCB scb, FjDscpMessage msg) {
        if (!scb.has("return.phase")) {
            if (DSCP.CMD.ECOM_APPLY_RETURN   == msg.cmd()) scb.put("return.phase", PHASE_APPLY);
            if (DSCP.CMD.ECOM_SPECIFY_RETURN == msg.cmd()) scb.put("return.phase", PHASE_SPECIFY);
        }
        switch (scb.getInteger("return.phase")) {
            case PHASE_APPLY:
                logger.info(String.format("ECOM_APPLY_RETURN   - %s:%s", msg.fs(), scb.sid()));
                processPhaseApply  (server.name(), scb, msg);
                break;
            case PHASE_SPECIFY:
                logger.info(String.format("ECOM_SPECIFY_RETURN - %s:%s", msg.fs(), scb.sid()));
                processPhaseSpecify(server.name(), scb, msg);
                break;
            case PHASE_FINISH:
                break;
        }
    }
    
    private static void processPhaseApply(String serverName, FjSCB scb, FjDscpMessage msg) {
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
            arg.put("user",    scb.getString("caid"));
            arg.put("content", createUserResponseContent4Apply(scb, msg));
            
            FjDscpMessage msg_wca = new FjDscpMessage();
            msg_wca.json().put("fs",  serverName);
            msg_wca.json().put("ts",  "wca");
            msg_wca.json().put("sid", scb.sid());
            msg_wca.json().put("cmd", DSCP.CMD.USER_RESPONSE);
            msg_wca.json().put("arg", arg);
            FjServerToolkit.getSender(serverName).send(msg_wca);
            
            scb.put("return.phase", PHASE_SPECIFY);
        }
    }
    
    private static void processPhaseSpecify(String serverName, FjSCB scb, FjDscpMessage msg) {
        if (msg.fs().startsWith("wca")) {
            logger.info("[test] msg=" + msg);
            scb.end();
        }
    }

    private static String createUserResponseContent4Apply(FjSCB scb, FjDscpMessage msg) {
        StringBuffer content = new StringBuffer();
        String[] products = ((JSONArray) ((JSONArray) msg.arg()).get(0)).getString(2).split("\n");
        for (String productString : products) {
            String[] product = productString.split("\t");
            FjAddress address = FjServerToolkit.getSlb().getAddress("wcweb");
            String url = String.format("http://%s:%d/wcweb?user=%s&cmd=%s&content=%s", 
                    address.host,
                    address.port,
                    scb.getString("caid"),
                    Integer.toHexString(DSCP.CMD.ECOM_SPECIFY_RETURN),
                    product[0]);
            content.append(String.format("<a href='%s'>%s</a>%s\r\n",
                    url,
                    product[1],
                    product[2]));
        }
        return content.toString();
    }
}
