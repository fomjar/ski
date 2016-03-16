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

public class SessionTaskUpdateAccount implements FjSessionTask {
    
    private static final Logger logger = Logger.getLogger(SessionTaskUpdateAccount.class);

    @Override
    public boolean onSession(FjSessionPath path, FjMessageWrapper wrapper) {
        FjSessionContext context = path.context();
        String server = context.server();
        FjDscpMessage msg = (FjDscpMessage) wrapper.message();
        
        @SuppressWarnings("unchecked")
        Map<String, String> account = (Map<String, String>) context.get("account");
        
        if (msg.fs().startsWith("wa")) {
            JSONObject args2cdb = new JSONObject();
            args2cdb.put("user", account.get("user"));
            args2cdb.put("pass", account.get("pass.a"));
            FjDscpMessage msg2cdb = new FjDscpMessage();
            msg2cdb.json().put("fs",   server);
            msg2cdb.json().put("ts",   "cdb");
            msg2cdb.json().put("sid",  context.sid());
            msg2cdb.json().put("inst", SkiCommon.ISIS.INST_ECOM_UPDATE_ACCOUNT);
            msg2cdb.json().put("args", args2cdb);
            FjServerToolkit.getSender(server).send(msg2cdb);
            
            return true;
        } else if (msg.fs().startsWith("cdb")) {
            account.put("pass.current", account.get("pass.a"));
            
            if (!context.has("business.updateaccount.times")) context.put("business.updateaccount.times", 1);
            else context.put("business.updateaccount.times", context.getInteger("business.updateaccount.times") + 1);
            
            switch (context.getInteger("business.updateaccount.times")) {
            case 1:
            {
                String user = context.getString("user");
                JSONObject args2wca = new JSONObject();
                args2wca.put("user", user);
                args2wca.put("content", "please deactive account from your device");
                FjDscpMessage msg2wca = new FjDscpMessage();
                msg2wca.json().put("fs",   server);
                msg2wca.json().put("ts",   "wca");
                msg2wca.json().put("sid",  context.sid());
                msg2wca.json().put("inst", SkiCommon.ISIS.INST_USER_RESPONSE);
                msg2wca.json().put("args", args2wca);
                FjServerToolkit.getSender(server).send(msg2wca);
            }
                break;
            case 2:
            {
                String user = context.getString("user");
                JSONObject args2cdb = new JSONObject();
                args2cdb.put("user", user);
                FjDscpMessage msg2wca = new FjDscpMessage();
                msg2wca.json().put("fs",   server);
                msg2wca.json().put("ts",   "cdb");
                msg2wca.json().put("sid",  context.sid());
                msg2wca.json().put("inst", SkiCommon.ISIS.INST_ECOM_APPLY_RETURN);
                msg2wca.json().put("args", args2cdb);
            }
                break;
            default:
                break;
            }
            return true;
        } else {
            logger.error("invalid message, unsupported from: " + msg);
            return false;
        }
    }

}
