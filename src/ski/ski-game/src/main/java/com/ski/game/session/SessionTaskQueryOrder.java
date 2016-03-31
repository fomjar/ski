package com.ski.game.session;

import org.apache.log4j.Logger;

import com.ski.common.SkiCommon;
import com.ski.game.GameToolkit;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServerToolkit;
import fomjar.server.FjServerToolkit.FjAddress;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.session.FjSessionContext;
import fomjar.server.session.FjSessionPath;
import fomjar.server.session.FjSessionTask;

/**
 * 请求来自WCA，向CDB请求产品详单
 * 
 * @author fomjar
 */
public class SessionTaskQueryOrder implements FjSessionTask {
    
    private static final Logger logger = Logger.getLogger(SessionTaskQueryOrder.class);

    @Override
    public boolean onSession(FjSessionContext context, FjSessionPath path, FjMessageWrapper wrapper) {
        String server = context.server();
        FjDscpMessage msg = (FjDscpMessage) wrapper.message();
        if (msg.fs().startsWith("wca")) {
            context.put("caid", msg.argsToJsonObject().getString("user"));
            
            FjDscpMessage msg_cdb = new FjDscpMessage();
            msg_cdb.json().put("fs",   server);
            msg_cdb.json().put("ts",   "cdb");
            msg_cdb.json().put("sid",  context.sid());
            msg_cdb.json().put("inst", SkiCommon.ISIS.INST_ECOM_QUERY_RETURN);
            msg_cdb.json().put("args", String.format("{'c_caid':\"%s\"}", msg.argsToJsonObject().getString("user")));
            FjServerToolkit.getSender(server).send(msg_cdb);
            
            return true;
        } else if (msg.fs().startsWith("cdb")) {
            if (!context.has("user")) {
                logger.error("query order request not come from user side, no user data cached in context, can not forward query data");
                return false;
            }
            
            GameToolkit.sendUserResponse(server, context.sid(), context.getString("user"), createUserResponseContent4Apply(context, msg));
            return true;
        } else {
            logger.error("invalid message, unsupported from: " + msg);
            return false;
        }
    }

    private static String createUserResponseContent4Apply(FjSessionContext context, FjDscpMessage msg) {
        StringBuffer content = new StringBuffer("【游戏清单】\n\n");
        int code = msg.argsToJsonObject().getInt("code");
        if (SkiCommon.CODE.CODE_SYS_SUCCESS != code) return "database operate failed";
        
        String[] products = msg.argsToJsonObject().getJSONArray("desc").get(0).toString().split("\n");
        for (String productString : products) {
            String[] product = productString.split("\t");
            /**
             * product[0] = pid
             * product[1] = product type
             * product[2] = product name
             * product[3] = product inst
             * product[4] = user name
             * product[5] = password current
             * product[6] = password a
             * product[7] = password b
             */
            FjAddress address = FjServerToolkit.getSlb().getAddress("wcweb");
            String url = String.format("http://%s:%d/wcweb?user=%s&inst=%s&account=%s", 
                    address.host,
                    address.port,
                    context.getString("user"),
                    Integer.toHexString(SkiCommon.ISIS.INST_ECOM_APPLY_RETURN),
                    product[1]);
            content.append(String.format("<a href='%s'>《%s》</a>\n账号(%s)：%s\n\n",
                    url,
                    product[2],
                    0 == Integer.parseInt(product[1], 16) ? "A类" : "B类",
                    product[4]));
        }
        return content.toString();
    }

}
