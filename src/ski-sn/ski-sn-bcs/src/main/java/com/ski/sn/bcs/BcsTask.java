package com.ski.sn.bcs;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.ski.sn.common.CommonDefinition;
import com.ski.sn.common.CommonService;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class BcsTask implements FjServer.FjServerTask {
    
    private static final Logger logger = Logger.getLogger(BcsTask.class);
    
    private Map<String, FjDscpMessage> cache;
    
    @Override
    public void initialize(FjServer server) {
        cache = new HashMap<String, FjDscpMessage>();
    }

    @Override
    public void destroy(FjServer server) {
        cache.clear();
    }

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (!(msg instanceof FjDscpMessage)) {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
            return;
        }

        FjDscpMessage dmsg = (FjDscpMessage) msg;
        if (server.name().startsWith("wsi")) { // 用户侧请求
            logger.info(String.format("[ REQUEST  ] - %s:%s:0x%08X", dmsg.fs(), dmsg.sid(), dmsg.inst()));
            processRequest(dmsg);
        } else { // 平台侧响应
            logger.info(String.format("[ RESPONSE ] - %s:%s:0x%08X", dmsg.fs(), dmsg.sid(), dmsg.inst()));
            processResponse(dmsg, cache.remove(dmsg.sid()));
        }
    }
    
    public static void processRequest(FjDscpMessage request) {
        switch (request.inst()) {
        case CommonDefinition.ISIS.INST_APPLY_VERIFY:
            applyVerify(request);
            break;
        case CommonDefinition.ISIS.INST_UPDATE_USER:
            updateUser(request);
            break;
        }
    }
    
    public static void processResponse(FjDscpMessage response, FjDscpMessage request) {
        if (null == request) {
            logger.warn("find no request for response: " + response);
            return;
        }
        
        response.json().put("fs", FjServerToolkit.getAnyServer().name());
        response.json().put("ts", request.fs());
        FjServerToolkit.getAnySender().send(response);
    }
    
    private static Map<String, String> cache_vcode = new ConcurrentHashMap<String, String>();
    private static void applyVerify(FjDscpMessage request) {
        if (!illegalArgs(request, "type")) return;
        
        JSONObject args = request.argsToJsonObject();
        switch (args.getString("type")) {
        case "phone":
            if (!illegalArgs(request, "phone")) break;
            
            String phone = args.getString("phone");
            if (args.has("vcode")) {
                String vcode = args.getString("vcode");
                if (vcode.equals(cache_vcode.get(phone))) {
                    CommonService.response(request, CommonDefinition.CODE.CODE_SUCCESS, null);
                } else {
                    CommonService.response(request, CommonDefinition.CODE.CODE_ERROR, "验证失败");
                }
            } else {
                String time  = String.valueOf(System.currentTimeMillis());
                String vcode = time.substring(time.length() - 4);
                JSONObject args_ura = new JSONObject();
                args_ura.put("phone", phone);
                args_ura.put("vcode", vcode);
                CommonService.requesta("ura", CommonDefinition.ISIS.INST_APPLY_VERIFY, args_ura);
                cache_vcode.put(phone, vcode);
                CommonService.response(request, CommonDefinition.CODE.CODE_SUCCESS, null);
            }
            break;
        }
    }
    
    private static void updateUser(FjDscpMessage request) {
        if (!illegalArgs(request, "phone", "vcode", "name", "cover")) return;
        
        JSONObject args = request.argsToJsonObject();
        String phone = args.getString("phone");
        String vcode = args.getString("vcode");
        String name  = args.getString("name");
        String cover = args.getString("cover");
    }
    
    private static boolean illegalArgs(FjDscpMessage request, String... keys) {
        JSONObject args = request.argsToJsonObject();
        for (String key : keys) {
            if (!args.has(key)) {
                logger.error("illegal arguments, no: " + key);
                CommonService.response(request, CommonDefinition.CODE.CODE_ILLEGAL_ARGS, "参数错误");
                return false;
            }
        }
        return true;
    }

}
