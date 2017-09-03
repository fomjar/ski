package com.ski.frs.web.filter;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.ski.frs.web.WebToolkit;

import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.msg.FjISIS;
import fomjar.server.web.FjWebFilter;
import net.sf.json.JSONObject;

public class Filter1Authorize extends FjWebFilter {
    
    private static final Logger logger = Logger.getLogger(Filter1Authorize.class);
    
    private Map<String, String> user;
    private Map<String, String> token;
    
    public Filter1Authorize() {
        user = new HashMap<>();
        token = new HashMap<>();
    }

    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn, FjServer server) {
        updateUser();
        
        if ("/ski-web".equals(request.path()))  return authorizeInterface(response, request);
        if (request.path().endsWith(".html"))   return authorizeDocument(response, request);
        return true;
    }
    
    private void updateUser() {
        synchronized(user) {
            user.clear();
            String[] users = FjServerToolkit.getServerConfig("web.user").split(";", -1);
            for (String u : users) {
                if (0 >= u.length()) continue;
                String[] kv = u.split(":", -1);
                user.put(kv[0], kv[1]);
            }
        }
    }
    
    private boolean authorizeUser(String key, String val) {
        if (val.equals(user.get(key))) {
            String t = UUID.randomUUID().toString().replace("-", "");
            token.put(key, t);
            return true;
        }
        if (val.equals(token.get(key))) return true;
        return false;
    }
    
    private static int[] instruction_exclude = new int[] {};
    
    private boolean authorizeInterface(FjHttpResponse response, FjHttpRequest request) {
        JSONObject args = request.argsToJson();
        if (!args.has("inst")) {
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", FjISIS.CODE_ILLEGAL_INST);
            args_rsp.put("desc", "未知指令");
//            response.code(400);
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
            logger.error(args_rsp);
            return false;
        }
        int inst = WebToolkit.getIntFromArgs(args, "inst");
        for (int ie : instruction_exclude) {
            if (ie == inst) return true;
        }
        if (FjISIS.INST_AUTHORIZE == inst) {
            String key = null;
            String val = null;
            if (args.has("user"))   key = args.getString("user");
            if (args.has("pass"))   val = args.getString("pass");
            if (null == key || null == val || !authorizeUser(key, val)) {
                JSONObject args_rsp = new JSONObject();
                args_rsp.put("code", FjISIS.CODE_UNAUTHORIZED);
                args_rsp.put("desc", "认证失败");
//                response.code(401);
                response.setcookie("token", "");
                response.attr().put("Content-Type", "application/json");
                response.content(args_rsp);
                logger.error(args_rsp);
            } else {
                JSONObject args_rsp = new JSONObject();
                args_rsp.put("code", FjISIS.CODE_SUCCESS);
                args_rsp.put("desc", "认证成功");
                response.setcookie("user",  key);
                response.setcookie("token", token.get(key));
                response.attr().put("Content-Type", "application/json");
                response.content(args_rsp);
            }
            return false;
        } else {
            Map<String, String> cookie = request.cookie();
            String key = null;
            String val = null;
            if (cookie.containsKey("user"))     key = cookie.get("user");
            if (cookie.containsKey("token"))    val = cookie.get("token");
            if (null == key || null == val || !authorizeUser(key, val)) {
                JSONObject args_rsp = new JSONObject();
                args_rsp.put("code", FjISIS.CODE_UNAUTHORIZED);
                args_rsp.put("desc", "认证失败，请重新登陆");
//                response.code(401);
                response.setcookie("token", null);
                response.attr().put("Content-Type", "application/json");
                response.content(args_rsp);
                logger.error(args_rsp);
                return false;
            }
            return true;
        }
    }
    
    private static String[] document_exclude = new String[] {
            "/",
            "/login.html",
            "/404.html",
    };
    
    private boolean authorizeDocument(FjHttpResponse response, FjHttpRequest request) {
		if (request.path().startsWith("/test")) return true;
		if (request.path().startsWith("/page")) return true;
        for (String de : document_exclude) {
            if (de.equals(request.path())) return true;
        }
        
        Map<String, String> cookie = request.cookie();
        String key = cookie.get("user");
        String val = cookie.get("token");
        if (null == key || null == val || !authorizeUser(key, val)) {
            redirect(response, "/login.html");
            return false;
        }
        
        return true;
    }

}
