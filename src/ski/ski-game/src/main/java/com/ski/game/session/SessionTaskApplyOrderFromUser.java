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

public class SessionTaskApplyOrderFromUser implements FjSessionTask {
    
    private static final Logger logger = Logger.getLogger(SessionTaskApplyOrderFromUser.class);

    @Override
    public void onSession(FjSessionPath path, FjMessageWrapper wrapper) {
        FjSessionContext context = path.context();
        String server = context.server();
        FjDscpMessage msg = (FjDscpMessage) wrapper.message();
        if (!msg.fs().startsWith("wca")) {
            logger.error("invalid message, not come from wca: " + msg);
            return;
        }
        
        JSONObject args = msg.argsToJsonObject();
        if (!args.has("user")) {
            logger.error("invalid message for no 'user' argument: " + msg);
            return;
        }
        String user = args.getString("user");
        context.put("user", user);
        if (null == args || !args.has("account")) {
            JSONObject args2cdb = new JSONObject();
            args2cdb.put("type", "return");
            args2cdb.put("user", user);
            FjDscpMessage msg2cdb = new FjDscpMessage();
            msg2cdb.json().put("fs",   server);
            msg2cdb.json().put("ts",   "cdb");
            msg2cdb.json().put("sid",  context.sid());
            msg2cdb.json().put("inst", SkiCommon.ISIS.INST_ECOM_QUERY_ORDER);
            msg2cdb.json().put("args", args2cdb);
            FjServerToolkit.getSender(server).send(msg2cdb);
        } else {
            if (!args.has("instance")) {
                logger.error("invalid message, has no 'instance' parameter: " + msg);
                return;
            }
            int instance = args.getInt("instance");
            if (!context.has(Integer.toHexString(instance))) {
                logger.error("invalid instance from message: " + msg);
                return;
            }
            context.put("instance", instance);
            // TODO: describe it
            @SuppressWarnings("unchecked")
            Map<String, String> account_info = (Map<String, String>) context.get(Integer.toHexString(instance));
            String account = account_info.get("account");
            // TODO: infer instance -> instance type -> product type
            JSONObject args2cdb = new JSONObject();
            args2cdb.put("account", account);
            FjDscpMessage msg2cdb = new FjDscpMessage();
            msg2cdb.json().put("fs",   server);
            msg2cdb.json().put("ts",   "cdb");
            msg2cdb.json().put("sid",  context.sid());
            msg2cdb.json().put("inst", SkiCommon.ISIS.INST_ECOM_LOCK_ACCOUNT);
            msg2cdb.json().put("args", args2cdb);
            FjServerToolkit.getSender(server).send(msg2cdb);
        }
     }
}
