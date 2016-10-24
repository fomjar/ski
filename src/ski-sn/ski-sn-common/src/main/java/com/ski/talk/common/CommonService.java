package com.ski.talk.common;

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
    
}
