package com.ski.sn.bcs;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.ski.sn.common.CommonDefinition;
import com.ski.sn.common.CommonService;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class BcsTask implements FjServer.FjServerTask {
    
    private static final Logger logger = Logger.getLogger(BcsTask.class);
    
    private Map<String, FjDscpMessage> cache_request;
    
    @Override
    public void initialize(FjServer server) {
        cache_request = new HashMap<String, FjDscpMessage>();
    }

    @Override
    public void destroy(FjServer server) {
        cache_request.clear();
    }

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (!(msg instanceof FjDscpMessage)) {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
            return;
        }

        FjDscpMessage dmsg = (FjDscpMessage) msg;
        try {
            if (dmsg.fs().startsWith("wsi")) { // 用户侧请求
                logger.info(String.format("[ REQUEST  ] - %s:%s:0x%08X", dmsg.fs(), dmsg.sid(), dmsg.inst()));
                processRequest(dmsg);
            } else { // 平台侧响应
                logger.info(String.format("[ RESPONSE ] - %s:%s:0x%08X", dmsg.fs(), dmsg.sid(), dmsg.inst()));
                if (cache_request.containsKey(dmsg.sid())) processResponse(dmsg, cache_request.remove(dmsg.sid()));
            }
        } catch (Exception e) {
            logger.error("unexpected error occurred by message: " + dmsg, e);
            CommonService.response(dmsg, CommonDefinition.CODE.CODE_ERROR, "发生了一个奇怪的错误😱");
        }
    }
    
    private void catchResponse(FjDscpMessage request) {
        cache_request.put(request.sid(), request);
    }
    
    public void processRequest(FjDscpMessage request) {
        switch (request.inst()) {
        case CommonDefinition.ISIS.INST_APPLY_AUTHORIZE:
            requestApplyAuthorize(request);
            break;
        case CommonDefinition.ISIS.INST_APPLY_VERIFY:
            requestApplyVerify(request);
            break;
        case CommonDefinition.ISIS.INST_UPDATE_MESSAGE:
            requestUpdateMessage(request);
            break;
        case CommonDefinition.ISIS.INST_UPDATE_USER:
            requestUpdateUser(request);
            break;
        case CommonDefinition.ISIS.INST_UPDATE_USER_STATE:
            requestUpdateUserState(request);
            break;
        default:
            logger.error("illegal inst: " + request);
            CommonService.response(request, CommonDefinition.CODE.CODE_ILLEGAL_INST, "未知指令");
            break;
        }
    }
    
    public void processResponse(FjDscpMessage response, FjDscpMessage request) {
        JSONObject args = response.argsToJsonObject();
        switch(response.inst()) {
        case CommonDefinition.ISIS.INST_APPLY_AUTHORIZE:
            responseApplyAuthorize(args, request);
            break;
        case CommonDefinition.ISIS.INST_QUERY_USER_STATE:
            responseQueryUserState(args, request);
            break;
        case CommonDefinition.ISIS.INST_UPDATE_MESSAGE:
            break;
        default:
            if (CommonDefinition.CODE.CODE_SUCCESS != CommonService.getResponseCode(response))
                args.put("desc", "发生了一个奇怪的错误😱");
            break;
        }
        response.json().put("fs",   FjServerToolkit.getAnyServer().name());
        response.json().put("ts",   request.fs());
        response.json().put("args", args);
        FjServerToolkit.getAnySender().send(response);
    }
    
    private void requestApplyAuthorize(FjDscpMessage request) {
        JSONObject args = request.argsToJsonObject();
        if (args.has("token")) {    // 自动登录
            if (!illegalArgs(request, "token", "uid")) return;
            
            String token = args.getString("token");
            String uid   = args.getString("uid");
            
            JSONObject args_cdb = new JSONObject();
            args_cdb.put("token", token);
            args_cdb.put("uid",   uid);
            CommonService.requesta("cdb", request.sid(), CommonDefinition.ISIS.INST_QUERY_USER_STATE, args_cdb);
            catchResponse(request);
        } else {    // 手动登录
            if (!illegalArgs(request, "phone", "pass")) return;
            
            String phone = args.getString("phone");
            String pass  = args.getString("pass");
            
            JSONObject args_cdb = new JSONObject();
            args_cdb.put("phone", phone);
            args_cdb.put("pass",  pass);
            CommonService.requesta("cdb", request.sid(), CommonDefinition.ISIS.INST_APPLY_AUTHORIZE, args_cdb);
            catchResponse(request);
        }
    }
    
    private void responseApplyAuthorize(JSONObject args, FjDscpMessage request) {
        if (0 == args.getJSONArray("desc").size()) {
            logger.error("用户名或密码错误: " + request);
            args.put("code", CommonDefinition.CODE.CODE_ERROR);
            args.put("desc", "用户名或密码错误");
            return;
        }
        
        JSONArray user = args.getJSONArray("desc").getJSONArray(0);
        String token = UUID.randomUUID().toString().replace("-", "");
        {
            JSONObject args_cdb = new JSONObject();
            args_cdb.put("uid",         Integer.parseInt(user.getString(0)));
            args_cdb.put("state",       CommonDefinition.Field.USER_STATE_ONLINE);
            args_cdb.put("token",       token);
            CommonService.requesta("cdb", CommonDefinition.ISIS.INST_UPDATE_USER_STATE, args_cdb);
        }
        
        JSONObject desc = new JSONObject();
        desc.put("uid",     Integer.parseInt(user.getString(0)));
        desc.put("create",  user.getString(1));
        desc.put("phone",   user.getString(2));
        desc.put("email",   user.getString(3));
        desc.put("name",    user.getString(4));
        desc.put("cover",   user.getString(5));
        desc.put("token",   token);
        
        args.put("code", CommonDefinition.CODE.CODE_SUCCESS);
        args.put("desc", desc);
    }
    
    private void responseQueryUserState(JSONObject args, FjDscpMessage request) {
        if (0 == args.getJSONArray("desc").size()) {
            logger.error("缓存已失效，请重新登录: " + request);
            args.put("code", CommonDefinition.CODE.CODE_ERROR);
            args.put("desc", "缓存已失效，请重新登录");
            return;
        }
        JSONArray user = args.getJSONArray("desc").getJSONArray(0);
        {
            JSONObject args_cdb = new JSONObject();
            args_cdb.put("uid",         Integer.parseInt(user.getString(0)));
            args_cdb.put("state",       CommonDefinition.Field.USER_STATE_ONLINE);
            CommonService.requesta("cdb", CommonDefinition.ISIS.INST_UPDATE_USER_STATE, args_cdb);
        }
        
        JSONObject desc = new JSONObject();
        desc.put("uid",     Integer.parseInt(user.getString(0)));
        desc.put("create",  user.getString(1));
        desc.put("phone",   user.getString(2));
        desc.put("email",   user.getString(3));
        desc.put("name",    user.getString(4));
        desc.put("cover",   user.getString(5));
        desc.put("token",   request.argsToJsonObject().getString("token"));
        
        args.put("code", CommonDefinition.CODE.CODE_SUCCESS);
        args.put("desc", desc);
    }
    
    private Map<String, String> cache_vcode = new ConcurrentHashMap<String, String>();
    private void requestApplyVerify(FjDscpMessage request) {
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
                CommonService.requesta("ura", request.sid(), CommonDefinition.ISIS.INST_APPLY_VERIFY, args_ura);
                catchResponse(request);
                cache_vcode.put(phone, vcode); // for verify
            }
            break;
        }
    }
    
    private void requestUpdateMessage(FjDscpMessage request) {
        if (!illegalArgs(request, "uid", "coosys", "lat", "lng")) return;
        
        JSONObject args = request.argsToJsonObject();
        int     uid     = args.getInt("uid");
        int     coosys  = args.getInt("coosys");
        double  lat     = args.getDouble("lat");
        double  lng     = args.getDouble("lng");
        String  text    = args.has("text") ? args.getString("text") : null;
        String  image   = args.has("image") ? args.getString("image") : null;
        String  geohash = GeoHash.encode(lat, lng);
        String  mid     = String.format("%s:%d", geohash, System.currentTimeMillis());
        
        JSONObject args_cdb = new JSONObject();
        args_cdb.put("mid",     mid);
        args_cdb.put("uid",     uid);
        args_cdb.put("coosys",  coosys);
        args_cdb.put("lat",     lat);
        args_cdb.put("lng",     lng);
        args_cdb.put("geohash", geohash);
        args_cdb.put("text",    text);
        args_cdb.put("image",   image);
        
        CommonService.requesta("cdb", request.sid(), CommonDefinition.ISIS.INST_UPDATE_MESSAGE, args_cdb);
        catchResponse(request);
    }
    
    private void requestUpdateUser(FjDscpMessage request) {
        if (!illegalArgs(request, "pass", "phone", "vcode", "name")) return;
        
        JSONObject args = request.argsToJsonObject();
        String phone = args.getString("phone");
        String vcode = args.getString("vcode");
        if (!vcode.equals(cache_vcode.get(phone))) {
            CommonService.response(request, CommonDefinition.CODE.CODE_ERROR, "验证失败");
            return;
        }
//        cache_vcode.remove(phone);
        
        JSONObject args_cdb = new JSONObject();
        args_cdb.put("pass",    args.getString("pass"));
        args_cdb.put("phone",   args.getString("phone"));
        args_cdb.put("name",    args.getString("name"));
        if (args.has("cover"))  args_cdb.put("cover", args.getString("cover")); // data:image/jpeg;base64,/9j/4SxpRXhpZgA...
        CommonService.requesta("cdb", request.sid(), CommonDefinition.ISIS.INST_UPDATE_USER, args_cdb);
        catchResponse(request);
    }
    
    private void requestUpdateUserState(FjDscpMessage request) {
        if (!illegalArgs(request, "uid")) return;
        
        CommonService.requesta("cdb", CommonDefinition.ISIS.INST_UPDATE_USER_STATE, request.argsToJsonObject());
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
