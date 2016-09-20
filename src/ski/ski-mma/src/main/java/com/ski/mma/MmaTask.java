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
    
    public  static final String URL_KEY = "/ski-mma";
    
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
        
        if (!dmsg.argsToJsonObject().containsKey("user")
                || !dmsg.argsToJsonObject().containsKey("content")) {
            code = CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS;
            desc = "illegal arguments, illegal arguments: user, content";
            logger.error(desc);
            response(server.name(), dmsg, code, desc);
            return;
        }
        
        Object      object  = dmsg.argsToJsonObject().get("user");
        String[]    user    = null;
        if (object instanceof String)           user = new String[] {object.toString()};
        else if (object instanceof JSONArray)   user = Arrays.asList(((JSONArray) object).toArray()).toArray(new String[((JSONArray) object).size()]);
        else {
            code = CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS;
            desc = "illegal arguments, unrecognized 'user' format: " + object;
            logger.error(desc);
            response(server.name(), dmsg, code, desc);
            return;
        }
        
        object = dmsg.argsToJsonObject().get("content");
        String[] content = null;
        if (object instanceof String)           content = new String[] {object.toString()};
        else if (object instanceof JSONArray)   content = Arrays.asList(((JSONArray) object).toArray()).toArray(new String[((JSONArray) object).size()]);
        else {
            code = CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS;
            desc = "illegal arguments, unrecognized 'content' format: " + object;
            logger.error(desc);
            response(server.name(), dmsg, code, desc);
            return;
        }
        
        switch (dmsg.inst()) {
        case CommonDefinition.ISIS.INST_ECOM_APPLY_AUTHORIZE: {
            logger.info(String.format("INST_ECOM_APPLY_AUTHORIZE    - %s:%s", dmsg.fs(), dmsg.sid()));
            FjJsonMessage rsp_weimi = WeimiInterface.sendSms2(
                    FjServerToolkit.getServerConfig("mma.weimi.uid"),
                    FjServerToolkit.getServerConfig("mma.weimi.pas"),
                    user,
                    FjServerToolkit.getServerConfig("mma.weimi.cid.authorize"),
                    content);
            code = rsp_weimi.json().getInt("code");
            desc = null;
            
            if (CommonDefinition.CODE.CODE_SYS_SUCCESS != code) {
                logger.error("send sms failed: " + rsp_weimi);
                code = CommonDefinition.CODE.CODE_SYS_UNAVAILABLE;
                desc = "系统不可用，请稍候再试";
            }
            break;
        }
        case CommonDefinition.ISIS.INST_ECOM_APPLY_RENT_BEGIN: {
            logger.info(String.format("INST_ECOM_APPLY_RENT_BEGIN   - %s:%s", dmsg.fs(), dmsg.sid()));
            FjJsonMessage rsp_weimi = WeimiInterface.sendSms2(
                    FjServerToolkit.getServerConfig("mma.weimi.uid"),
                    FjServerToolkit.getServerConfig("mma.weimi.pas"),
                    user,
                    FjServerToolkit.getServerConfig("mma.weimi.cid.rentbegin"),
                    content);
            code = rsp_weimi.json().getInt("code");
            desc = null;
            
            if (CommonDefinition.CODE.CODE_SYS_SUCCESS != code) {
                logger.error("send sms failed: " + rsp_weimi);
                code = CommonDefinition.CODE.CODE_SYS_UNAVAILABLE;
                desc = "系统不可用，请稍候再试";
            }
            break;
        }
        default:
            logger.error(String.format("unsupported instruction: 0x%08X", dmsg.inst()));
            code = CommonDefinition.CODE.CODE_SYS_ILLEGAL_INST;
            desc = "非法指令";
            break;
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
