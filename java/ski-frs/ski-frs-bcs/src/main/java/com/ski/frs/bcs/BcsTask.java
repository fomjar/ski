package com.ski.frs.bcs;

import java.util.List;

import org.apache.log4j.Logger;

import com.ski.frs.isis.ISIS;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.FjServerToolkit;
import fomjar.server.FjServerToolkit.FjAddress;
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
        logger.info(String.format("%s - 0x%08X", dmsg.sid(), dmsg.inst()));
        
        switch (dmsg.inst()) {
        case ISIS.INST_SET_PIC:
        case ISIS.INST_DEL_PIC:
        case ISIS.INST_GET_PIC:
        case ISIS.INST_SET_SUB:
        case ISIS.INST_DEL_SUB:
        case ISIS.INST_MOD_SUB:
        case ISIS.INST_GET_SUB:
        case ISIS.INST_SET_SUB_ITEM:
        case ISIS.INST_DEL_SUB_ITEM:
        case ISIS.INST_GET_SUB_ITEM:
        case ISIS.INST_SET_DEV:
        case ISIS.INST_DEL_DEV:
        case ISIS.INST_GET_DEV:
            processStoreBlock(server, dmsg);
            break;
        case ISIS.INST_GET_OPP:
            processGetOpp(server, dmsg);
            break;
        case ISIS.INST_APPLY_SUB_IMPORT:
            processApplySubImport(server, dmsg);
            break;
        case ISIS.INST_APPLY_DEV_IMPORT:
            processApplyDevImport(server, dmsg);
            break;
        default: {
            String err = String.format("illegal inst: 0x%08X", dmsg.inst());
            logger.error(err);
            FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_INST, err);
            break;
        }
        }
    }
    
    private static void waitSessionForResponse(FjServer server, FjDscpMessage req) {
        server.onDscpSession(req.sid(), new FjServer.FjServerTask() {
            @Override
            public void onMessage(FjServer server, FjMessageWrapper wrapper) {
                FjDscpMessage rsp = (FjDscpMessage) wrapper.message();
                FjServerToolkit.dscpResponse(req, FjServerToolkit.dscpResponseCode(rsp), FjServerToolkit.dscpResponseDesc(rsp));
            }
            @Override
            public void initialize(FjServer server) {}
            @Override
            public void destroy(FjServer server) {}
        });
    }
    
    private static void processStoreBlock(FjServer server, FjDscpMessage dmsg) {
        FjServerToolkit.dscpRequest("ccu", dmsg.sid(), dmsg.ttl() - 1, dmsg.inst(), dmsg.argsToJsonObject());
        waitSessionForResponse(server, dmsg);
    }
    
    private static void processGetOpp(FjServer server, FjDscpMessage dmsg) {
        List<FjAddress> addr = FjServerToolkit.getSlb().getAddresses("opp");
        JSONArray desc = new JSONArray();
        addr.forEach(a->{
            JSONObject o = new JSONObject();
            o.put("server", a.server);
            o.put("host", a.host);
            o.put("port", a.port);
            desc.add(o);
        });
        FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_SUCCESS, desc);
    }
    
    private static void processApplySubImport(FjServer server, FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        if (!args.has("p_type")) {
            String err = "illegal arguments, no p_type";
            logger.error(err + ", " + args);
            FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_ARGS, err);
            return;
        }
        switch (args.getInt("p_type")) {
        case ISIS.FIELD_TYPE_MAN:
            processApplySubLibImportMan(server, dmsg);
            break;
        default:
            break;
        }
    }
    
    private static void processApplySubLibImportMan(FjServer server, FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        if (!args.has("sid") || !args.has("siid") || !args.has("p_type") || !args.has("p_size") || !args.has("p_path") || !args.has("p_fv") || !args.has("s_idno")) {
            String err = "illegal arguments, no sid, siid, p_type, p_size, p_path, p_fv, s_idno";
            logger.error(err + ", " + args);
            FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_ARGS, err);
            return;
        }
        // subject item
        String idno = args.getString("s_idno");
        JSONObject args_ccu = new JSONObject();
        args_ccu.put("sid",     args.get("sid"));
        args_ccu.put("siid",    args.get("siid"));
        args_ccu.put("idno",    idno);
        args_ccu.put("gender",  getIdnoGender(idno));
        args_ccu.put("birth",   getIdnoBirth(idno));
        if (args.has("s_name"))     args_ccu.put("name", args.get("s_name"));
        if (args.has("s_phone"))    args_ccu.put("phone", args.get("s_phone"));
        if (args.has("s_addr"))     args_ccu.put("addr", args.get("s_addr"));
        FjDscpMessage req_ccu = FjServerToolkit.dscpRequest("ccu", ISIS.INST_SET_SUB_ITEM, args_ccu);
        server.onDscpSession(req_ccu.sid(), new FjServer.FjServerTask() {
            @Override
            public void onMessage(FjServer server, FjMessageWrapper wrapper) {
                FjDscpMessage rsp_ccu = (FjDscpMessage) wrapper.message();
                if (FjISIS.CODE_SUCCESS != FjServerToolkit.dscpResponseCode(rsp_ccu)) {
                    logger.error("set sub item failed: " + rsp_ccu);
                    FjServerToolkit.dscpResponse(dmsg, FjServerToolkit.dscpResponseCode(rsp_ccu), FjServerToolkit.dscpResponseDesc(rsp_ccu));
                    return;
                }
                args_ccu.clear();
                args_ccu.put("sid",  args.get("sid"));
                args_ccu.put("siid", args.get("siid"));
                if (args.has("p_name")) args_ccu.put("name", args.get("p_name"));
                args_ccu.put("type", args.get("p_type"));
                args_ccu.put("size", args.get("p_size"));
                args_ccu.put("path", args.get("p_path"));
                if (args.has("p_fv")) args_ccu.put("fv", args.get("p_fv"));
                FjDscpMessage req_ccu = FjServerToolkit.dscpRequest("ccu", ISIS.INST_SET_PIC, args_ccu);
                server.onDscpSession(req_ccu.sid(), new FjServer.FjServerTask() {
                    @Override
                    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
                        FjDscpMessage rsp_ccu = (FjDscpMessage) wrapper.message();
                        if (FjISIS.CODE_SUCCESS != FjServerToolkit.dscpResponseCode(rsp_ccu)) {
                            logger.error("set pic failed: " + rsp_ccu);
                            FjServerToolkit.dscpResponse(dmsg, FjServerToolkit.dscpResponseCode(rsp_ccu), FjServerToolkit.dscpResponseDesc(rsp_ccu));
                            return;
                        }
                        FjServerToolkit.dscpResponse(dmsg, FjServerToolkit.dscpResponseCode(rsp_ccu), FjServerToolkit.dscpResponseDesc(rsp_ccu));
                    }
                    @Override
                    public void initialize(FjServer server) {}
                    @Override
                    public void destroy(FjServer server) {}
                });
            }
            @Override
            public void initialize(FjServer server) {}
            @Override
            public void destroy(FjServer server) {}
        });
    }
    
    private static int getIdnoGender(String idno) {
        switch (idno.length()) {
        case 15:    // 15位身份证号码: 第15位代表性别，奇数为男，偶数为女。
            return Integer.parseInt(idno.substring(14, 15)) % 2;
        case 18:    // 18位身份证号码: 第17位代表性别，奇数为男，偶数为女。
            return Integer.parseInt(idno.substring(16, 17)) % 2;
        default:
            return -1;
        }
    }
    
    private static String getIdnoBirth(String idno) {
        switch (idno.length()) {
        case 15:    // 15位身份证号码: 第7、8位为出生年份(两位数)，第9、10位为出生月份，第11、12位代表出生日期。
            return "19" + idno.substring(6, 12);
        case 18:    // 18位身份证号码: 第7、8、9、10位为出生年份(四位数)，第11、第12位为出生月份，第13、14位代表出生日期。
            return idno.substring(6, 14);
        default:
            return "20000101";
        }
    }
    
    private static void processApplyDevImport(FjServer server, FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        if (!args.has("opp") || !args.has("did") || !args.has("path")) {
            String err = "illegal arguments, no opp, did, path";
            logger.error(err + ", " + args);
            FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_ARGS, err);
            return;
        }
        FjDscpMessage req_opp = FjServerToolkit.dscpRequest(args.getString("opp"), dmsg.inst(), args);
        server.onDscpSession(req_opp.sid(), new FjServer.FjServerTask() {
            @Override
            public void onMessage(FjServer server, FjMessageWrapper wrapper) {
                FjDscpMessage rsp_opp = (FjDscpMessage) wrapper.message();
                FjServerToolkit.dscpResponse(dmsg, FjServerToolkit.dscpResponseCode(rsp_opp), FjServerToolkit.dscpResponseDesc(rsp_opp));
            }
            @Override
            public void initialize(FjServer server) {
            }
            @Override
            public void destroy(FjServer server) {
            }
        });
    }
}
