package com.ski.frs.web.filter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.ski.frs.isis.ISIS;
import com.ski.frs.web.CacheResponse;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.msg.FjISIS;
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
            args_rsp.put("code", FjISIS.CODE_ILLEGAL_INST);
            args_rsp.put("desc", "非法指令");
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
            return false;
        }
        
        int inst = FilterToolkit.getIntFromArgs(args, "inst");
        args.remove("inst");
        logger.info(String.format("[ INTERFACE ] - %s - 0x%08X", request.url(), inst));
        
        boolean result = true;
        switch (inst) {
        case ISIS.INST_QUERY_PIC_BY_FV_I:
            result = processQueryPicByFVI(args);
            break;
        }
        
        if (result) {   // success
            FjDscpMessage req = FjServerToolkit.dscpRequest("bcs", inst, args);
            CacheResponse.getInstance().cacheWait(req.sid(), response);
        } else {    // fail
            logger.error("pre process fail: " + args);
            response.content(args);
        }
        
        return true;
    }
    
    private static boolean init_face_interface = false;
    
    private static boolean processQueryPicByFVI(JSONObject args) {
        int code = FjISIS.CODE_SUCCESS;
        
        if (!args.has("pic")) {
            String desc = "illegal arguments, no pic";
            args.clear();
            args.put("code", code = FjISIS.CODE_ILLEGAL_ARGS);
            args.put("desc", desc);
            logger.error(desc + ", " + args);
            return false;
        }
        
        String pic = args.getString("pic");
        args.remove("pic");
        if (!pic.startsWith("data:image")) {
            String desc = "illegal arguments, illegal pic";
            args.clear();
            args.put("code", code = FjISIS.CODE_ILLEGAL_ARGS);
            args.put("desc", desc);
            logger.error(desc + ", " + args);
            return false;
        }
        
        pic = pic.substring(pic.indexOf("base64,") + 7);
        byte[] pic_data = Base64.getDecoder().decode(pic);
        File file = new File("pic_" + System.currentTimeMillis());
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(pic_data));
            ImageIO.write(img, "jpg", file);
        } catch (IOException e) {
            String desc = "illegal arguments, illegal pic, " + e.getMessage();
            args.clear();
            args.put("code", code = FjISIS.CODE_ILLEGAL_ARGS);
            args.put("desc", desc);
            logger.error(desc, e);
            file.delete();
            return false;
        }
        
        if (!init_face_interface) {
            int rst = FaceInterface.init(FaceInterface.DEVICE_GPU);
            if (FaceInterface.SUCCESS == rst) init_face_interface = true;
            else {
                String desc = "init face interface failed, code: " + rst;
                args.clear();
                args.put("code", code = FjISIS.CODE_INTERNAL_ERROR);
                args.put("desc", desc);
                logger.error(desc);
                file.delete();
                return false;
            }
        }
        String fv = FaceInterface.fv(file.getPath()).trim();
        int err = Integer.parseInt(fv.substring(0, fv.indexOf(" ")));
        fv = fv.substring(fv.indexOf(" ") + 1);
        if (FaceInterface.SUCCESS != err) {
            String desc = "convert pic to fv failed, code: " + err;
            args.clear();
            args.put("code", code = FjISIS.CODE_INTERNAL_ERROR);
            args.put("desc", desc);
            logger.error(desc);
            file.delete();
            return false;
        }
        
        file.delete();
        args.put("fv", fv); // 特征向量
        
        return code == FjISIS.CODE_SUCCESS;
    }
}
