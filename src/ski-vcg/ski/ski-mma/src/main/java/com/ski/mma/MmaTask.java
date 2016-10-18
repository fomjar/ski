package com.ski.mma;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjJsonMessage;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class MmaTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(MmaTask.class);
    
    public  static final String URL_KEY = "/ski-mma";
    
    @Override
    public void initialize(FjServer server) {}

    @Override
    public void destroy(FjServer server) {}

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper){
        FjMessage msg = wrapper.message();
        if (!(msg instanceof FjDscpMessage)) {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
            return;
        }
        
        FjDscpMessage   dmsg = (FjDscpMessage) msg;
        
        switch (dmsg.inst()) {
        case CommonDefinition.ISIS.INST_ECOM_APPLY_AUTHORIZE: {
            logger.info(String.format("INST_ECOM_APPLY_AUTHORIZE            - %s:%s", dmsg.fs(), dmsg.sid()));
            processApplyAuthorize(dmsg);
            break;
        }
        case CommonDefinition.ISIS.INST_ECOM_APPLY_RENT_BEGIN: {
            logger.info(String.format("INST_ECOM_APPLY_RENT_BEGIN           - %s:%s", dmsg.fs(), dmsg.sid()));
            processApplyRentBegin(dmsg);
            break;
        }
        case CommonDefinition.ISIS.INST_ECOM_APPLY_ENCODE: {
            logger.info(String.format("INST_ECOM_APPLY_ENCODE               - %s:%s", dmsg.fs(), dmsg.sid()));
            processApplyEncode(dmsg);
            break;
        }
        case CommonDefinition.ISIS.INST_ECOM_UPDATE_CHATROOM_MESSAGE: {
            if (dmsg.fs().startsWith("cdb")) break;
            
            logger.info(String.format("INST_ECOM_UPDATE_CHATROOM_MESSAGE    - %s:%s", dmsg.fs(), dmsg.sid()));
            processUpdateChatroomMessage(dmsg);
            break;
        }
        default:
            logger.error(String.format("unsupported instruction: 0x%08X", dmsg.inst()));
            response(dmsg, CommonDefinition.CODE.CODE_SYS_ILLEGAL_INST, "非法指令");
            break;
        }
    }
    
    private static void processApplyAuthorize(FjDscpMessage dmsg) {
        JSONObject args     = dmsg.argsToJsonObject();
        String[] user       = getArrayFromArgs(args, "user");
        String[] content    = getArrayFromArgs(args, "content");
        
        FjJsonMessage rsp_weimi = WeimiInterface.sendSms2(
                FjServerToolkit.getServerConfig("mma.weimi.uid"),
                FjServerToolkit.getServerConfig("mma.weimi.pas"),
                user,
                FjServerToolkit.getServerConfig("mma.weimi.cid.authorize"),
                content);
        int code = rsp_weimi.json().getInt("code");
        
        if (CommonDefinition.CODE.CODE_SYS_SUCCESS != code) {
            logger.error("send sms failed: " + rsp_weimi);
            response(dmsg, CommonDefinition.CODE.CODE_SYS_UNAVAILABLE, "系统不可用，请稍候再试");
            return;
        }
        
        response(dmsg, CommonDefinition.CODE.CODE_SYS_SUCCESS, null);
    }
    
    private static void processApplyRentBegin(FjDscpMessage dmsg) {
        JSONObject args     = dmsg.argsToJsonObject();
        String[] user       = getArrayFromArgs(args, "user");
        String[] content    = getArrayFromArgs(args, "content");
        
        FjJsonMessage rsp_weimi = WeimiInterface.sendSms2(
                FjServerToolkit.getServerConfig("mma.weimi.uid"),
                FjServerToolkit.getServerConfig("mma.weimi.pas"),
                user,
                FjServerToolkit.getServerConfig("mma.weimi.cid.rentbegin"),
                content);
        int code = rsp_weimi.json().getInt("code");
        
        if (CommonDefinition.CODE.CODE_SYS_SUCCESS != code) {
            logger.error("send sms failed: " + rsp_weimi);
            response(dmsg, CommonDefinition.CODE.CODE_SYS_UNAVAILABLE, "系统不可用，请稍候再试");
            return;
        }
        
        response(dmsg, CommonDefinition.CODE.CODE_SYS_SUCCESS, null);
    }
    
    private static void processApplyEncode(FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        String type = args.getString("type");
        byte[] data = Base64.getDecoder().decode(args.getString("data"));
        int    code = CommonDefinition.CODE.CODE_SYS_UNAVAILABLE;
        String desc = "system unavailable";
        switch (type) {
        case "image": {
            try {
                ByteArrayInputStream  src = new ByteArrayInputStream(data);
                ByteArrayOutputStream dst = new ByteArrayOutputStream();
                BufferedImage image = ImageIO.read(src);
                ImageIO.write(image, "png", dst);
                
                code = CommonDefinition.CODE.CODE_SYS_SUCCESS;
                desc = Base64.getEncoder().encodeToString(dst.toByteArray());
            } catch (IOException e) {
                logger.error("encode image failed", e);
                code = CommonDefinition.CODE.CODE_SYS_UNAVAILABLE;
                desc = e.getMessage();
            }
            break;
        }
        case "audio": {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                new FFMPEG().source(data).target(baos).format("wav").execute();
                
                code = CommonDefinition.CODE.CODE_SYS_SUCCESS;
                desc = Base64.getEncoder().encodeToString(baos.toByteArray());
            } catch (IOException | IllegalArgumentException | InterruptedException e) {
                logger.error("encode audio failed", e);
                code = CommonDefinition.CODE.CODE_SYS_UNAVAILABLE;
                desc = e.getMessage();
            }
            break;
        }
        case "vedio": {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                new FFMPEG().source(data).target(baos).format("mp4").execute();
                
                code = CommonDefinition.CODE.CODE_SYS_SUCCESS;
                desc = Base64.getEncoder().encodeToString(baos.toByteArray());
            } catch (IOException | IllegalArgumentException | InterruptedException e) {
                logger.error("encode audio failed", e);
                code = CommonDefinition.CODE.CODE_SYS_UNAVAILABLE;
                desc = e.getMessage();
            }
            break;
        }
        }
        
        response(dmsg, code, desc);
    }
    
    private static Map<Integer, Long> cache_last_message_time = new ConcurrentHashMap<Integer, Long>();
    
    private static void processUpdateChatroomMessage(FjDscpMessage dmsg) {
        JSONObject message = dmsg.argsToJsonObject();
        int crid = message.getInt("crid");
        
        if (!cache_last_message_time.containsKey(crid)) cache_last_message_time.put(crid, System.currentTimeMillis());
        else {
            long timer = Long.parseLong(FjServerToolkit.getServerConfig("mma.chatroom.message.timer"));
            long last = cache_last_message_time.get(crid);
            long now  = System.currentTimeMillis();
            if (now - last > timer * 1000) {
                JSONObject timer_message = new JSONObject();
                timer_message.put("crid",   crid);
                timer_message.put("member", CommonService.CHATROOM_MEMBER_SYSTEM);
                timer_message.put("type",   CommonService.CHATROOM_MESSAGE_TEXT);
                timer_message.put("message", new String(Base64.getEncoder().encode(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).getBytes())));
                recordChatroomMessage(timer_message);
            }
        }
        cache_last_message_time.put(crid, System.currentTimeMillis());
        
        recordChatroomMessage(message);
    }
    
    private static void recordChatroomMessage(JSONObject message) {
        FjDscpMessage msg = new FjDscpMessage();
        msg.json().put("fs",   FjServerToolkit.getAnyServer().name());
        msg.json().put("ts",   "cdb");
        msg.json().put("inst", CommonDefinition.ISIS.INST_ECOM_UPDATE_CHATROOM_MESSAGE);
        msg.json().put("args", message);
        FjServerToolkit.getAnySender().send(msg);
    }
    
    private static String[] getArrayFromArgs(JSONObject args, String name) {
        Object      object  = args.get(name);
        String[]    array   = null;
        if (object instanceof String)           array = new String[] {object.toString()};
        else if (object instanceof JSONArray)   array = Arrays.asList(((JSONArray) object).toArray()).toArray(new String[((JSONArray) object).size()]);
        else return null;
        return array;
    }
    
    private static void response(FjDscpMessage req, int code, String desc) {
        JSONObject args = new JSONObject();
        args.put("code", code);
        args.put("desc", desc);
        FjDscpMessage rsp = new FjDscpMessage();
        rsp.json().put("fs",   FjServerToolkit.getAnyServer().name());
        rsp.json().put("ts",   req.fs());
        rsp.json().put("sid",  req.sid());
        rsp.json().put("inst", req.inst());
        rsp.json().put("args", args);
        FjServerToolkit.getAnySender().send(rsp);
    }
}
