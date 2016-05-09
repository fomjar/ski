package com.ski.stub;

import java.util.List;

import com.ski.common.SkiCommon;

import fomjar.server.FjMessage;
import fomjar.server.FjSender;
import fomjar.server.msg.FjHttpRequest;
import net.sf.json.JSONObject;

public class Service {
    
    private static final String URL_SKI_WSI = "http://www.pan-o.cn:8080/ski-wsi";
    
    public static String getWsiUrl() {return URL_SKI_WSI;}
    
    public static FjMessage send(String report, int inst, JSONObject args) {
        if (null == args) args = new JSONObject();
        
        args.put("report", report);
        args.put("inst", inst);
        FjHttpRequest req = new FjHttpRequest("POST", URL_SKI_WSI, args.toString());
        return FjSender.sendHttpRequest(req);
    }
    
    public static String send(int inst, JSONObject args) {
        return send("cdb", inst, args).toString();
    }
    
    public static List<List<String>> queryGame() {
        String rsp = send(SkiCommon.ISIS.INST_ECOM_QUERY_GAME, null);
        System.out.println(rsp);
        return null;
    }

}
