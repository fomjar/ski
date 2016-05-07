package com.ski.stub;

import fomjar.server.FjMessage;
import fomjar.server.FjSender;
import fomjar.server.msg.FjHttpRequest;
import net.sf.json.JSONObject;

public class Service {
    
    private static final String URL_SKI_WSI = "http://www.pan-o.cn:8080/ski-wsi";
    
    public static String getWsiUrl() {return URL_SKI_WSI;}
    
    public static FjMessage send(String report, int inst, JSONObject args) {
        args.put("report", report);
        args.put("inst", inst);
        FjHttpRequest req = new FjHttpRequest("POST", URL_SKI_WSI, args.toString());
        return FjSender.sendHttpRequest(req);
    }
    
    public static String sendSubmit(int inst, JSONObject args) {
        return send("cdb", inst, args).toString();
    }

}
