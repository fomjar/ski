package com.ski.wca;

import java.awt.geom.Point2D;

import fomjar.server.FjSender;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjJsonMessage;
import net.sf.json.JSONObject;

public class BaiduMapInterface {
    
    private static String host = "api.map.baidu.com";
    
    public static void setHost(String host) {BaiduMapInterface.host = host;}
    public static String host() {return host;}
    
    public static FjJsonMessage sendRequest(String method, String url, String content) {
        FjJsonMessage rsp = (FjJsonMessage) FjSender.sendHttpRequest(new FjHttpRequest(method, url, FjHttpRequest.CT_TEXT_PLAIN, content));
        return rsp;
    }
    
    /**
     * {
     * "status":0,
     * "message":"ok",
     * "results":[
     * {
     * "name":"上海市",
     * "location":{
     * "lat":31.236305,
     * "lng":121.480237
     * },
     * "uid":"4141110d95d0f74fefe4a5f0"
     * }
     * ]
     * }
     * @return
     */
    public static Point2D.Double getCordinate(String ak, String place) {
        String url = String.format("http://%s/place/v2/search?q=%s&region=全国&output=json&ak=%s", host(), place.replace(" ", "_"), ak);
        FjJsonMessage rsp = sendRequest("GET", url, null);
        if (0 != rsp.json().getInt("status")) return null;
        
        JSONObject location = rsp.json().getJSONArray("results").getJSONObject(0).getJSONObject("location");
        return new Point2D.Double(location.getDouble("lat"), location.getDouble("lng"));
    }

}
