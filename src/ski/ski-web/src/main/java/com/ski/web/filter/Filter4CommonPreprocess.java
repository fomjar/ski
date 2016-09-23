package com.ski.web.filter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.ski.common.CommonDefinition;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.web.FjWebFilter;
import net.sf.json.JSONObject;

public class Filter4CommonPreprocess extends FjWebFilter {
    
    private static final Logger logger = Logger.getLogger(Filter4CommonPreprocess.class);

    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn) {
        if (request.path().endsWith(".html")) {
            JSONObject args = request.argsToJson();
            int user = -1;
            
            if (request.cookie().containsKey("user")) user = Integer.parseInt(request.cookie().get("user"), 16);
            else if (args.containsKey("user")) {
                Object obj = args.get("user");
                if (obj instanceof Integer) user = (int) obj;
                else user = Integer.parseInt(obj.toString(), 16);
                response.cookie().put("user", Integer.toHexString(user));
                response.setcookie("user", Integer.toHexString(user));
            } else {
                if (!request.path().equals("/wechat/message.html")) {
                    logger.info("anonymous access deny: " + request.url());
                    redirect(response, "/wechat/message.html?msg_type=warn&msg_title=谢绝游客&msg_content=请关注微信“VC电玩”，然后从微信访问我们，非常感谢！");
                    return false;
                }
            }
            recordaccess(user, conn, request.url());
        }
        
        return true;
    }
    
    private static void recordaccess(int user, SocketChannel conn, String url) {
        try {
            String remote = String.format("%15s|%6d",
                    ((InetSocketAddress)conn.getRemoteAddress()).getHostName(),
                    ((InetSocketAddress)conn.getRemoteAddress()).getPort());
            String local = String.format("%25s|%6d|%s",
                    ((InetSocketAddress)conn.getLocalAddress()).getHostName(),
                    ((InetSocketAddress)conn.getLocalAddress()).getPort(),
                    url);
            JSONObject args = new JSONObject();
            args.put("caid",     user);
            args.put("remote",     remote);
            args.put("local",     local);
            FjDscpMessage msg = new FjDscpMessage();
            msg.json().put("fs", FjServerToolkit.getAnyServer().name());
            msg.json().put("ts", "cdb");
            msg.json().put("inst", CommonDefinition.ISIS.INST_ECOM_UPDATE_ACCESS_RECORD);
            msg.json().put("args", args);
            FjServerToolkit.getAnySender().send(msg);
        } catch (IOException e) {e.printStackTrace();}
    }
}
