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

public class SessionTaskLockAccountFromCDB implements FjSessionTask {
    
    private static final Logger logger = Logger.getLogger(SessionTaskLockAccountFromCDB.class);

    @Override
    public void onSession(FjSessionPath path, FjMessageWrapper wrapper) {
        FjSessionContext context = path.context();
        String server = context.server();
        FjDscpMessage msg = (FjDscpMessage) wrapper.message();
        if (!msg.fs().startsWith("cdb")) {
            logger.error("invalid message, not come from cdb: " + msg);
            return;
        }
        int instance = context.getInteger("instance");
        @SuppressWarnings("unchecked")
        Map<String, String> account_info = (Map<String, String>) context.get(Integer.toHexString(instance));
        JSONObject args2wa = new JSONObject();
        args2wa.put("password.old", account_info.get("password.current"));
        args2wa.put("password.new", account_info.get("password.a"));
        FjDscpMessage msg2wa = new FjDscpMessage();
        msg2wa.json().put("fs",   server);
        msg2wa.json().put("ts",   "wa");
        msg2wa.json().put("sid",  context.sid());
        msg2wa.json().put("inst", SkiCommon.ISIS.INST_ECOM_UPDATE_ACCOUNT);
        msg2wa.json().put("args", args2wa);
        FjServerToolkit.getSender(server).send(msg2wa);
    }

}
