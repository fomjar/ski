package com.ski.vcg.web.filter;

import java.nio.channels.SocketChannel;
import java.util.List;

import org.apache.log4j.Logger;

import com.ski.vcg.common.CommonService;
import com.ski.vcg.common.bean.BeanChannelAccount;
import com.ski.vcg.web.wechat.WechatInterface;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.msg.FjJsonMessage;
import fomjar.server.web.FjWebFilter;
import net.sf.json.JSONObject;

public class Filter3WechatAuthorize extends FjWebFilter {

    private static final Logger logger = Logger.getLogger(Filter3WechatAuthorize.class);

    private static final int ANONYMOUS = -1;

    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn) {
        if (request.url().contains("code=")) {
            JSONObject args = request.argsToJson();
            int user = ANONYMOUS;

            if (ANONYMOUS == (user = authorize(args))) return true;

            logger.info("wechat user authorize: " + CommonService.getChannelAccountByCaid(user).getDisplayName());

            String url = request.path();
            if (2 < args.size()) url += "?" + combineArgs(args);
            redirect(response, url);
            response.setcookie("user", Integer.toHexString(user));

            return false;
        }
        return true;
    }

    private static int authorize(JSONObject args) {
        String code = args.getString("code");

        FjJsonMessage rsp = WechatInterface.snsOauth2(FjServerToolkit.getServerConfig("web.wechat.appid"), FjServerToolkit.getServerConfig("web.wechat.secret"), code);
        if (!rsp.json().has("openid")) {
            logger.error("user authorize failed: " + rsp);
            return ANONYMOUS;
        }

        List<BeanChannelAccount> users = CommonService.getChannelAccountByUserNChannel(rsp.json().getString("openid"), CommonService.CHANNEL_WECHAT);
        if (users.isEmpty()) return ANONYMOUS;

        return users.get(0).i_caid;
    }

    @SuppressWarnings("unchecked")
    private static String combineArgs(JSONObject args) {
        StringBuilder sb = new StringBuilder();
        args.forEach((k, v)->{
            if (k.toString().equals("code"))    return; // come from wechat
            if (k.toString().equals("state"))   return; // come from wechat

            if (0 < sb.length()) sb.append("&");
            sb.append(k.toString());
            sb.append("=");

            if (v instanceof Integer) sb.append(Integer.parseInt(v.toString(), 16));
            else sb.append(v.toString());
        });
        return sb.toString();
    }
}
