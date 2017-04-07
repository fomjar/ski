package com.ski.frs.web.filter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import net.sf.json.JSONArray;
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
        
        boolean forward = true;
        switch (inst) {
        case ISIS.INST_QUERY_PIC_BY_FV_I:
            forward = processQueryPicByFVI(args);
            break;
        case ISIS.INST_APPLY_SUB_LIB_CHECK:
            forward = processApplySubLibCheck(args);
            break;
        case ISIS.INST_APPLY_SUB_LIB_IMPORT:
            forward = processApplySubLibImport(args);
            break;
        }
        
        if (forward) {   // forward to bcs
            FjDscpMessage req = FjServerToolkit.dscpRequest("bcs", inst, args);
            CacheResponse.getInstance().cacheWait(req.sid(), response);
        } else {    // response
            response.attr().put("Content-Type", "application/json");
            response.attr().put("Content-Encoding", "gzip");
            response.content(args);
        }
        
        return true;
    }
    
    private static boolean init_face_interface = false;
    
    private static boolean processQueryPicByFVI(JSONObject args) {
        if (!args.has("pic")) {
            String desc = "illegal arguments, no pic";
            args.clear();
            args.put("code", FjISIS.CODE_ILLEGAL_ARGS);
            args.put("desc", desc);
            logger.error(desc + ", " + args);
            return false;
        }
        
        String pic = args.getString("pic");
        args.remove("pic");
        if (!pic.startsWith("data:image")) {
            String desc = "illegal arguments, illegal pic";
            args.clear();
            args.put("code", FjISIS.CODE_ILLEGAL_ARGS);
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
            args.put("code", FjISIS.CODE_ILLEGAL_ARGS);
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
                args.put("code", FjISIS.CODE_INTERNAL_ERROR);
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
            args.put("code", FjISIS.CODE_INTERNAL_ERROR);
            args.put("desc", desc);
            logger.error(desc);
            file.delete();
            return false;
        }
        
        file.delete();
        args.put("fv", fv); // 特征向量
        
        return true;
    }
    
    private static boolean processApplySubLibCheck(JSONObject args) {
        if (!args.has("type") || !args.has("path") || !args.has("reg_id")) {
            String desc = "illegal arguments, no type, path, reg_id";
            args.clear();
            args.put("code", FjISIS.CODE_ILLEGAL_ARGS);
            args.put("desc", desc);
            logger.error(desc + ", " + args);
            return false;
        }
        
        switch (args.getInt("type")) {
        case ISIS.FIELD_PIC_TYPE_MAN: processApplySubLibCheckMan(args); break;
        }
        return false;
    }
    
    private static boolean processApplySubLibCheckMan(JSONObject args) {
        String path = args.getString("path");
        String reg_id = args.getString("reg_id");
        String reg_name = args.has("reg_name") ? args.getString("reg_name") : null;
        String reg_addr = args.has("reg_addr") ? args.getString("reg_addr") : null;
        int count = args.has("count") ? args.getInt("count") : 3;
        
        JSONArray desc = new JSONArray();
        List<File> list = new LinkedList<File>();
        collectSomeFile(list, new File(path), count);
        list.forEach(file->{
            JSONObject check = new JSONObject();
            check.put("file", file.getName());
            if (null != reg_id)     check.put("id",     getFileRegexField(file, reg_id));
            if (null != reg_name)   check.put("name",   getFileRegexField(file, reg_name));
            if (null != reg_addr)   check.put("addr",   getFileRegexField(file, reg_addr));
            
            desc.add(check);
        });
        
        args.clear();
        args.put("code", FjISIS.CODE_SUCCESS);
        args.put("desc", desc);
        logger.info("sublib check result: " + args);
        return false;
    }
    
    private static boolean processApplySubLibImport(JSONObject args) {
        return false;
    }
    
    private static List<File> collectFile(File dir) {
        List<File> list = new LinkedList<File>();
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) list.addAll(collectFile(file));
                else if (file.isFile()) list.add(file);
                else logger.error("unknown file: " + file);
            }
        }
        return list;
    }
    
    private static void collectSomeFile(List<File> list, File dir, int count) {
        if (list.size() >= count) return;
        
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) list.addAll(collectFile(file));
                else if (file.isFile()) list.add(file);
                else logger.error("unknown file: " + file);
                
                if (list.size() >= count) break;
            }
        }
    }
    
    private static String getFileRegexField(File file, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(file.getName());
        if (matcher.find()) return matcher.group();
        else return null;
    }
//
//    private static void scanDirectory(File dir) {
//        if (!dir.isDirectory()) return;
//        
//        for (File file : dir.listFiles()) {
//            if (file.isDirectory()) scanDirectory(file);
//            else if (file.isFile()) scanFile(file);
//            else logger.error("unknown file: " + file);
//        }
//        if (0 == dir.listFiles().length) dir.delete();
//    }
//    
//    private static void scanFile(File file) {
//        String name = file.getName();
//        if (!name.toLowerCase().endsWith(".jpg")) return;
//        
//        logger.info("scan file: " + file.getPath());
//        if (name.startsWith("20")) updatePic(file, ISIS.FIELD_PIC_SIZE_LARGE);
//        else if (name.startsWith("F")) updatePic(file, ISIS.FIELD_PIC_SIZE_SMALL);
//        else if (name.startsWith("P")) updatePic(file, ISIS.FIELD_PIC_SIZE_MIDDLE);
//        else logger.error("unknown picture file: " + file);
//        
//        checkWait();
//    }
//    
//    private static void updatePic(File file, int size) {
//        String[] fields = file.getName().split("\\.")[0].split("_");
//        JSONObject args = new JSONObject();
//        args.put("did",  fields[1]);
//        args.put("name", file.getName());
//        String time = fields[0].substring(fields[0].indexOf("20"));
//        args.put("time", time.substring(0, time.length() - 3));
//        args.put("size", size);
//        args.put("type", ISIS.FIELD_PIC_TYPE_MAN);
//        File xml = new File(file.getPath().replace(".jpg", ".xml"));
//        if (xml.isFile()) {
//            try {args.put("fv0", readVector(xml));}
//            catch (IOException e) {
//                logger.error("read vector failed: " + xml, e);
//                return;
//            }
//            if (!moveToDst(xml)) return;
//        }
//        FjServerToolkit.dscpRequest("web", ISIS.INST_UPDATE_PIC, args);
//        moveToDst(file);
//    }
//    
//    private String readFileAsString(File file) throws IOException {
//        return new String(readFile(file), "utf-8");
//    }
//    
//    private String readVector(File file) throws IOException {
//        String xml = readFileAsString(file);
//        String vector = xml.substring(xml.indexOf("<data>") + 6, xml.indexOf("</data>"));
//        if (vector.contains("\r\n")) vector = vector.replace("\r\n", "");
//        if (vector.contains("\r")) vector = vector.replace("\r", "");
//        if (vector.contains("\n")) vector = vector.replace("\n", "");
//        while (vector.contains("  ")) vector = vector.replace("  ", " ");
//        vector = vector.trim();
//        return vector;
//    }
//    
//    private static boolean moveToDst(File src) {
//        File dst = new File("document"
//                + FjServerToolkit.getServerConfig("web.pic.dst")
//                + "/"
//                + src.getName());
//        File par = dst.getParentFile();
//        if (!par.isDirectory()) {
//            if (!par.mkdirs()) {
//                logger.error("create dst dir failed: " + par);
//                return false;
//            }
//        }
//        if (!src.renameTo(dst)) {
//            logger.error("move file to dst failed: " + src);
//            return false;
//        } else {
//            logger.info("move file to dst success: " + src);
//            return true;
//        }
//    }
//    
//    private byte[] readFile(File file) throws IOException {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        int len = 0;
//        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
//        while (-1 < (len = bis.read(buffer))) baos.write(buffer, 0, len);
//        bis.close();
//        return baos.toByteArray();
//    }
//    
//    private static void checkWait() {
//        while (FjServerToolkit.getAnySender().mq().size() >= Integer.parseInt(FjServerToolkit.getServerConfig("web.pic.que"))) {
//            try {Thread.sleep(100L);}
//            catch (InterruptedException e) {logger.warn("check and wait mq failed", e);}
//        }
//    }
}
