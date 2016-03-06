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

public class SessionTaskVerifyAccountFromWA implements FjSessionTask {
    
    private static final Logger logger = Logger.getLogger(SessionTaskVerifyAccountFromWA.class);

    @Override
    public boolean onSession(FjSessionPath path, FjMessageWrapper wrapper) {
        FjSessionContext context = path.context();
        FjDscpMessage msg = (FjDscpMessage) wrapper.message();
        if (!msg.fs().startsWith("wa")) {
            logger.error("invalid message, not come from wa: " + msg);
            return false;
        }
        
        @SuppressWarnings("unchecked")
        Map<String, String> account = (Map<String, String>) context.get("account");
        String pass_new = generateNewPass();
        account.put("pass.new", pass_new);
        JSONObject args2wa = new JSONObject();
        args2wa.put("user", account.get("user"));
        args2wa.put("pass.old", account.get("pass.current"));
        args2wa.put("pass.new", account.get("pass.new"));
        FjDscpMessage msg2wa = new FjDscpMessage();
        msg2wa.json().put("fs",   context.server());
        msg2wa.json().put("ts",   "wa");
        msg2wa.json().put("sid",  msg.sid());
        msg2wa.json().put("inst", SkiCommon.ISIS.INST_ECOM_VERIFY_ACCOUNT);
        msg2wa.json().put("args", args2wa);
        FjServerToolkit.getSender(context.server()).send(msg2wa);
        return true;
    }
    
    private static String generateNewPass() {
        return String.valueOf(Math.abs(Long.toHexString(System.nanoTime()).hashCode()));
    }
    
}
