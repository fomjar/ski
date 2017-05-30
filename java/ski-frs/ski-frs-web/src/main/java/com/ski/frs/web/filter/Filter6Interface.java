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

import org.apache.log4j.Logger;

import com.ski.frs.isis.ISIS;
import com.ski.frs.web.FeatureService;
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
    
    private static void processGetPicFV(FjHttpResponse response, JSONObject args, FjServer server) {
        if (!args.has("data")) {
            String desc = "illegal arguments, no data";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        
        String data = args.getString("data");
        args.remove("data");
        
        FjReference<double[]> fv0 = new FjReference<>(null);
        FeatureService.getDefault().fv_base64(new FeatureService.FV() {
            @Override
            public void fv(double[] fv) {fv0.t = fv;}
        }, data);
        JSONArray desc = JSONArray.fromObject(fv0.t);
        response(response, FjISIS.CODE_SUCCESS, desc);
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
        case ISIS.FIELD_TYPE_MAN:
            processApplySubImportMan(response, args, server);
            break;
        }
    }
    
    private void processApplySubImportMan(FjHttpResponse response, JSONObject args, FjServer server) {
        if (!args.has("key") || !args.has("sid") || !args.has("type") || !args.has("path") || !args.has("reg_idno")) {
            String desc = "illegal arguments, no key, sid, type, path, reg_idno";
            logger.error(desc + ", " + args);
            response(response, FjISIS.CODE_ILLEGAL_ARGS, desc);
            return;
        }
        
        String key_state = args.getString("key");
        String sid = args.getString("sid");
        int type = args.getInt("type");
        String path = args.getString("path");
        String reg_idno = args.getString("reg_idno");
        String reg_name = args.has("reg_name") ? args.getString("reg_name") : null;
        String reg_phone = args.has("reg_phone") ? args.getString("reg_phone") : null;
        String reg_addr = args.has("reg_addr") ? args.getString("reg_addr") : null;
        
        List<File> list_all = WebToolkit.collectFile(new File(path));

        SubImportState state = new SubImportState();
        cache_sub_import_state.put(key_state, state);
        state.file_total = list_all.size();
        state.time_begin = System.currentTimeMillis();
        
        response(response, FjISIS.CODE_SUCCESS, null);
        new Thread(()->{
            FjReference<Long> time_ts = new FjReference<>(0l);
            
            int cache_size = Integer.parseInt(FjServerToolkit.getServerConfig("web.sub.import"));
            FeatureService[] cache_fv = new FeatureService[cache_size];
            for (int i = 0; i < cache_size; i++) cache_fv[i] = new FeatureService();
            
            ExecutorService pool = Executors.newFixedThreadPool(cache_size, new FjThreadFactory("sub-import"));
            
            FjReference<Integer> file_index = new FjReference<>(0);
            for (File file : list_all) {
                final int index = file_index.t;
                pool.submit(()->{
                    try {
                        state.file_current = file.getPath();
                        
                        String siid = "suject-item-" + UUID.randomUUID().toString().replace("-", "");
                        File dst = new File("document" + FjServerToolkit.getServerConfig("web.pic.sub") + "/" + sid + "/" + siid + "/" + file.getName());
                        
                        time_ts.t = System.currentTimeMillis();
                        if (!WebToolkit.moveFile(file, dst)) {
                            logger.error("file move failed: " + file.getPath());
                            state.file_fails.add(file.getPath());
                            return;
                        } 
                        state.time_move += System.currentTimeMillis() - time_ts.t;
                        
                        time_ts.t = System.currentTimeMillis();
                        FjReference<double[]> fv0 = new FjReference<>(null);
                        cache_fv[index % cache_size].fv_path(new FeatureService.FV() {
                            @Override
                            public void fv(double[] fv) {fv0.t = fv;}
                        }, dst.getPath());
                        if (null == fv0.t) {
                            logger.error("file fv failed: " + file.getPath());
                            state.file_fails.add(file.getPath());
                            return;
                        }
                        state.time_fv += System.currentTimeMillis() - time_ts.t;
                        
                        JSONObject args_bcs = new JSONObject();
                        args_bcs.put("sid",     sid);
                        args_bcs.put("siid",    siid);
                        args_bcs.put("p_type",  type);
                        args_bcs.put("p_size",  ISIS.FIELD_PIC_SIZE_SMALL);
                        args_bcs.put("p_name",  dst.getName());
                        args_bcs.put("p_path",  dst.getPath().substring("document".length()).replace("\\", "/")); 
                        args_bcs.put("p_fv",    fv0.t);
                        if (null != reg_idno)   args_bcs.put("s_idno",   WebToolkit.regexField(file.getName(), reg_idno));
                        if (null != reg_name)   args_bcs.put("s_name",   WebToolkit.regexField(file.getName(), reg_name));
                        if (null != reg_phone)  args_bcs.put("s_phone",  WebToolkit.regexField(file.getName(), reg_phone));
                        if (null != reg_addr)   args_bcs.put("s_addr",   WebToolkit.regexField(file.getName(), reg_addr));
                        
                        FjServerToolkit.dscpRequest("bcs", ISIS.INST_APPLY_SUB_IMPORT, args_bcs);
                        state.file_success++;
                        logger.info("importing file success: " + file.getPath());
                        
                        while (FjServerToolkit.getAnySender().mq().size() >= Integer.parseInt(FjServerToolkit.getServerConfig("web.pic.que"))) {
                            try {Thread.sleep(100L);}
                            catch (InterruptedException e) {e.printStackTrace();}
                        }
                    } catch (Exception e) {
                        logger.error("file import failed: " + file.getPath(), e);
                        state.file_fails.add(file.getPath());
                    }
                }); // pool.submit
                file_index.t++;
            }
            pool.shutdownNow();
            for (int i = 0; i < cache_size; i++) cache_fv[i].close();
            
            state.time_end = System.currentTimeMillis();
            
            if (0 < state.file_fails.size()) {
                logger.error("import failes: " + state.file_fails.stream().map(p->"\r\n" + p).reduce((p1, p2)->p1 + p2).get());
            }
        }).start();
    }
    
    private static class SubImportState {
        public int file_total = 0;
        public volatile int file_success = 0;
        public List<String> file_fails = new LinkedList<>();
        public volatile String file_current = null;
        public long time_begin = System.currentTimeMillis();
        public long time_end = 0;
        public volatile long time_move = 0;
        public volatile long time_fv = 0;
        
        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("file_total",      file_total);
            json.put("file_success",    file_success);
            json.put("file_fails",      file_fails);
            json.put("file_current",    file_current);
            json.put("time_begin",      time_begin);
            json.put("time_end",        time_end);
            json.put("time_move",       time_move);
            json.put("time_fv",         time_fv);
            return json;
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
        case ISIS.FIELD_TYPE_MAN:
            processApplySubImportCheckMan(response, args, server);
            break;
        }
    }
    
    private static void processApplySubImportCheckMan(FjHttpResponse response, JSONObject args, FjServer server) {
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
