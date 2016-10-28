package com.ski.sn.ura;

import org.apache.log4j.Logger;

import com.ski.sn.common.CommonDefinition;
import com.ski.sn.common.CommonService;
import com.ski.sn.ura.res.WEIMI;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjJsonMessage;
import net.sf.json.JSONObject;

public class UraTask implements FjServer.FjServerTask {
    
    private static final Logger logger = Logger.getLogger(UraTask.class);

    @Override
    public void initialize(FjServer server) {}

    @Override
    public void destroy(FjServer server) {}

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (!(msg instanceof FjDscpMessage)) {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
            return;
        }
        
        FjDscpMessage dmsg = (FjDscpMessage) msg;
        switch (dmsg.inst()) {
        case CommonDefinition.ISIS.INST_APPLY_VERIFY:
            applyVerify(dmsg);
            break;
        }
    }
    
    private static void applyVerify(FjDscpMessage request) {
        JSONObject  args    = request.argsToJsonObject();
        String      phone   = args.getString("phone");
        String      vcode   = args.getString("vcode");

        FjJsonMessage rsp_weimi = WEIMI.sendSms2(
                FjServerToolkit.getServerConfig("ura.weimi.uid"),
                FjServerToolkit.getServerConfig("ura.weimi.pas"),
                new String[] {phone},
                FjServerToolkit.getServerConfig("ura.weimi.cid.verify"),
                new String[] {vcode});
        int code = rsp_weimi.json().getInt("code");

        if (CommonDefinition.CODE.CODE_SUCCESS != code) {
            logger.error("send sms failed: " + rsp_weimi);
            CommonService.response(request, CommonDefinition.CODE.CODE_INTERNAL_ERROR, "系统不可用，请稍候再试");
            return;
        }
        
        CommonService.response(request, CommonDefinition.CODE.CODE_SUCCESS, null);
    }

}
