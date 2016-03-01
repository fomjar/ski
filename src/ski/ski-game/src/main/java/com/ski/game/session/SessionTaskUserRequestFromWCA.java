package com.ski.game.session;

import org.apache.log4j.Logger;

import fomjar.server.FjMessageWrapper;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.session.FjSessionContext;
import fomjar.server.session.FjSessionPath;
import fomjar.server.session.FjSessionTask;
import net.sf.json.JSONObject;

public class SessionTaskUserRequestFromWCA implements FjSessionTask {
    
    private static final Logger logger = Logger.getLogger(SessionTaskUserRequestFromWCA.class);
    
    private static final byte TYPE_MENU = (byte) (1 << 4);

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
        if (args.has(""))
        
        String cmd = args
        if ()
        String content
    }

}
