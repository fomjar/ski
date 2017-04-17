package com.ski.frs.web.filter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.SocketChannel;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.ski.frs.isis.ISIS;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
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
    
    private Map<String, SubLibImportState> cache_sublib_import_state;
    
    public Filter6Interface() {
        cache_sublib_import_state = new HashMap<>();
    }
    
    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn, FjServer server) {
        if (!"/ski-web".equals(request.path())) return true;
        
        JSONObject args = request.argsToJson();
        if (!args.has("inst")) {
            String desc = "illegal arguments, no inst";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_INST, desc);
            return false;
        }
        
        int inst = FilterToolkit.getIntFromArgs(args, "inst");
        args.remove("inst");
        logger.info(String.format("[ INTERFACE ] - %s - 0x%08X", request.url(), inst));
        
        switch (inst) {
        case ISIS.INST_QUERY_PIC_BY_FV_I:
            processQueryPicByFVI(response, args, server);
            break;
        case ISIS.INST_UPDATE_SUB_LIB_DEL:
            processApplySubLibDel(response, args, server);
            break;
        case ISIS.INST_APPLY_SUB_LIB_CHECK:
            processApplySubLibCheck(response, args, server);
            break;
        case ISIS.INST_APPLY_SUB_LIB_IMPORT:
            processApplySubLibImport(response, args, server);
            break;
        default: {
            FjDscpMessage req_bcs = FjServerToolkit.dscpRequest("bcs", inst, args);
            waitSessionForResponse(server, response, req_bcs.sid());
            break;
        }
        }
        
        return true;
    }
    
    private static void waitSessionForResponse(FjServer server, FjHttpResponse response, String sid) {
        server.onDscpSession(sid, new FjServer.FjServerTask() {
            @Override
            public void onMessage(FjServer server, FjMessageWrapper wrapper) {
                FjDscpMessage dmsg = (FjDscpMessage) wrapper.message();
                response(response, FjServerToolkit.dscpResponseCode(dmsg), FjServerToolkit.dscpResponseDesc(dmsg));
            }
            @Override
            public void initialize(FjServer server) {}
            @Override
            public void destroy(FjServer server) {}
        });
        responseWait(response);
    }
    
    private static void responseWait(FjHttpResponse response) {
        synchronized (response) {
            try {response.wait();}
            catch (InterruptedException e) {e.printStackTrace();}
        }
    }
    
    private static void response(FjHttpResponse response, int code, Object desc) {
        JSONObject args = new JSONObject();
        args.put("code", code);
        args.put("desc", desc);
        response.content(args);
        response.attr().put("Content-Type", "application/json");
        response.attr().put("Content-Encoding", "gzip");
        synchronized (response) {response.notifyAll();}
    }
    
    private static long fi = 0;
    
    private static void processQueryPicByFVI(FjHttpResponse response, JSONObject args, FjServer server) {
        if (!args.has("pic")) {
            String desc = "illegal arguments, no pic";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        
        String pic = args.getString("pic");
        args.remove("pic");
        if (!pic.startsWith("data:image")) {
            String desc = "illegal arguments, illegal pic";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        
        pic = pic.substring(pic.indexOf("base64,") + 7);
        byte[] pic_data = Base64.getDecoder().decode(pic);
        File file = new File("pic_" + System.currentTimeMillis());
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(pic_data));
            ImageIO.write(img, "jpg", file);
        } catch (IOException e) {
            String desc = "illegal arguments, illegal pic, " + e.getMessage();
            logger.error(desc, e);
            file.delete();
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        
        if (0 == fi) fi = FaceInterface.initInstance(FaceInterface.DEVICE_GPU);
        
        String fv = null;
        try {fv = new String(FaceInterface.fv(fi,
                file.getPath().getBytes(FjServerToolkit.getServerConfig("web.pic.enc")))).trim();}
        catch (UnsupportedEncodingException e) {e.printStackTrace();}
        int err = Integer.parseInt(fv.substring(0, fv.indexOf(" ")));
        fv = fv.substring(fv.indexOf(" ") + 1);
        if (FaceInterface.SUCCESS != err) {
            String desc = "convert pic to fv failed, code: " + err;
            logger.error(desc);
            file.delete();
            response(response, FjISIS.CODE_INTERNAL_ERROR, desc);
            return;
        }
        
        file.delete();
        args.put("fv", fv); // 特征向量
        
        FjDscpMessage req_bcs = FjServerToolkit.dscpRequest("bcs", ISIS.INST_QUERY_PIC_BY_FV_I, args);
        waitSessionForResponse(server, response, req_bcs.sid());
    }
    
    private static void processApplySubLibDel(FjHttpResponse response, JSONObject args, FjServer server) {
        if (!args.has("slid")) {
            String desc = "illegal arguments, no slid";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        int slid = args.getInt("slid");
        File dir = new File("document" + FjServerToolkit.getServerConfig("web.pic.sub") + "/" + slid);
        if (!SubLibHelper.deleteDir(dir)) {
            String desc = "delete dir failed: " + dir.getPath();
            logger.error(desc);
            response(response, FjISIS.CODE_INTERNAL_ERROR, desc);
            return;
        }
        FjDscpMessage req_cdb = FjServerToolkit.dscpRequest("cdb", ISIS.INST_UPDATE_SUB_LIB_DEL, args);
        waitSessionForResponse(server, response, req_cdb.sid());
    }
    
    private static void processApplySubLibCheck(FjHttpResponse response, JSONObject args, FjServer server) {
        if (!args.has("type") || !args.has("path") || !args.has("reg_idno")) {
            String desc = "illegal arguments, no type, path, reg_idno";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        
        switch (args.getInt("type")) {
        case ISIS.FIELD_TYPE_MAN:
            processApplySubLibCheckMan(response, args, server);
            break;
        }
    }
    
    private static void processApplySubLibCheckMan(FjHttpResponse response, JSONObject args, FjServer server) {
        String path = args.getString("path");
        String reg_idno = args.getString("reg_idno");
        String reg_name = args.has("reg_name") ? args.getString("reg_name") : null;
        String reg_phone = args.has("reg_phone") ? args.getString("reg_phone") : null;
        String reg_addr = args.has("reg_addr") ? args.getString("reg_addr") : null;
        int count = args.has("count") ? args.getInt("count") : 3;
        
        JSONArray desc = new JSONArray();
        List<File> list = new LinkedList<>();
        SubLibHelper.collectSomeFile(list, new File(path), count);
        list.forEach(file->{
            JSONObject check = new JSONObject();
            check.put("file", file.getName());
            if (null != reg_idno)   check.put("idno",   SubLibHelper.getRegexField(file.getName(), reg_idno));
            if (null != reg_name)   check.put("name",   SubLibHelper.getRegexField(file.getName(), reg_name));
            if (null != reg_phone)  check.put("phone",  SubLibHelper.getRegexField(file.getName(), reg_phone));
            if (null != reg_addr)   check.put("addr",   SubLibHelper.getRegexField(file.getName(), reg_addr));
            
            desc.add(check);
        });
        
        logger.info("sublib check result: " + args);
        response(response, FjISIS.CODE_SUCCESS, desc);
    }
    
    
    private void processApplySubLibImport(FjHttpResponse response, JSONObject args, FjServer server) {
        if (!args.has("slid") || !args.has("type") || !args.has("path") || !args.has("reg_idno")) {
            String desc = "illegal arguments, no slid, type, path, reg_idno";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        int slid = args.getInt("slid");
        int type = args.getInt("type");
        String path = args.getString("path");
        String reg_idno = args.getString("reg_idno");
        String reg_name = args.has("reg_name") ? args.getString("reg_name") : null;
        String reg_phone = args.has("reg_phone") ? args.getString("reg_phone") : null;
        String reg_addr = args.has("reg_addr") ? args.getString("reg_addr") : null;
        
        List<File> list_all = SubLibHelper.collectFile(new File(path));
        Map<Thread, Long> cache_fi = new HashMap<>();
        JSONObject desc = new JSONObject();
        JSONArray desc_fail = new JSONArray();
        list_all.parallelStream().forEach(file->{
            File dst = new File("document" + FjServerToolkit.getServerConfig("web.pic.sub") + "/" + slid + "/" + file.getName());
            if (!SubLibHelper.moveFile(file, dst)) {
                logger.error("importing file failed: " + file.getPath());
                desc_fail.add(file.getPath());
                return;
            }
            
            if (!cache_fi.containsKey(Thread.currentThread())) {
                cache_fi.put(Thread.currentThread(), FaceInterface.initInstance(FaceInterface.DEVICE_GPU));
                logger.info(String.format("init face interface instance: 0x%016X", cache_fi.get(Thread.currentThread())));
            }
            String fv = null;
            try {fv = new String(FaceInterface.fv(cache_fi.get(Thread.currentThread()),
                    dst.getPath().getBytes(FjServerToolkit.getServerConfig("web.pic.enc")))).trim();}
            catch (UnsupportedEncodingException e1) {e1.printStackTrace();}
//            int err = Integer.parseInt(fv.substring(0, fv.indexOf(" ")));
            fv = fv.substring(fv.indexOf(" ") + 1).trim();
            
            JSONObject args_bcs = new JSONObject();
            args_bcs.put("pic_type", type);
            args_bcs.put("pic_path", dst.getPath().substring("document/".length())); 
            args_bcs.put("pic_fv",   fv);
            args_bcs.put("slm_slid", slid);
            if (null != reg_idno)   args_bcs.put("slm_idno",   SubLibHelper.getRegexField(file.getName(), reg_idno));
            if (null != reg_name)   args_bcs.put("slm_name",   SubLibHelper.getRegexField(file.getName(), reg_name));
            if (null != reg_phone)  args_bcs.put("slm_phone",  SubLibHelper.getRegexField(file.getName(), reg_phone));
            if (null != reg_addr)   args_bcs.put("slm_addr",   SubLibHelper.getRegexField(file.getName(), reg_addr));
            
            FjServerToolkit.dscpRequest("bcs", ISIS.INST_APPLY_SUB_LIB_IMPORT, args_bcs);
            logger.info("importing file success: " + file.getPath());
            
            while (FjServerToolkit.getAnySender().mq().size() >= Integer.parseInt(FjServerToolkit.getServerConfig("web.pic.que"))) {
                try {Thread.sleep(100L);}
                catch (InterruptedException e) {e.printStackTrace();}
            }
        }); // blocked
        logger.info("free face interface count: " + cache_fi.size());
        cache_fi.forEach((t, i)->{FaceInterface.freeInstance(i);});
        cache_fi.clear();
        
        desc.put("fail", desc_fail);
        response(response, FjISIS.CODE_SUCCESS, desc);
    }
    
    private static class SubLibHelper {
        
        private static List<File> collectFile(File dir) {
            List<File> list = new LinkedList<>();
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
        
        private static String getRegexField(String string, String regex) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(string);
            if (matcher.find()) return matcher.group(1);
            else return null;
        }
        
        private static Object lock_move = new Object();
        private static boolean moveFile(File src, File dst) {
            if (!src.isFile()) {
                logger.error("move file failed, no such file, from: " + src.getPath() + ", to: " + dst.getPath());
                return false;
            }
            File dir = dst.getParentFile();
            if (!dir.isDirectory()) {
                synchronized(lock_move) {
                    if (!dir.isDirectory()) {
                        if (!dir.mkdirs()) {
                            logger.error("move file failed, create dir failed, from: " + src.getPath() + ", to: " + dst.getPath());
                            return false;
                        }
                    }
                }
            }
            if (!src.renameTo(dst)) {
                logger.error("move file failed, from: " + src.getPath() + ", to: " + dst.getPath());
                return false;
            }
            return true;
        }
        
        private static boolean deleteDir(File dir) {
            if (!dir.exists()) return true;
            if (!dir.isDirectory()) return false;
            
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    if (!file.delete()) return false;
                } else if (file.isDirectory()) {
                    if (!deleteDir(file)) return false;
                }
            }
            return dir.delete();
        }
    }
    
    private static class SubLibImportState {
        public int total = 0;
        public int success = 0;
        public List<File> fails = new LinkedList<>();
    }
}
