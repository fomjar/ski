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
    
    @Override
    public void onSession(FjServer server, FjSCB scb, FjDscpMessage msg) {
        switch (msg.cmd()) {
            case DSCP.CMD.ECOM_APPLY_RETURN:
                logger.info(String.format("ECOM_APPLY_RETURN   - %s:%s", msg.fs(), scb.sid()));
                processApply  (server.name(), scb, msg);
                break;
            case DSCP.CMD.ECOM_SPECIFY_RETURN:
                logger.info(String.format("ECOM_SPECIFY_RETURN - %s:%s", msg.fs(), scb.sid()));
                processSpecify(server.name(), scb, msg);
                break;
            case DSCP.CMD.ECOM_FINISH_RETURN:
                break;
        }
    }
    
    private static void processApply(String serverName, FjSCB scb, FjDscpMessage msg) {
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
            
            scb.end();
        }
    }
    
    private static void processSpecify(String serverName, FjSCB scb, FjDscpMessage msg) {
        if (msg.fs().startsWith("wca")) {
            JSONObject arg = new JSONObject();
            arg.put("c_caid", ((JSONObject) msg.arg()).getString("user"));
            arg.put("i_pid",  Integer.parseInt(((JSONObject) msg.arg()).getString("content"), 16));
            
            FjDscpMessage msg_cdb = new FjDscpMessage();
            msg_cdb.json().put("fs",  serverName);
            msg_cdb.json().put("ts",  "cdb");
            msg_cdb.json().put("sid", scb.sid());
            msg_cdb.json().put("cmd", DSCP.CMD.ECOM_SPECIFY_RETURN);
            msg_cdb.json().put("arg", arg);
            FjServerToolkit.getSender(serverName).send(msg_cdb);
        } else if (msg.fs().startsWith("cdb")) {
            logger.info("[TEST] message = " + msg);
            scb.end();
        }
    }

    private static String createUserResponseContent4Apply(FjSCB scb, FjDscpMessage msg) {
        StringBuffer content = new StringBuffer("【游戏清单】\n\n");
        String[] products = ((JSONArray) ((JSONArray) msg.arg()).get(0)).getString(2).split("\n");
        for (String productString : products) {
            String[] product = productString.split("\t");
            FjAddress address = FjServerToolkit.getSlb().getAddress("wcweb");
            String url = String.format("http://%s:%d/wcweb?user=%s&cmd=%s&content=%s", 
                    address.host,
                    address.port,
                    scb.getString("caid"),
                    Integer.toHexString(DSCP.CMD.ECOM_SPECIFY_RETURN),
                    product[1]);
            content.append(String.format("<a href='%s'>《%s》</a>\n账号(%s)：%s\n\n",
                    url,
                    product[2],
                    0 == Integer.parseInt(product[0], 16) ? "A类" : "B类",
                    product[3]));
        }
        return content.toString();
    }
}
