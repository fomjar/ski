package com.ski.xs.web.filter;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import com.ski.xs.common.CommonDefinition;

import fomjar.server.FjServer;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.web.FjWebFilter;
import net.sf.json.JSONObject;

public class Filter1Authorize extends FjWebFilter {
    
    private static final Logger logger = Logger.getLogger(Filter1Authorize.class);

    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn, FjServer server) {
        if ("/ski-web".equals(request.path()))  return authorizeInterface(response, request);
        if (request.path().endsWith(".html"))   return authorizeDocument(response, request);
        return true;
    }
    
    private static int[] instruction_exclude = new int[] {
    };
    
    private static boolean authorizeInterface(FjHttpResponse response, FjHttpRequest request) {
        if (!request.argsToJson().has("inst")) {
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_ILLEGAL_INST);
            args_rsp.put("desc", "未知指令");
            response.code(400);
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
            logger.error(args_rsp);
            return false;
        }
        for (int ie : instruction_exclude) {
            if (ie == FilterToolkit.getIntFromArgs(request.argsToJson(), "inst")) return true;
        }
        if (!request.cookie().containsKey("token") || !request.cookie().containsKey("uid")) {
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_UNAUTHORIZED);
            args_rsp.put("desc", "请先登录");
            response.code(401);
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
            logger.error(args_rsp);
            return false;
        }
//        String token    = request.cookie().get("token");
//        long   uid     = Long.parseLong(request.cookie().get("uid"));
//        JSONObject args_bcs = new JSONObject();
//        args_bcs.put("token",   token);
//        args_bcs.put("uid",     uid);
//        FjDscpMessage rsp = CommonService.requests("bcs", CommonDefinition.ISIS.INST_APPLY_AUTHORIZE, args_bcs);
//        if (!CommonService.isResponseSuccess(rsp)) {
//            JSONObject args_rsp = new JSONObject();
//            args_rsp.put("code", CommonService.getResponseCode(rsp));
//            args_rsp.put("desc", CommonService.getResponseDescToString(rsp));
//            response.code(401);
//            response.attr().put("Content-Type", "application/json");
//            response.content(args_rsp);
//            logger.error(args_rsp);
//            return false;
//        }
        
        return true;
    }
    
    private static String[] document_exclude = new String[] {
            "/",
            "/index.html",
            "/404.html",
            "/CrazyHuman/bin/index.html",
    };
    
    private static boolean authorizeDocument(FjHttpResponse response, FjHttpRequest request) {
        for (String de : document_exclude) {
            if (de.equals(request.path())) return true;
        }
        if (request.path().startsWith("/test")) return true;
        if (!request.cookie().containsKey("token") || !request.cookie().containsKey("uid")) {
            redirect(response, "/");
            return false;
        }
//        String token = request.cookie().get("token");
//        long   uid   = Long.parseLong(request.cookie().get("uid"));
//        JSONObject args_bcs = new JSONObject();
//        args_bcs.put("token",   token);
//        args_bcs.put("uid",     uid);
//        FjDscpMessage rsp = CommonService.requests("bcs", CommonDefinition.ISIS.INST_APPLY_AUTHORIZE, args_bcs);
//        if (!CommonService.isResponseSuccess(rsp)) {
//            redirect(response, "/");
//            return false;
//        }
        
        return true;
    }

}
