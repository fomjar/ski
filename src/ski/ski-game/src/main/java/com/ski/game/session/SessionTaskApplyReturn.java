package com.ski.game.session;

import org.apache.log4j.Logger;

import com.ski.common.SkiCommon;
import com.ski.game.GameToolkit;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.session.FjSessionContext;
import fomjar.server.session.FjSessionPath;
import fomjar.server.session.FjSessionTask;
import net.sf.json.JSONObject;

public class SessionTaskApplyReturn implements FjSessionTask {
    
    private static final Logger logger = Logger.getLogger(SessionTaskApplyReturn.class);

    @Override
    public boolean onSession(FjSessionContext context, FjSessionPath path, FjMessageWrapper wrapper) {
        String server = context.server();
        FjDscpMessage msg = (FjDscpMessage) wrapper.message();
        
        // from user side, means begin
        if (msg.fs().startsWith("wca") || msg.fs().startsWith("wsi")) {
            JSONObject args = msg.argsToJsonObject();
            if (!args.has("user")) {
                logger.error("invalid message for no 'user' argument: " + msg);
                return false;
            }
            // 重复查询
            if (context.has("user") && !args.has("account")) {
                GameToolkit.sendUserResponse(server, msg.sid(), context.getString("user"), "请选择指定链接进行退货");
                return false;
            }
            String user = args.getString("user");
            context.put("business.type", SkiCommon.ISIS.INST_ECOM_APPLY_RETURN);
            context.put("user", user);
            if (args.has("instance")) {
                String instance = args.getString("instance");
                context.put("instance", instance);
                
                JSONObject args2cdb = new JSONObject();
                args2cdb.put("user", user);
                args2cdb.put("instance", instance);
                FjDscpMessage msg2cdb = new FjDscpMessage();
                msg2cdb.json().put("fs",   server);
                msg2cdb.json().put("ts",   "cdb");
                msg2cdb.json().put("sid",  context.sid());
                msg2cdb.json().put("inst", SkiCommon.ISIS.INST_ECOM_LOCK_INSTANCE);
                msg2cdb.json().put("args", args2cdb);
                FjServerToolkit.getSender(server).send(msg2cdb);
            } else {
                JSONObject args2cdb = new JSONObject();
                args2cdb.put("user", user);
                FjDscpMessage msg2cdb = new FjDscpMessage();
                msg2cdb.json().put("fs",   server);
                msg2cdb.json().put("ts",   "cdb");
                msg2cdb.json().put("sid",  context.sid());
                msg2cdb.json().put("inst", SkiCommon.ISIS.INST_ECOM_QUERY_ORDER);
                msg2cdb.json().put("args", args2cdb);
                FjServerToolkit.getSender(server).send(msg2cdb);
            }
            return true;
        }
        // from db side, means finish
        if (msg.fs().startsWith("cdb")) {
            JSONObject args2wa = new JSONObject();
            args2wa.put("user",  context.getString("alipay.user"));
            args2wa.put("money", context.get("money.rest"));
            FjDscpMessage msg2wa = new FjDscpMessage();
            msg2wa.json().put("fs",   server);
            msg2wa.json().put("ts",   "wa");
            msg2wa.json().put("sid",  context.sid());
            msg2wa.json().put("inst", SkiCommon.ISIS.INST_ECOM_APPLY_TRANSFER);
            msg2wa.json().put("args", args2wa);
            FjServerToolkit.getSender(server).send(msg2wa);
            
            return true;
        }
        
        logger.error("invalid message, unsupported from: " + msg);
        return false;
    }
}
