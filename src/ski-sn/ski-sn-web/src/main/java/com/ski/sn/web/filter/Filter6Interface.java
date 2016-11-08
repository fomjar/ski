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
        
        logger.info(String.format("[ INTERFACE ] - %s", request.url()));
        
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
        args.remove("inst");
        
        if (request.cookie().containsKey("uid"))    args.put("uid",     Integer.parseInt(request.cookie().get("uid")));
        if (request.cookie().containsKey("token"))  args.put("token",   request.cookie().get("token"));
        
        switch (inst) {
        case CommonDefinition.ISIS.INST_UPDATE_USER:
            preprocessUpdateUser(args);
            break;
        case CommonDefinition.ISIS.INST_UPDATE_MESSAGE:
            preprocesssUpdateMessage(args);
            break;
        }
        
        FjDscpMessage rsp = CommonService.requests("bcs", inst, args);
        
        response.attr().put("Content-Type", "application/json");
        response.attr().put("Content-Encoding", "gzip");
        response.content(rsp.args());
        return true;
    }
    
    private void preprocessUpdateUser(JSONObject args) {
        if (!args.has("cover")) return;
        
        String cover = args.getString("cover");
        
        if (null == cover || 0 == cover.length() || "null".equals(cover)) return;
        
        String[] data = data(cover);
        switch (data[1]) {
        case "base64":
            byte[] img0 = Base64.getDecoder().decode(data[2]);
            byte[] img1 = compressImage(data[0].substring(data[0].indexOf("/") + 1), img0, 200);
            String cover_new = String.format("data:%s;%s,%s", data[0], data[1], Base64.getEncoder().encodeToString(img1));
            args.put("cover", cover_new);
            break;
        }
    }
    
    private void preprocesssUpdateMessage(JSONObject args) {
        if (!args.has("image")) return;
        
        String image = args.getString("image");
        
        if (null == image || 0 == image.length() || "null".equals(image)) return;
        
        String[] data = data(image);
        switch (data[1]) {
        case "base64": {
            byte[] img0 = Base64.getDecoder().decode(data[2]);
            byte[] img1 = compressImage(data[0].substring(data[0].indexOf("/") + 1), img0, 600);
            String image_new = String.format("data:%s;%s,%s", data[0], data[1], Base64.getEncoder().encodeToString(img1));
            args.put("image", image_new);
            break;
        }
        }
    }
    
    private static byte[] compressImage(String type, byte[] data, int width) {
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
