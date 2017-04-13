package com.ski.frs.bcs;

import org.apache.log4j.Logger;

import com.ski.frs.isis.ISIS;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjISIS;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class BcsTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(BcsTask.class);
    
    @Override
    public void initialize(FjServer server) {}

    @Override
    public void destroy(FjServer server) {}

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (!(msg instanceof FjDscpMessage)) {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
            return;
        }
        
        FjDscpMessage dmsg = (FjDscpMessage) wrapper.message();
        
        logger.info(String.format("0x%08X - %s", dmsg.inst(), dmsg.sid()));
        switch (dmsg.inst()) {
        case ISIS.INST_UPDATE_PIC:
            processUpdatePic(server, dmsg);
            break;
        case ISIS.INST_UPDATE_SUB_LIB:
            processUpdateSubLib(server, dmsg);
            break;
        case ISIS.INST_QUERY_PIC:
            processQueryPic(server, dmsg);
            break;
        case ISIS.INST_QUERY_PIC_BY_FV_I:
            processQueryPicByFVI(server, dmsg);
            break;
        case ISIS.INST_QUERY_PIC_BY_FV:
            processQueryPicByFV(server, dmsg);
            break;
        case ISIS.INST_QUERY_SUB_LIB:
            processQuerySubLib(server, dmsg);
            break;
        case ISIS.INST_APPLY_SUB_LIB_IMPORT:
            processApplySubLibImport(server, dmsg);
            break;
        default: {
            String err = String.format("illegal inst: 0x%08X", dmsg.inst());
            logger.error(err);
            FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_INST, err);
            break;
        }
        }
    }
    
    private static void processUpdatePic(FjServer server, FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        
        FjDscpMessage req_cdb = FjServerToolkit.dscpRequest("cdb", dmsg.sid(), ISIS.INST_UPDATE_PIC, args);
        server.onDscpSession(req_cdb.sid(), new FjServer.FjServerTask() {
            @Override
            public void onMessage(FjServer server, FjMessageWrapper wrapper) {
                FjDscpMessage dmsg1 = (FjDscpMessage) wrapper.message();
                FjServerToolkit.dscpResponse(dmsg, FjServerToolkit.dscpResponseCode(dmsg1), FjServerToolkit.dscpResponseDesc(dmsg1));
            }
            @Override
            public void initialize(FjServer server) {}
            @Override
            public void destroy(FjServer server) {}
        });
    }
    
    private static void processUpdateSubLib(FjServer server, FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        if (!args.has("name") || !args.has("type")) {
            String err = "illegal arguments, no name, type";
            logger.error(err + ", " + args);
            FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_ARGS, err);
            return;
        }
        
        FjDscpMessage req_cdb = FjServerToolkit.dscpRequest("cdb", dmsg.sid(), ISIS.INST_UPDATE_SUB_LIB, args);
        server.onDscpSession(req_cdb.sid(), new FjServer.FjServerTask() {
            @Override
            public void onMessage(FjServer server, FjMessageWrapper wrapper) {
                FjDscpMessage dmsg1 = (FjDscpMessage) wrapper.message();
                FjServerToolkit.dscpResponse(dmsg, FjServerToolkit.dscpResponseCode(dmsg1), FjServerToolkit.dscpResponseDesc(dmsg1));
            }
            @Override
            public void initialize(FjServer server) {}
            @Override
            public void destroy(FjServer server) {}
        });
    }
    
    private static void processQueryPic(FjServer server, FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        if (!args.has("con") || !args.has("pf") || !args.has("pt")) {
            String err = "illegal arguments, no con, pf, pt";
            logger.error(err + ", " + args);
            FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_ARGS, err);
            return;
        }
        
        FjDscpMessage req_cdb = FjServerToolkit.dscpRequest("cdb", dmsg.sid(), dmsg.inst(), args);
        server.onDscpSession(req_cdb.sid(), new FjServer.FjServerTask() {
            @Override
            public void onMessage(FjServer server, FjMessageWrapper wrapper) {
                FjDscpMessage dmsg1 = (FjDscpMessage) wrapper.message();
                FjServerToolkit.dscpResponse(dmsg, FjServerToolkit.dscpResponseCode(dmsg1), FjServerToolkit.dscpResponseDesc(dmsg1));
            }
            @Override
            public void initialize(FjServer server) {}
            @Override
            public void destroy(FjServer server) {}
        });
    }
    
    private static void processQueryPicByFVI(FjServer server, FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        if (!args.has("fv")) {
            String err = "illegal arguments, no fv";
            logger.error(err + ", " + args);
            FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_ARGS, err);
            return;
        }
        args.put("vd", Integer.parseInt(FjServerToolkit.getServerConfig("bcs.face.vd"))); // 向量维数
        
        FjDscpMessage req_cdb = FjServerToolkit.dscpRequest("cdb", dmsg.sid(), dmsg.inst(), args);
        server.onDscpSession(req_cdb.sid(), new FjServer.FjServerTask() {
            @Override
            public void onMessage(FjServer server, FjMessageWrapper wrapper) {
                FjDscpMessage dmsg1 = (FjDscpMessage) wrapper.message();
                FjServerToolkit.dscpResponse(dmsg, FjServerToolkit.dscpResponseCode(dmsg1), FjServerToolkit.dscpResponseDesc(dmsg1));
            }
            @Override
            public void initialize(FjServer server) {}
            @Override
            public void destroy(FjServer server) {}
        });
    }
    private static void processQueryPicByFV(FjServer server, FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        if (!args.has("pf") || !args.has("pt")) {
            String err = "illegal arguments, no pf, pt";
            logger.error(err + ", " + args);
            FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_ARGS, err);
            return;
        }
        
        if (!args.has("tv")) {
            args.put("tv", Float.parseFloat(FjServerToolkit.getServerConfig("bcs.face.tv"))); // 内积阈值
        }
        
        FjDscpMessage req_cdb = FjServerToolkit.dscpRequest("cdb", dmsg.sid(), dmsg.inst(), args);
        server.onDscpSession(req_cdb.sid(), new FjServer.FjServerTask() {
            @Override
            public void onMessage(FjServer server, FjMessageWrapper wrapper) {
                FjDscpMessage dmsg1 = (FjDscpMessage) wrapper.message();
                JSONArray desc = (JSONArray) FjServerToolkit.dscpResponseDesc(dmsg1);
                JSONArray args = new JSONArray();
                for (int i = 0; i < desc.size(); i++) args.add(json_pic(desc.getJSONArray(i)));
                FjServerToolkit.dscpResponse(dmsg, FjServerToolkit.dscpResponseCode(dmsg1), args);
            }
            @Override
            public void initialize(FjServer server) {}
            @Override
            public void destroy(FjServer server) {}
        });
    }
    
    private static void processQuerySubLib(FjServer server, FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        
        FjDscpMessage req_cdb = FjServerToolkit.dscpRequest("cdb", dmsg.sid(), dmsg.inst(), args);
        server.onDscpSession(req_cdb.sid(), new FjServer.FjServerTask() {
            @Override
            public void onMessage(FjServer server, FjMessageWrapper wrapper) {
                FjDscpMessage dmsg1 = (FjDscpMessage) wrapper.message();
                JSONArray desc = (JSONArray) FjServerToolkit.dscpResponseDesc(dmsg1);
                JSONArray args = new JSONArray();
                for (int i = 0; i < desc.size(); i++) args.add(json_sub_lib(desc.getJSONArray(i)));
                FjServerToolkit.dscpResponse(dmsg, FjServerToolkit.dscpResponseCode(dmsg1), args);
            }
            @Override
            public void initialize(FjServer server) {}
            @Override
            public void destroy(FjServer server) {}
        });
    }
    
    private static void processApplySubLibImport(FjServer server, FjDscpMessage dmsg) {
    }
    
    private static JSONObject json_pic(JSONArray array) {
        JSONObject json = new JSONObject();
        int i = 0;
        json.put("pid",     array.getInt(i++));
        json.put("did",     array.getString(i++));
        json.put("name",    array.getString(i++));
        json.put("time",    array.getString(i++));
        json.put("size",    array.getInt(i++));
        json.put("type",    array.getInt(i++));
        json.put("tv0",     array.getString(i++));
        return json;
    }
    
    private static JSONObject json_sub_lib(JSONArray array) {
        JSONObject json = new JSONObject();
        int i = 0;
        json.put("slid",    array.getInt(i++));
        json.put("name",    array.getString(i++));
        json.put("type",    array.getInt(i++));
        json.put("time",    array.getString(i++));
        json.put("count",   array.getInt(i++));
        return json;
    }
}
