package com.ski.stub;

import java.util.LinkedHashMap;
import java.util.Map;

import com.ski.common.SkiCommon;

import fomjar.server.FjMessage;
import fomjar.server.FjSender;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import net.sf.json.JSONObject;

public class Service {
    
    private static final String URL_SKI_WSI = "http://www.pan-o.cn:8080/ski-wsi";
    
    public static String getWsiUrl() {return URL_SKI_WSI;}
    
    public static final Map<Integer, String>                map_games       = new LinkedHashMap<Integer, String>();
    public static final Map<Integer, Map<Integer, String>>  map_accounts    = new LinkedHashMap<Integer, Map<Integer, String>>();
    
    public static FjMessage send(String report, int inst, JSONObject args) {
        if (null == args) args = new JSONObject();
        
        args.put("report", report);
        args.put("inst", inst);
        FjHttpRequest req = new FjHttpRequest("POST", URL_SKI_WSI, args.toString());
        return FjSender.sendHttpRequest(req);
    }
    
    public static FjDscpMessage send(int inst, JSONObject args) {
        FjMessage rsp = send("cdb", inst, args);
        System.out.println(rsp);
        return (FjDscpMessage) rsp;
    }
    
    public static boolean isResponseSuccess(FjDscpMessage rsp) {
        JSONObject args = rsp.argsToJsonObject();
        if (0 == args.getInt("code")) return true;
        else return false;
    }
    
    public static String getResponseString(FjDscpMessage rsp) {
        return rsp.argsToJsonObject().getJSONArray("desc").getString(0);
    }
    
    public static void updateGames() {
        Service.map_games.clear();
        String rsp = Service.getResponseString(Service.send(SkiCommon.ISIS.INST_ECOM_QUERY_GAME, null));
        if (null != rsp && !"null".equals(rsp)) {
            String[] lines = rsp.split("\n");
            for (String line : lines) {
                String[] fields = line.split("\t");
                Service.map_games.put(Integer.parseInt(fields[0], 16), fields[7] + "(" + fields[8] + ")");
            }
        }
    }

}
