package com.ski.xs.common;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class CommonService {
    
    public static FjDscpMessage request(String ts, int inst, JSONObject args) {
        FjDscpMessage request = new FjDscpMessage();
        request.json().put("fs",   FjServerToolkit.getAnyServer().name());
        request.json().put("ts",   ts);
        request.json().put("inst", inst);
        request.json().put("args", args);
        FjServerToolkit.getAnySender().send(request);
        return request;
    }
    
    public static FjDscpMessage request(String ts, String sid, int inst, JSONObject args) {
        FjDscpMessage request = new FjDscpMessage();
        request.json().put("fs",   FjServerToolkit.getAnyServer().name());
        request.json().put("ts",   ts);
        request.json().put("sid",  sid);
        request.json().put("inst", inst);
        request.json().put("args", args);
        FjServerToolkit.getAnySender().send(request);
        return request;
    }
    
    public static void response(FjDscpMessage request, int code, Object desc) {
        JSONObject args = new JSONObject();
        args.put("code", code);
        args.put("desc", desc);
        FjDscpMessage response = new FjDscpMessage();
        response.json().put("fs",   FjServerToolkit.getAnyServer().name());
        response.json().put("ts",   request.fs());
        response.json().put("sid",  request.sid());
        response.json().put("inst", request.inst());
        response.json().put("args", args);
        FjServerToolkit.getAnySender().send(response);
    }
    
    public static boolean isResponseSuccess(FjDscpMessage rsp) {
        if (null == rsp) return false;

        if (CommonDefinition.CODE.CODE_SUCCESS == getResponseCode(rsp)) return true;
        else return false;
    }

    public static int getResponseCode(FjDscpMessage rsp) {
        if (null == rsp) return -1;

        return rsp.argsToJsonObject().getInt("code");
    }

    public static String getResponseDescToString(FjDscpMessage rsp) {
        if (null == rsp) return null;

        JSONObject args = rsp.argsToJsonObject();
        if (args.has("desc")) return args.get("desc").toString();
        else return null;
    }
    
}
