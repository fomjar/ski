package com.ski.mma;

import java.util.Arrays;

import org.apache.log4j.Logger;

import com.ski.common.CommonDefinition;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjJsonMessage;
import net.sf.json.JSONArray;
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
        int     code = CommonDefinition.CODE.CODE_SYS_UNKNOWN_ERROR;
        String  desc = null;
        
        if (!dmsg.argsToJsonObject().containsKey("phones")
                || !dmsg.argsToJsonObject().containsKey("smsargs")) {
            code = CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS;
            desc = "illegal arguments, illegal arguments: phones, smsargs";
            logger.error(desc);
            response(server.name(), dmsg, code, desc);
            return;
        }
        
        Object      object  = dmsg.argsToJsonObject().get("phones");
        String[]    phones  = null;
        if (object instanceof String)           phones = new String[] {object.toString()};
        else if (object instanceof JSONArray)   phones = Arrays.asList(((JSONArray) object).toArray()).toArray(new String[((JSONArray) object).size()]);
        else {
            code = CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS;
            desc = "illegal arguments, unrecognized 'phones' format: " + object;
            logger.error(desc);
            response(server.name(), dmsg, code, desc);
            return;
        }
        
        object = dmsg.argsToJsonObject().get("smsargs");
        String[] smsargs = null;
        if (object instanceof String)           smsargs = new String[] {object.toString()};
        else if (object instanceof JSONArray)   smsargs = Arrays.asList(((JSONArray) object).toArray()).toArray(new String[((JSONArray) object).size()]);
        else {
            code = CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS;
            desc = "illegal arguments, unrecognized 'smsargs' format: " + object;
            logger.error(desc);
            response(server.name(), dmsg, code, desc);
            return;
        }
        
        String cid = null;
        switch (dmsg.inst()) {
        case CommonDefinition.ISIS.INST_USER_SUBSCRIBE:
            logger.info(String.format("INST_USER_SUBSCRIBE   - %s:%s", dmsg.fs(), dmsg.sid()));
            cid = FjServerToolkit.getServerConfig("mma.weimi.cid.subscribe");
            break;
        case CommonDefinition.ISIS.INST_USER_AUTHORIZE:
            logger.info(String.format("INST_USER_AUTHORIZE   - %s:%s", dmsg.fs(), dmsg.sid()));
            cid = FjServerToolkit.getServerConfig("mma.weimi.cid.authorize");
            break;
        default:
            logger.error(String.format("unsupported instruct: 0x%08X", dmsg.inst()));
            code = CommonDefinition.CODE.CODE_SYS_ILLEGAL_INST;
            desc = "非法指令";
            break;
        }
        
        FjJsonMessage rsp_weimi = WeimiInterface.sendSms2(
                FjServerToolkit.getServerConfig("mma.weimi.uid"),
                FjServerToolkit.getServerConfig("mma.weimi.pas"),
                phones,
                cid,
                smsargs);
        code = rsp_weimi.json().getInt("code");
        desc = null;
        
        if (CommonDefinition.CODE.CODE_SYS_SUCCESS != code) {
            logger.error("send sms failed: " + rsp_weimi);
            code = CommonDefinition.CODE.CODE_SYS_UNAVAILABLE;
            desc = "系统不可用，请稍候再试";
        }
        
        response(server.name(), dmsg, code, desc);
    }
    
    private static void response(String server, FjDscpMessage req, int code, String desc) {
        JSONObject args = new JSONObject();
        args.put("code", code);
        args.put("desc", desc);
        FjDscpMessage rsp = new FjDscpMessage();
        rsp.json().put("fs",   server);
        rsp.json().put("ts",   req.fs());
        rsp.json().put("sid",  req.sid());
        rsp.json().put("inst", req.inst());
        rsp.json().put("args", args);
        FjServerToolkit.getAnySender().send(rsp);
    }
}
