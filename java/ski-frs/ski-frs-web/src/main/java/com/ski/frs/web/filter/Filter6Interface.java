package com.ski.frs.web.filter;

import java.io.File;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ski.frs.isis.ISIS;
import com.ski.frs.web.WebToolkit;

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
        
        int inst = WebToolkit.getIntFromArgs(args, "inst");
        args.remove("inst");
        logger.info(String.format("[ INTERFACE ] %s - 0x%08X", request.url(), inst));
        
        switch (inst) {
        case ISIS.INST_QUERY_PIC_BY_FV_I:
            processQueryPicByFVI(response, args, server);
            break;
        case ISIS.INST_QUERY_SUB_LIB_IMPORT:
            processQuerySubLibImport(response, args, server);
            break;
        case ISIS.INST_UPDATE_SUB_LIB_DEL:
            processUpdateSubLibDel(response, args, server);
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
    
    private static void processQueryPicByFVI(FjHttpResponse response, JSONObject args, FjServer server) {
        if (!args.has("data")) {
            String desc = "illegal arguments, no data";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        
        String data = args.getString("data");
        args.remove("data");
        if (data.startsWith("data:image")) data = data.substring(data.indexOf("base64,") + 7);
        
        String fv = null;
        if (null == (fv = WebToolkit.fvBase64Image(data))) {
            String desc = "illegal arguments, invalid base64 image data";
            logger.error(desc);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        args.put("fv", fv); // 特征向量
        
        FjDscpMessage req_bcs = FjServerToolkit.dscpRequest("bcs", ISIS.INST_QUERY_PIC_BY_FV_I, args);
        waitSessionForResponse(server, response, req_bcs.sid());
    }
    
    private void processQuerySubLibImport(FjHttpResponse response, JSONObject args, FjServer server) {
        if (!args.has("key")) {
            String desc = "illegal arguments, no key";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        String key = args.getString("key");
        if (!cache_sublib_import_state.containsKey(key)) {
            String desc = "illegal arguments, invalid key: " + key;
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        SubLibImportState state = cache_sublib_import_state.get(key);
        if (state.total == state.success + state.fails.size()) cache_sublib_import_state.remove(key);
        
        JSONObject desc = new JSONObject();
        desc.put("total",   state.total);
        desc.put("success", state.success);
        desc.put("fails",   state.fails);
        
        response(response, FjISIS.CODE_SUCCESS, desc);
    }
    
    private static void processUpdateSubLibDel(FjHttpResponse response, JSONObject args, FjServer server) {
        if (!args.has("slid")) {
            String desc = "illegal arguments, no slid";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        int slid = args.getInt("slid");
        File dir = new File("document" + FjServerToolkit.getServerConfig("web.pic.sub") + "/" + slid);
        if (!WebToolkit.deleteFile(dir)) {
            String desc = "delete dir failed: " + dir.getPath();
            logger.error(desc);
            response(response, FjISIS.CODE_INTERNAL_ERROR, desc);
            return;
        }
        FjDscpMessage req_cdb = FjServerToolkit.dscpRequest("cdb", ISIS.INST_UPDATE_SUB_LIB_DEL, args);
        waitSessionForResponse(server, response, req_cdb.sid());
    }
    
    private static void processApplySubLibCheck(FjHttpResponse response, JSONObject args, FjServer server) {
        if (!args.has("type") || !args.has("path")) {
            String desc = "illegal arguments, no type, path";
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
        if (!args.has("type") || !args.has("path") || !args.has("reg_idno")) {
            String desc = "illegal arguments, no type, path, reg_idno";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        
        String path = args.getString("path");
        String reg_idno = args.getString("reg_idno");
        String reg_name = args.has("reg_name") ? args.getString("reg_name") : null;
        String reg_phone = args.has("reg_phone") ? args.getString("reg_phone") : null;
        String reg_addr = args.has("reg_addr") ? args.getString("reg_addr") : null;
        int count = args.has("count") ? args.getInt("count") : 3;
        
        JSONArray desc = new JSONArray();
        List<File> list = new LinkedList<>();
        WebToolkit.collectSomeFile(list, new File(path), count);
        list.forEach(file->{
            JSONObject check = new JSONObject();
            check.put("file", file.getName());
            check.put("idno", WebToolkit.regexField(file.getName(), reg_idno));
            if (null != reg_name)   check.put("name",   WebToolkit.regexField(file.getName(), reg_name));
            if (null != reg_phone)  check.put("phone",  WebToolkit.regexField(file.getName(), reg_phone));
            if (null != reg_addr)   check.put("addr",   WebToolkit.regexField(file.getName(), reg_addr));
            
            desc.add(check);
        });
        
        logger.info("sublib check result: " + args);
        response(response, FjISIS.CODE_SUCCESS, desc);
    }
    
    private void processApplySubLibImport(FjHttpResponse response, JSONObject args, FjServer server) {
        if (!args.has("key") || !args.has("slid") || !args.has("type") || !args.has("path")) {
            String desc = "illegal arguments, no key, slid, type, path";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        
        switch (args.getInt("type")) {
        case ISIS.FIELD_TYPE_MAN:
            processApplySubLibImportMan(response, args, server);
            break;
        }
    }
    
    private void processApplySubLibImportMan(FjHttpResponse response, JSONObject args, FjServer server) {
        if (!args.has("key") || !args.has("slid") || !args.has("type") || !args.has("path") || !args.has("reg_idno")) {
            String desc = "illegal arguments, no key, slid, type, path, reg_idno";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        
        String key_state = args.getString("key");
        int slid = args.getInt("slid");
        int type = args.getInt("type");
        String path = args.getString("path");
        String reg_idno = args.getString("reg_idno");
        String reg_name = args.has("reg_name") ? args.getString("reg_name") : null;
        String reg_phone = args.has("reg_phone") ? args.getString("reg_phone") : null;
        String reg_addr = args.has("reg_addr") ? args.getString("reg_addr") : null;
        
        List<File> list_all = WebToolkit.collectFile(new File(path));

        SubLibImportState state = new SubLibImportState();
        cache_sublib_import_state.put(key_state, state);
        state.total = list_all.size();
        
        JSONObject desc = new JSONObject();
        desc.put("total",   state.total);
        response(response, FjISIS.CODE_SUCCESS, desc);
        new Thread(()->{
//          Map<Thread, Long> cache_fvi = new HashMap<>();
            list_all.stream().forEach(file->{
                File dst = new File("document" + FjServerToolkit.getServerConfig("web.pic.sub") + "/" + slid + "/" + file.getName());
                if (!WebToolkit.moveFile(file, dst)) {
                    logger.error("importing file failed: " + file.getPath());
                    synchronized(state.fails) {state.fails.add(file.getPath());}
                    return;
                }
                
//                if (!cache_fvi.containsKey(Thread.currentThread())) {
//                    cache_fvi.put(Thread.currentThread(), FaceInterface.initInstance(FaceInterface.DEVICE_GPU));
//                    logger.info(String.format("init face interface instance: 0x%016X", cache_fvi.get(Thread.currentThread())));
//                }
//                String fv = WebToolkit.fvLocalImage(cache_fvi.get(Thread.currentThread()), dst.getPath());
                String fv = WebToolkit.fvLocalImage(dst.getPath());
                
                JSONObject args_bcs = new JSONObject();
                args_bcs.put("pic_type", type);
                args_bcs.put("pic_path", dst.getPath().substring("document/".length())); 
                args_bcs.put("pic_fv",   fv);
                args_bcs.put("slm_slid", slid);
                if (null != reg_idno)   args_bcs.put("slm_idno",   WebToolkit.regexField(file.getName(), reg_idno));
                if (null != reg_name)   args_bcs.put("slm_name",   WebToolkit.regexField(file.getName(), reg_name));
                if (null != reg_phone)  args_bcs.put("slm_phone",  WebToolkit.regexField(file.getName(), reg_phone));
                if (null != reg_addr)   args_bcs.put("slm_addr",   WebToolkit.regexField(file.getName(), reg_addr));
                
                FjServerToolkit.dscpRequest("bcs", ISIS.INST_APPLY_SUB_LIB_IMPORT, args_bcs);
                state.success++;
                logger.info("importing file success: " + file.getPath());
                
                while (FjServerToolkit.getAnySender().mq().size() >= Integer.parseInt(FjServerToolkit.getServerConfig("web.pic.que"))) {
                    try {Thread.sleep(100L);}
                    catch (InterruptedException e) {e.printStackTrace();}
                }
            }); // blocked
//            logger.info("free face interface count: " + cache_fvi.size());
//            cache_fvi.forEach((t, i)->{FaceInterface.freeInstance(i);});
//            cache_fvi.clear();
            
            if (0 < state.fails.size()) {
                logger.error("import failes: " + state.fails.stream().map(p->"\r\n" + p).reduce((p1, p2)->p1 + p2).get());
            }
        }).start();
    }
    
    private static class SubLibImportState {
        public int total = 0;
        public volatile int success = 0;
        public List<String> fails = new LinkedList<>();
    }
}
