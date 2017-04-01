package com.ski.frs.bcs;

import java.util.HashMap;
import java.util.Map;

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
    
    private Map<String, FjDscpMessage> cache = new HashMap<String, FjDscpMessage>();

    @Override
    public void initialize(FjServer server) {}

    @Override
    public void destroy(FjServer server) {
        cache.clear();
    }

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
            processUpdatePic(dmsg);
            break;
        case ISIS.INST_QUERY_PIC:
            processQueryPic(dmsg);
            break;
        case ISIS.INST_QUERY_PIC_BY_FV_I:
            processQueryPicByFVI(dmsg);
            break;
        case ISIS.INST_QUERY_PIC_BY_FV:
            processQueryPicByFV(dmsg);
            break;
        }
    }
    
    private void processUpdatePic(FjDscpMessage dmsg) {
        if (dmsg.fs().startsWith("cdb")) return;
        
        JSONObject args = dmsg.argsToJsonObject();
        FjServerToolkit.dscpRequest("cdb", dmsg.sid(), ISIS.INST_UPDATE_PIC, args);
    }
    
    private void processQueryPic(FjDscpMessage dmsg) {
        if (dmsg.fs().startsWith("cdb")) {
            FjServerToolkit.dscpResponse(cache.remove(dmsg.sid()), FjServerToolkit.dscpResponseCode(dmsg), FjServerToolkit.dscpResponseDesc(dmsg));
        } else {
            JSONObject args = dmsg.argsToJsonObject();
            if (!args.has("con") || !args.has("pf") || !args.has("pt")) {
                String err = "illegal arguments, no con, pf, pt";
                logger.error(err + ", " + args);
                FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_ARGS, err);
                return;
            }
            
            FjServerToolkit.dscpRequest("cdb", dmsg.sid(), dmsg.inst(), args);
            cache.put(dmsg.sid(), dmsg);
        }
    }
    
    private void processQueryPicByFVI(FjDscpMessage dmsg) {
        if (dmsg.fs().startsWith("cdb")) {
            FjServerToolkit.dscpResponse(cache.remove(dmsg.sid()), FjServerToolkit.dscpResponseCode(dmsg), FjServerToolkit.dscpResponseDesc(dmsg));
        } else {
            JSONObject args = dmsg.argsToJsonObject();
            if (!args.has("fv")) {
                String err = "illegal arguments, no fv";
                logger.error(err + ", " + args);
                FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_ARGS, err);
                return;
            }
            
            args.put("vd", Integer.parseInt(FjServerToolkit.getServerConfig("bcs.face.vd"))); // 向量维数
            FjServerToolkit.dscpRequest("cdb", dmsg.sid(), dmsg.inst(), args);
            cache.put(dmsg.sid(), dmsg);
        }
    }
    private void processQueryPicByFV(FjDscpMessage dmsg) {
        if (dmsg.fs().startsWith("cdb")) {
            JSONArray desc = (JSONArray) FjServerToolkit.dscpResponseDesc(dmsg);
            JSONArray args = new JSONArray();
            for (int i = 0; i < desc.size(); i++) args.add(json_pic(desc.getJSONArray(i)));
            FjServerToolkit.dscpResponse(cache.remove(dmsg.sid()), FjServerToolkit.dscpResponseCode(dmsg), args);
        } else {
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
            
            FjServerToolkit.dscpRequest("cdb", dmsg.sid(), dmsg.inst(), args);
            cache.put(dmsg.sid(), dmsg);
        }
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
}
