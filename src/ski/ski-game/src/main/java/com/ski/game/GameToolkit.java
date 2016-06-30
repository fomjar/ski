package com.ski.game;

import com.ski.common.CommonDefinition;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class GameToolkit {
    
    public static void sendUserResponse(String server, String sid, String user, String content) {
        JSONObject args2wca = new JSONObject();
        args2wca.put("user", user);
        args2wca.put("content", content);
        
        FjDscpMessage msg2wca = new FjDscpMessage();
        msg2wca.json().put("fs",   server);
        msg2wca.json().put("ts",   "wca");
        msg2wca.json().put("sid",  sid);
        msg2wca.json().put("inst", CommonDefinition.ISIS.INST_USER_RESPONSE);
        msg2wca.json().put("args", args2wca);
        FjServerToolkit.getAnySender().send(msg2wca);
    }
    
}
