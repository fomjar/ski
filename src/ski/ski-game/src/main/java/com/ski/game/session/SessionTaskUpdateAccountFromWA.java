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

public class SessionTaskUpdateAccountFromWA implements FjSessionTask {
    
    private static final Logger logger = Logger.getLogger(SessionTaskUpdateAccountFromWA.class);

    @Override
    public void onSession(FjSessionPath path, FjMessageWrapper wrapper) {
        FjSessionContext context = path.context();
        String server = context.server();
        FjDscpMessage msg = (FjDscpMessage) wrapper.message();
        if (!msg.fs().startsWith("wa")) {
            logger.error("invalid message, not come from wa: " + msg);
            return;
        }
        int instance = context.getInteger("instance");
        @SuppressWarnings("unchecked")
        Map<String, String> account_info = (Map<String, String>) context.get(Integer.toHexString(instance));
        JSONObject args2cdb = new JSONObject();
        args2cdb.put("instance", instance);
        args2cdb.put("password.current", account_info.get("password.a"));
        FjDscpMessage msg2cdb = new FjDscpMessage();
        msg2cdb.json().put("fs",   server);
        msg2cdb.json().put("ts",   "cdb");
        msg2cdb.json().put("sid",  context.sid());
        msg2cdb.json().put("inst", SkiCommon.ISIS.INST_ECOM_UPDATE_ACCOUNT);
        msg2cdb.json().put("args", args2cdb);
        FjServerToolkit.getSender(server).send(msg2cdb);
    }

}
