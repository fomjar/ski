package com.ski.mma;

import org.apache.log4j.Logger;

import com.ski.common.DSCP;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.OpenSmsSendvercodeRequest;
import com.taobao.api.request.OpenSmsSendvercodeRequest.SendVerCodeRequest;
import com.taobao.api.response.OpenSmsSendvercodeResponse;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjMessage;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class MmaSmTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(MmaSmTask.class);

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper){
        FjMessage msg = wrapper.message();
        if (!(msg instanceof FjDscpMessage)) {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
            return;
        }
        
        FjDscpMessage dmsg = (FjDscpMessage) msg;
        switch (dmsg.cmd()) {
        case DSCP.CMD.USER_RESPONSE:
            logger.info(String.format("USER_RESPONSE    - %s:%s", dmsg.fs(), dmsg.sid()));
            sendShortMessage(dmsg.argToJsonObject());
            break;
        default:
            String cmd = Integer.toHexString(dmsg.cmd());
            while (8 > cmd.length()) cmd = "0" + cmd;
            logger.error("unsupported command: 0x" + cmd);
            break;
        }
    }
    
    private static void sendShortMessage(JSONObject arg) {
        String    phone       = arg.getString("phone");
        JSONArray context_arg = arg.getJSONArray("context");
        
        TaobaoClient client = new DefaultTaobaoClient("http://gw.api.taobao.com/router/rest",
                FjServerToolkit.getServerConfig("sm.key"),
                FjServerToolkit.getServerConfig("sm.secret"));
        OpenSmsSendvercodeRequest req = new OpenSmsSendvercodeRequest();
        SendVerCodeRequest svcr = new SendVerCodeRequest();
        svcr.setTemplateId(Long.parseLong(FjServerToolkit.getServerConfig("sm.template")));
        svcr.setSignatureId(Long.parseLong(FjServerToolkit.getServerConfig("sm.signature")));
        svcr.setDeviceId(phone);
        svcr.setDeviceLimit(1000L);
        svcr.setDeviceLimitInTime(1000L);
        svcr.setMobile(phone);
        String context = FjServerToolkit.getServerConfig("sm.context");
        context = String.format(context, context_arg.toArray());
        svcr.setContext(context);
        req.setSendVerCodeRequest(svcr);
        try {
            OpenSmsSendvercodeResponse rsp = client.execute(req);
            logger.debug(String.format("send user(%s) response: %s", phone, context));
            logger.info(rsp.getBody());
        } catch (ApiException e) {
            logger.error("error occurs when send short message", e);
        }
    }

}
