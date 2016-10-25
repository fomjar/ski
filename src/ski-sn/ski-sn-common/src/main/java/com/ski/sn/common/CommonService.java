package com.ski.sn.common;

import fomjar.server.FjSender;
import fomjar.server.FjServerToolkit;
import fomjar.server.FjServerToolkit.FjAddress;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import net.sf.json.JSONObject;

public class CommonService {
    
    private static String wsi_host = null;
    public static void setWsiHost(String host) {wsi_host = host;}

    public static String getWsiUrl() {
        String  host = "localhost";
        int     port = 8080;
        if (null == wsi_host) {
            FjAddress addr = FjServerToolkit.getSlb().getAddress("wsi");
            host = addr.host;
            port = addr.port;
        } else host = wsi_host;
        return String.format("http://%s:%d/ski-wsi", host, port);
    }
    
    public static FjDscpMessage request(String report, int inst, JSONObject args) {
        if (null == args) args = new JSONObject();

        args.put("report", report);
        args.put("inst", inst);
        FjHttpRequest req = new FjHttpRequest("POST", getWsiUrl(), "application/json", args.toString());
        FjDscpMessage rsp = (FjDscpMessage) FjSender.sendHttpRequest(req);
        return rsp;
    }

    public static FjDscpMessage request(String report, int inst, JSONObject args, int timeout) {
        if (null == args) args = new JSONObject();

        args.put("report", report);
        args.put("inst", inst);
        FjHttpRequest req = new FjHttpRequest("POST", getWsiUrl(), "application/json", args.toString());
        FjDscpMessage rsp = (FjDscpMessage) FjSender.sendHttpRequest(req, timeout);
        return rsp;
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
