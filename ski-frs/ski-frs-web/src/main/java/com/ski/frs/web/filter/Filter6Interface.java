package com.ski.frs.web.filter;

import java.io.File;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.ski.frs.isis.ISIS;
import com.ski.frs.web.FeatureService;
import com.ski.frs.web.FeatureService.FeatureServicePool;
import com.ski.frs.web.WebToolkit;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.msg.FjISIS;
import fomjar.server.web.FjWebFilter;
import fomjar.util.FjReference;
import fomjar.util.FjThreadFactory;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Filter6Interface extends FjWebFilter {
    
    private static final Logger logger = Logger.getLogger(Filter6Interface.class);
    
    private Map<String, SubImportState> cache_sub_import_state;
    
    public Filter6Interface() {
        cache_sub_import_state = new HashMap<>();
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
        case ISIS.INST_SET_PIC:
            processSetPic(response, args, server);
            break;
        case ISIS.INST_GET_PIC_FV:
            processGetPicFV(response, args, server);
            break;
        case ISIS.INST_APPLY_SUB_IMPORT:
            processApplySubImport(response, args, server);
            break;
        case ISIS.INST_APPLY_SUB_IMPORT_CHECK:
            processApplySubImportCheck(response, args, server);
            break;
        case ISIS.INST_APPLY_SUB_IMPORT_STATE:
            processApplySubImportState(response, args, server);
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
    
    private static void processSetPic(FjHttpResponse response, JSONObject args, FjServer server) {
        JSONObject json = WebToolkit.processSetPic(args);
        if (FjISIS.CODE_SUCCESS != json.getInt("code")) {
            response(response, json.getInt("code"), json.get("desc"));
            return;
        }
        
        FjDscpMessage req_bcs = FjServerToolkit.dscpRequest("bcs", ISIS.INST_SET_PIC, json.getJSONObject("desc"));
        waitSessionForResponse(server, response, req_bcs.sid());
    }
    
    private static void processGetPicFV(FjHttpResponse response, JSONObject args, FjServer server) {
        if (!args.has("data")) {
            String desc = "illegal arguments, no data";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        
        String data = args.getString("data");
        args.remove("data");
        
        JSONObject desc = new JSONObject();
        FeatureService.pool0().next().fv_base64(new FeatureService.FV() {
            @Override
            public void fv(int mark, double[] fv, int glass, int mask, int hat, int gender, int nation) {
                desc.put("mark",    mark);
                desc.put("fv",      fv);
                desc.put("glass",   glass);
                desc.put("mask",    mask);
                desc.put("hat",     hat);
                desc.put("gender",  gender);
                desc.put("nation",  nation);
            }
        }, data);
        if (FeatureService.SUCCESS == desc.getInt("mark")) response(response, FjISIS.CODE_SUCCESS, desc);
        else {
            logger.error("illegal picture, desc=" + desc);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
        }
        logger.debug("get pic fv: " + desc);
    }
    
    private void processApplySubImport(FjHttpResponse response, JSONObject args, FjServer server) {
        if (!args.has("key") || !args.has("sid") || !args.has("type") || !args.has("path")) {
            String desc = "illegal arguments, no key, sid, type, path";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        
        switch (args.getInt("type")) {
        case ISIS.FIELD_TYPE_MAN_FACE:
            processApplySubImportMan(response, args, server);
            break;
        }
    }
    
    private void processApplySubImportMan(FjHttpResponse response, JSONObject args, FjServer server) {
        if (!args.has("key") || !args.has("sid") || !args.has("type") || !args.has("path")) {
            String desc = "illegal arguments, no key, sid, type, path";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        
        List<File> list_all = WebToolkit.collectFile(new File(args.getString("path")));

        SubImportState state = new SubImportState();
        state.key = args.getString("key");
        state.sid = args.getString("sid");
        state.type = args.getInt("type");
        state.path = args.getString("path");
        state.reg_idno = args.has("reg_idno") ? args.getString("reg_idno") : null;
        state.reg_name = args.has("reg_name") ? args.getString("reg_name") : null;
        state.reg_phone = args.has("reg_phone") ? args.getString("reg_phone") : null;
        state.reg_addr = args.has("reg_addr") ? args.getString("reg_addr") : null;
        
        state.file_total = list_all.size();
        cache_sub_import_state.put(state.key, state);
        
        response(response, FjISIS.CODE_SUCCESS, null);

        new Thread(()->{
            try {
                FeatureServicePool feat_pool = FeatureService.pool0();
                ExecutorService pool = Executors.newFixedThreadPool(feat_pool.size(), new FjThreadFactory("sub-import"));
                list_all.forEach(file->pool.submit(new SubImportTask(state, file, feat_pool.next())));
                pool.shutdown();
                try {pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);}
                catch (InterruptedException e) {logger.error("wait for sub import done failed", e);}
                state.time_end = System.currentTimeMillis();
            } catch (Exception e) {logger.error("submit sub import task failed", e);}
        }).start();
    }
    
    private static class SubImportState {
        public String key;
        public String sid;
        public int type;
        public String path;
        public String reg_idno;
        public String reg_name;
        public String reg_phone;
        public String reg_addr;
        public int file_total = 0;
        public volatile int file_success = 0;
        public List<String> file_fails = new LinkedList<>();
        public volatile String file_current = null;
        public long time_begin = System.currentTimeMillis();
        public long time_end = 0;
        
        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("key",             key);
            json.put("sid",             sid);
            json.put("type",            type);
            json.put("path",            path);
            json.put("reg_idno",        reg_idno);
            json.put("reg_name",        reg_name);
            json.put("reg_phone",       reg_phone);
            json.put("reg_addr",        reg_addr);
            json.put("file_total",      file_total);
            json.put("file_success",    file_success);
            json.put("file_fails",      file_fails);
            json.put("file_current",    file_current);
            json.put("time_begin",      time_begin);
            json.put("time_end",        time_end);
            return json;
        }
    }
    
    private static class SubImportTask implements Runnable {
        
        private SubImportState  state;
        private File            file;
        private FeatureService  service;
        
        public SubImportTask(SubImportState state, File file, FeatureService service) {
            this.state = state;
            this.file = file;
            this.service = service;
        }
        @Override
        public void run() {
            try {
                state.file_current = file.getPath();
                if (!file.isFile()) {
                    logger.error("illegal file, file not found: " + file.getPath());
                    state.file_fails.add(file.getPath());
                    return;
                }
                
                String siid = "suject-item-" + UUID.randomUUID().toString().replace("-", "");
                File dst = new File("document" + FjServerToolkit.getServerConfig("web.pic.sub") + "/" + state.sid + "/" + siid + "/" + file.getName());
                
                if (!WebToolkit.moveFile(file, dst)) {
                    logger.error("file move failed: " + file.getPath());
                    state.file_fails.add(file.getPath());
                    return;
                }
                
                FjReference<Integer> rmark      = new FjReference<>(-1);
                FjReference<double[]> rfv       = new FjReference<>(null);
                FjReference<Integer> rglass     = new FjReference<>(ISIS.FIELD_GLASS_UNKNOWN);
                FjReference<Integer> rmask      = new FjReference<>(ISIS.FIELD_MASK_UNKNOWN);
                FjReference<Integer> rhat       = new FjReference<>(ISIS.FIELD_HAT_UNKNOWN);
                FjReference<Integer> rgender    = new FjReference<>(ISIS.FIELD_GENDER_UNKNOWN);
                FjReference<Integer> rnation    = new FjReference<>(ISIS.FIELD_NATION_UNKNOWN);
                service.fv_path(new FeatureService.FV() {
                    @Override
                    public void fv(int mark, double[] fv, int glass, int mask, int hat, int gender, int nation) {
                        rmark.t = mark;
                        rfv.t = fv;
                        rglass.t = glass;
                        rmask.t = mask;
                        rhat.t = hat;
                        rgender.t = gender;
                        rnation.t = nation;
                    }
                }, dst.getPath());
                if (FeatureService.SUCCESS != rmark.t) {
                    logger.error("file fv failed: " + dst.getPath());
                    state.file_fails.add(file.getPath());
                    return;
                }
                
                JSONObject args_bcs = new JSONObject();
                args_bcs.put("sid",     state.sid);
                args_bcs.put("siid",    siid);
                args_bcs.put("p_type",  state.type);
                args_bcs.put("p_size",  ISIS.FIELD_PIC_SIZE_SMALL);
                args_bcs.put("p_name",  dst.getName());
                args_bcs.put("p_path",  dst.getPath().substring("document".length()).replace("\\", "/")); 
                args_bcs.put("p_fv",    rfv.t);
                args_bcs.put("p_glass", rglass.t);
                args_bcs.put("p_mask",  rmask.t);
                args_bcs.put("p_hat",   rhat.t);
                args_bcs.put("p_gender",rgender.t);
                args_bcs.put("p_nation",rnation.t);
                if (null != state.reg_idno)   args_bcs.put("s_idno",   WebToolkit.regexField(file.getName(), state.reg_idno));
                if (null != state.reg_name)   args_bcs.put("s_name",   WebToolkit.regexField(file.getName(), state.reg_name));
                if (null != state.reg_phone)  args_bcs.put("s_phone",  WebToolkit.regexField(file.getName(), state.reg_phone));
                if (null != state.reg_addr)   args_bcs.put("s_addr",   WebToolkit.regexField(file.getName(), state.reg_addr));
                
                FjServerToolkit.dscpRequest("bcs", ISIS.INST_APPLY_SUB_IMPORT, args_bcs);
                state.file_success++;
                logger.info("importing file success: " + file.getPath());
            } catch (Exception e) {
                logger.error("file import failed: " + file.getPath(), e);
                state.file_fails.add(file.getPath());
            }
        }
    }
    
    private static void processApplySubImportCheck(FjHttpResponse response, JSONObject args, FjServer server) {
        if (!args.has("type") || !args.has("path")) {
            String desc = "illegal arguments, no type, path";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        
        switch (args.getInt("type")) {
        case ISIS.FIELD_TYPE_MAN_FACE:
            processApplySubImportCheckMan(response, args, server);
            break;
        }
    }
    
    private static void processApplySubImportCheckMan(FjHttpResponse response, JSONObject args, FjServer server) {
        if (!args.has("type") || !args.has("path")) {
            String desc = "illegal arguments, no type, path";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        
        String path = args.getString("path");
        String reg_idno = args.has("reg_idno") ? args.getString("reg_idno") : null;
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
            if (null != reg_idno)   check.put("idno",   WebToolkit.regexField(file.getName(), reg_idno));
            if (null != reg_name)   check.put("name",   WebToolkit.regexField(file.getName(), reg_name));
            if (null != reg_phone)  check.put("phone",  WebToolkit.regexField(file.getName(), reg_phone));
            if (null != reg_addr)   check.put("addr",   WebToolkit.regexField(file.getName(), reg_addr));
            
            desc.add(check);
        });
        
        logger.info("sub check result: " + args);
        response(response, FjISIS.CODE_SUCCESS, desc);
    }

    private void processApplySubImportState(FjHttpResponse response, JSONObject args, FjServer server) {
        if (!args.has("key")) {
            String desc = "illegal arguments, no key";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        String key = args.getString("key");
        if (!cache_sub_import_state.containsKey(key)) {
            String desc = "illegal arguments, invalid key: " + key;
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        SubImportState state = cache_sub_import_state.get(key);
        if (0 < state.time_end) cache_sub_import_state.remove(key);
        
        response(response, FjISIS.CODE_SUCCESS, state.toJson());
    }
}
