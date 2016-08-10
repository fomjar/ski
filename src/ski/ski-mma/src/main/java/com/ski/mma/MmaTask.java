package com.ski.mma;

import org.apache.log4j.Logger;

import com.ski.common.CommonDefinition;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class MmaTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(MmaTask.class);
    
    public MmaTask() {
    }

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper){
        FjMessage msg = wrapper.message();
        if (!(msg instanceof FjDscpMessage)) {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
            return;
        }
        
        FjDscpMessage dmsg = (FjDscpMessage) msg;
        switch (dmsg.inst()) {
        case CommonDefinition.ISIS.INST_USER_AUTHORIZE:
            logger.info(String.format("INST_USER_AUTHORIZE   - %s:%s", dmsg.fs(), dmsg.sid()));
            {
            	JSONObject args = new JSONObject();
            	args.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
            	args.put("desc", null);
            	FjDscpMessage rsp = new FjDscpMessage();
            	rsp.json().put("fs", server.name());
            	rsp.json().put("ts", dmsg.fs());
            	rsp.json().put("inst", dmsg.inst());
            	rsp.json().put("args", args);
            	FjServerToolkit.getAnySender().send(rsp);
            }
            break;
        default:
            logger.error(String.format("unsupported instruct: 0x%08X", dmsg.inst()));
            break;
        }
    }
}
