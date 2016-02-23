package com.ski.fbbp.session;

import org.apache.log4j.Logger;

import com.ski.comm.COMM;

import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.FjServerToolkit.FjAddress;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.session.FjSCB;
import fomjar.server.session.FjSessionController;
import net.sf.json.JSONObject;

public class SessionReturn extends FjSessionController {
    
    private static final Logger logger = Logger.getLogger(SessionReturn.class);
    
    @Override
    protected boolean matchFirst(FjDscpMessage msg) {
        if (COMM.ISIS.INST_ECOM_QUERY_RETURN   == msg.inst()) return true;
        if (COMM.ISIS.INST_ECOM_APPLY_RETURN == msg.inst()) return true;
        return false;
    }

    @Override
    protected void onSession(FjServer server, FjSCB scb, FjDscpMessage msg) {
        switch (msg.inst()) {
            case COMM.ISIS.INST_ECOM_QUERY_RETURN:
                logger.info(String.format("INST_ECOM_APPLY_RETURN   - %s:%s", msg.fs(), scb.sid()));
                processApply  (server.name(), scb, msg);
                break;
            case COMM.ISIS.INST_ECOM_APPLY_RETURN:
                logger.info(String.format("INST_ECOM_SPECIFY_RETURN - %s:%s", msg.fs(), scb.sid()));
                processSpecify(server.name(), scb, msg);
                break;
            case COMM.ISIS.INST_ECOM_FINISH_RETURN:
                break;
        }
    }
    
    private static void processApply(String serverName, FjSCB scb, FjDscpMessage msg) {
        if (msg.fs().startsWith("wca")) { // 请求来自WCA，向CDB请求产品详单
            scb.put("caid", msg.argsToJsonObject().getString("user"));
            
            FjDscpMessage msg_cdb = new FjDscpMessage();
            msg_cdb.json().put("fs",   serverName);
            msg_cdb.json().put("ts",   "cdb");
            msg_cdb.json().put("sid",  scb.sid());
            msg_cdb.json().put("inst", COMM.ISIS.INST_ECOM_QUERY_RETURN);
            msg_cdb.json().put("args", String.format("{'c_caid':\"%s\"}", msg.argsToJsonObject().getString("user")));
            FjServerToolkit.getSender(serverName).send(msg_cdb);
        } else if (msg.fs().startsWith("cdb")) { // 请求来自CDB，转发产品详单至WCA
            JSONObject args = new JSONObject();
            args.put("user",    scb.getString("caid"));
            args.put("content", createUserResponseContent4Apply(scb, msg));
            
            FjDscpMessage msg_wca = new FjDscpMessage();
            msg_wca.json().put("fs",   serverName);
            msg_wca.json().put("ts",   "wca");
            msg_wca.json().put("sid",  scb.sid());
            msg_wca.json().put("inst", COMM.ISIS.INST_USER_RESPONSE);
            msg_wca.json().put("args", args);
            FjServerToolkit.getSender(serverName).send(msg_wca);
            
            scb.end();
        }
    }
    
    private static void processSpecify(String serverName, FjSCB scb, FjDscpMessage msg) {
        if (msg.fs().startsWith("wca")) {
            scb.put("caid", msg.argsToJsonObject().getString("user"));
            
            JSONObject args = new JSONObject();
            args.put("c_caid",    msg.argsToJsonObject().getString("user"));
            args.put("i_inst_id", Integer.parseInt(msg.argsToJsonObject().getString("content"), 16));
            
            FjDscpMessage msg_cdb = new FjDscpMessage();
            msg_cdb.json().put("fs",   serverName);
            msg_cdb.json().put("ts",   "cdb");
            msg_cdb.json().put("sid",  scb.sid());
            msg_cdb.json().put("inst", COMM.ISIS.INST_ECOM_APPLY_RETURN);
            msg_cdb.json().put("args", args);
            FjServerToolkit.getSender(serverName).send(msg_cdb);
        } else if (msg.fs().startsWith("cdb")) {
            JSONObject args = new JSONObject();
            args.put("user",    scb.getString("caid"));
            args.put("content", msg.args().toString().replace("\"", "'").replace("[", "").replace("]", ""));
            
            FjDscpMessage msg_wca = new FjDscpMessage();
            msg_wca.json().put("fs",   serverName);
            msg_wca.json().put("ts",   "wca");
            msg_wca.json().put("sid",  scb.sid());
            msg_wca.json().put("inst", COMM.ISIS.INST_USER_RESPONSE);
            msg_wca.json().put("args", args);
            FjServerToolkit.getSender(serverName).send(msg_wca);
        }
    }

    private static String createUserResponseContent4Apply(FjSCB scb, FjDscpMessage msg) {
        StringBuffer content = new StringBuffer("【游戏清单】\n\n");
        int    code = msg.argsToJsonObject().getInt("code");
        if (COMM.CODE.ERROR_SYSTEM_SUCCESS != code) return "database operate failed";
        
        String[] products = msg.argsToJsonObject().getJSONArray("desc").getJSONArray(0).getString(2).split("\n");
        for (String productString : products) {
            String[] product = productString.split("\t");
            /**
             * product[0] = product type
             * product[1] = instance id
             * product[2] = product name
             * product[3] = game account
             */
            FjAddress address = FjServerToolkit.getSlb().getAddress("wcweb");
            String url = String.format("http://%s:%d/wcweb?user=%s&inst=%s&content=%s", 
                    address.host,
                    address.port,
                    scb.getString("caid"),
                    Integer.toHexString(COMM.ISIS.INST_ECOM_APPLY_RETURN),
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
