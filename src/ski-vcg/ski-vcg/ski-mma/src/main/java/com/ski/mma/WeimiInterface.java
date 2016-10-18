package com.ski.mma;

import java.util.Arrays;
import java.util.stream.Collectors;

import fomjar.server.FjSender;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjJsonMessage;

public class WeimiInterface {
    
    private static final String url = "http://api.weimi.cc/2/sms/send.html";
    
    /**
     * √ 触发类短信：验证码 、 订单短信 等，调用短信模版，并可以传入定制化参数；验证码类短信请参考下方注意事项中的“验证码类短信规范”
     * √ 单发或群发；
     * √ 支持子账号；
     * √ 支持定时发送短信；
     * √ 可以做到 免审下发 。
     * 
     * @return
     */
    public static FjJsonMessage sendSms2(String uid, String pas, String[] mobs, String cid, String[] ps) {
        StringBuilder sb = new StringBuilder(url);
        sb.append(String.format("?uid=%s", uid));
        sb.append(String.format("&pas=%s", pas));
        sb.append(String.format("&mob=%s", Arrays.asList(mobs).stream().collect(Collectors.joining(","))));
        sb.append(String.format("&cid=%s", cid));
        if (null != ps) {
            for (int i = 0; i < ps.length; i++) {
                sb.append(String.format("&p%d=%s", i + 1, ps[i]));
            }
        }
        sb.append("&type=json");
        FjHttpRequest req = new FjHttpRequest("GET", sb.toString(), null, null);
        return (FjJsonMessage) FjSender.sendHttpRequest(req);
    }

}
