package com.ski.mma;

import org.apache.log4j.Logger;

import com.ski.common.CommonDefinition;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjJsonMessage;
import net.sf.json.JSONObject;

public class MmaTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(MmaTask.class);
    
	@Override
	public void initialize(FjServer server) {}

	@Override
	public void destroy(FjServer server) {}

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
        	String phone = dmsg.argsToJsonObject().getString("phone");
        	String verify = dmsg.argsToJsonObject().getString("verify");
        	
        	FjJsonMessage rsp_weimi = WeimiInterface.sendSms2(
        			FjServerToolkit.getServerConfig("mma.weimi.uid"),
        			FjServerToolkit.getServerConfig("mma.weimi.pas"),
        			new String[] {phone},
        			FjServerToolkit.getServerConfig("mma.weimi.cid.verify"),
        			new String[] {verify});
        	int 	code = rsp_weimi.json().getInt("code");
        	String 	desc = null;
        	if (CommonDefinition.CODE.CODE_SYS_SUCCESS != code) {
        		logger.error("send sms failed: " + rsp_weimi);
        		code = CommonDefinition.CODE.CODE_SYS_UNAVAILABLE;
        		desc = "系统不可用，请稍候再试";
        	}
        	
        	JSONObject args = new JSONObject();
        	args.put("code", code);
        	args.put("desc", desc);
        	FjDscpMessage rsp = new FjDscpMessage();
        	rsp.json().put("fs",   server.name());
        	rsp.json().put("ts",   dmsg.fs());
        	rsp.json().put("sid",  dmsg.sid());
        	rsp.json().put("inst", dmsg.inst());
        	rsp.json().put("args", args);
        	FjServerToolkit.getAnySender().send(rsp);
            break;
        default:
            logger.error(String.format("unsupported instruct: 0x%08X", dmsg.inst()));
            break;
        }
    }
}
