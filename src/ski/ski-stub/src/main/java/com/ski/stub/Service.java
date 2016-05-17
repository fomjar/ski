package com.ski.stub;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
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
    
    public static final Map<Integer, String>    map_game            = new LinkedHashMap<Integer, String>();
    public static final List<Integer>           list_product        = new LinkedList<Integer>();
    public static final Map<Integer, String>    map_game_account    = new LinkedHashMap<Integer, String>();
    public static final Map<String, String>     map_channel_account = new LinkedHashMap<String, String>();
    
    public static FjMessage send(String report, int inst, JSONObject args) {
        if (null == args) args = new JSONObject();
        
        args.put("report", report);
        args.put("inst", inst);
        FjHttpRequest req = new FjHttpRequest("POST", URL_SKI_WSI, args.toString());
        return FjSender.sendHttpRequest(req);
    }
    
    public static FjDscpMessage send(int inst) {
        return send(inst, null);
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
        JSONObject args = rsp.argsToJsonObject();
        if (0 != args.getInt("code")) return null;
        else return args.getJSONArray("desc").getString(0);
    }
    
    public static void updateGame() {
        Service.map_game.clear();
        String rsp = Service.getResponseString(Service.send(SkiCommon.ISIS.INST_ECOM_QUERY_GAME));
        if (null != rsp && !"null".equals(rsp)) {
            String[] lines = rsp.split("\n");
            for (String line : lines) {
                String[] fields = line.split("\t");
                Service.map_game.put(Integer.parseInt(fields[0], 16), fields[7] + "(" + fields[8] + ")");
            }
        }
    }
    
    public static void updateProduct() {
        Service.list_product.clear();
        String rsp = Service.getResponseString(Service.send(SkiCommon.ISIS.INST_ECOM_QUERY_PRODUCT));
        if (null != rsp && !"null".equals(rsp)) {
            String[] lines = rsp.split("\n");
            for (String line : lines) {
                String[] fields = line.split("\t");
                Service.list_product.add(Integer.parseInt(fields[0], 16));
            }
        }
    }
    
    public static void updateGameAccount(int gid) {
        Service.map_game_account.clear();
        JSONObject args = new JSONObject();
        args.put("gid", gid);
        String rsp = Service.getResponseString(Service.send(SkiCommon.ISIS.INST_ECOM_QUERY_GAME_ACCOUNT, args));
        if (null != rsp && !"null".equals(rsp)) {
            String[] lines = rsp.split("\n");
            for (String line : lines) {
                String[] fields = line.split("\t");
                Service.map_game_account.put(Integer.parseInt(fields[0], 16), fields[1]);
            }
        }
    }
    
    public static void updateChannelAccount() {
        Service.map_channel_account.clear();
        String rsp = Service.getResponseString(Service.send(SkiCommon.ISIS.INST_ECOM_QUERY_CHANNEL_ACCOUNT));
        if (null != rsp && !"null".equals(rsp)) {
            String[] lines = rsp.split("\n");
            for (String line : lines) {
                String[] fields = line.split("\t");
                Service.map_channel_account.put(fields[0], fields[1]);
            }
        }
    }

}
