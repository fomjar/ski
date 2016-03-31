package com.ski.game.session;

import com.ski.game.GameToolkit;

import fomjar.server.FjMessageWrapper;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.session.FjSessionContext;
import fomjar.server.session.FjSessionPath;
import fomjar.server.session.FjSessionTask;

public class SessionTaskMismatch implements FjSessionTask {

    @Override
    public boolean onSession(FjSessionContext context, FjSessionPath path, FjMessageWrapper wrapper) {
        FjDscpMessage msg = (FjDscpMessage) wrapper.message();
        
        if (msg.fs().startsWith("wca")) {
            GameToolkit.sendUserResponse(context.server(), context.sid(), msg.sid(), "操作错误，请重新操作！");
        }
        
        return false;
    }

}
