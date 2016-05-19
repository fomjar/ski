package com.ski.stub;

import java.util.LinkedHashMap;
import java.util.Map;

import com.ski.common.SkiCommon;
import com.ski.stub.bean.BeanChannelAccount;
import com.ski.stub.bean.BeanGame;
import com.ski.stub.bean.BeanGameAccount;
import com.ski.stub.bean.BeanProduct;

import fomjar.server.FjMessage;
import fomjar.server.FjSender;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import net.sf.json.JSONObject;

public class Service {
    
    private static final String URL_SKI_WSI = "http://www.pan-o.cn:8080/ski-wsi";
    
    public static String getWsiUrl() {return URL_SKI_WSI;}
    
    public static final Map<Integer, BeanGame>              map_game            = new LinkedHashMap<Integer, BeanGame>();
    public static final Map<Integer, BeanProduct>           map_product         = new LinkedHashMap<Integer, BeanProduct>();
    public static final Map<Integer, BeanGameAccount>       map_game_account    = new LinkedHashMap<Integer, BeanGameAccount>();
    public static final Map<Integer, BeanChannelAccount>    map_channel_account = new LinkedHashMap<Integer, BeanChannelAccount>();
    
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
        if (null == rsp) return null;
        
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
                BeanGame bean = new BeanGame(line);
                Service.map_game.put(bean.i_gid, bean);
            }
        }
    }
    
    public static void updateProduct() {
        Service.map_product.clear();
        String rsp = Service.getResponseString(Service.send(SkiCommon.ISIS.INST_ECOM_QUERY_PRODUCT));
        if (null != rsp && !"null".equals(rsp)) {
            String[] lines = rsp.split("\n");
            for (String line : lines) {
                BeanProduct bean = new BeanProduct(line);
                Service.map_product.put(bean.i_pid, bean);
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
                BeanGameAccount bean = new BeanGameAccount(line);
                Service.map_game_account.put(bean.i_gaid, bean);
            }
        }
    }
    
    public static void updateChannelAccount() {
        Service.map_channel_account.clear();
        String rsp = Service.getResponseString(Service.send(SkiCommon.ISIS.INST_ECOM_QUERY_CHANNEL_ACCOUNT));
        if (null != rsp && !"null".equals(rsp)) {
            String[] lines = rsp.split("\n");
            for (String line : lines) {
                BeanChannelAccount bean = new BeanChannelAccount(line);
                Service.map_channel_account.put(bean.i_caid, bean);
            }
        }
    }

}
