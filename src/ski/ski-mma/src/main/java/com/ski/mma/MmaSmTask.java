package com.ski.mma;

import org.apache.log4j.Logger;

import com.ski.common.SkiCommon;
import com.taobao.api.ApiException;
import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.TaobaoClient;
import com.taobao.api.request.OpenSmsSendvercodeRequest;
import com.taobao.api.request.OpenSmsSendvercodeRequest.SendVerCodeRequest;
import com.taobao.api.response.OpenSmsSendvercodeResponse;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class MmaSmTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(MmaSmTask.class);
    
    private TaobaoClient client;
    private OpenSmsSendvercodeRequest request;
    private SendVerCodeRequest svcr;
    
    public MmaSmTask() {
        client  = new DefaultTaobaoClient("http://gw.api.taobao.com/router/rest",
                FjServerToolkit.getServerConfig("sm.key"),
                FjServerToolkit.getServerConfig("sm.secret"));
        request = new OpenSmsSendvercodeRequest();
        svcr    = new SendVerCodeRequest();
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
        case SkiCommon.ISIS.INST_USER_RESPONSE:
            logger.info(String.format("INST_USER_RESPONSE    - %s:%s", dmsg.fs(), dmsg.sid()));
            sendShortMessage(dmsg.argsToJsonObject());
            break;
        default:
            String inst = Integer.toHexString(dmsg.inst());
            while (8 > inst.length()) inst = "0" + inst;
            logger.error("unsupported instruct: 0x" + inst);
            break;
        }
    }
    
    private void sendShortMessage(JSONObject args) {
        String    phone       = args.getString("phone");
        JSONArray context_arg = args.getJSONArray("context");
        
        svcr.setTemplateId(Long.parseLong(FjServerToolkit.getServerConfig("sm.template")));
        svcr.setSignatureId(Long.parseLong(FjServerToolkit.getServerConfig("sm.signature")));
        svcr.setDeviceId(phone);
        svcr.setDeviceLimit(1000L);
        svcr.setDeviceLimitInTime(1000L);
        svcr.setMobile(phone);
        String context = FjServerToolkit.getServerConfig("sm.context");
        context = String.format(context, context_arg.toArray());
        svcr.setContext(context);
        request.setSendVerCodeRequest(svcr);
        try {
            OpenSmsSendvercodeResponse rsp = client.execute(request);
            logger.debug(String.format("send user(%s) response: %s", phone, context));
            logger.info(rsp.getBody());
        } catch (ApiException e) {
            logger.error("error occurs when send short message", e);
        }
    }

}
