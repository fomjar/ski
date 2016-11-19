package com.ski.sn.web.filter;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.ski.sn.common.CommonDefinition;
import com.ski.sn.common.CommonService;

import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.web.FjWebFilter;
import net.sf.json.JSONObject;

public class Filter6Interface extends FjWebFilter {
    
    private static final Logger logger = Logger.getLogger(Filter6Interface.class);

    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn) {
        if (!"/ski-web".equals(request.path())) return true;
        
        JSONObject args = request.argsToJson();
        if (!args.has("inst")) {
            logger.error("illegal arguments, no inst: " + args);
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_ILLEGAL_INST);
            args_rsp.put("desc", "指令错误");
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
            return false;
        }
        
        int inst = FilterToolkit.getIntFromArgs(args, "inst");
        logger.info(String.format("[ INTERFACE ] - %s - 0x%08X", request.url(), inst));
        
        args.remove("inst");
        if (request.cookie().containsKey("uid"))    {
            String uid = request.cookie().get("uid");
            if (0 < uid.length()) args.put("uid",     Long.parseLong(uid));
        }
        if (request.cookie().containsKey("token"))  args.put("token",   request.cookie().get("token"));
        
        switch (inst) {
        case CommonDefinition.ISIS.INST_UPDATE_USER:
            compressImage(args, "cover", 80);
            break;
        case CommonDefinition.ISIS.INST_UPDATE_MESSAGE:
        case CommonDefinition.ISIS.INST_UPDATE_MESSAGE_REPLY:
            compressImage(args, "image", 240);
            break;
        case CommonDefinition.ISIS.INST_UPDATE_ACTIVITY:
            compressImage(args, "image", 320);
            break;
        case CommonDefinition.ISIS.INST_UPDATE_ACTIVITY_MODULE_VOTE_ITEM:
            compressImage(args, "arg1", 320);
            break;
        }
        
        FjDscpMessage rsp = CommonService.requests("bcs", inst, args);
        
        response.attr().put("Content-Type", "application/json");
        response.attr().put("Content-Encoding", "gzip");
        response.content(rsp.args());
        return true;
    }
    
    private static void compressImage(JSONObject args, String key, int width) {
        if (!args.has(key)) return;
        
        String img = args.getString(key);
        if (null == img || 0 == img.length() || "null".equals(img)) return;
        
        String[] data = data(img);
        switch (data[1]) {
        case "base64":
            byte[] img0 = Base64.getDecoder().decode(data[2]);
            byte[] img1 = compressImageData(data[0].substring(data[0].indexOf("/") + 1), img0, width);
            String img_new = String.format("data:%s;%s,%s", data[0], data[1], Base64.getEncoder().encodeToString(img1));
            args.put(key, img_new);
            break;
        }
    }
    
    private static byte[] compressImageData(String type, byte[] data, int width) {
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
            if (img.getWidth() <= width) return data;
            
            int height = (int) (((float) width) / img.getWidth() * img.getHeight());
            BufferedImage img1 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = img1.getGraphics();
            g.drawImage(img, 0, 0, width, height, null);
            g.dispose();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img1, type, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            logger.error("compress image failed", e);
        }
        return null;
    }

}
