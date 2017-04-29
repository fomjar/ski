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
        logger.info(String.format("%s - 0x%08X", dmsg.sid(), dmsg.inst()));
        
        switch (dmsg.inst()) {
        case ISIS.INST_UPDATE_PIC:
            processUpdatePic(server, dmsg);
            break;
        case ISIS.INST_UPDATE_SUB_LIB:
            processUpdateSubLib(server, dmsg);
            break;
        case ISIS.INST_UPDATE_DEV:
            processUpdateDev(server, dmsg);
            break;
        case ISIS.INST_UPDATE_DEV_DEL:
            processUpdateDevDel(server, dmsg);
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
        case ISIS.INST_QUERY_DEV:
            processQueryDev(server, dmsg);
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
    
    private static void processUpdatePic(FjServer server, FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        if (!args.has("did") || !args.has("name") || !args.has("size") || !args.has("type") || !args.has("path")) {
            String err = "illegal arguments, no did, name, size, type, path";
            logger.error(err + ", " + args);
            FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_ARGS, err);
            return;
        }
        
        FjServerToolkit.dscpRequest("cdb", dmsg.sid(), dmsg.inst(), args);
        waitSessionForResponse(server, dmsg);
    }
    
    private static void processUpdateSubLib(FjServer server, FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        if (!args.has("name") || !args.has("type")) {
            String err = "illegal arguments, no name, type";
            logger.error(err + ", " + args);
            FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_ARGS, err);
            return;
        }
        
        FjServerToolkit.dscpRequest("cdb", dmsg.sid(), dmsg.inst(), args);
        waitSessionForResponse(server, dmsg);
    }
    
    private static void processUpdateDev(FjServer server, FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        if (!args.has("did") || !args.has("path")) {
            String err = "illegal arguments, no did, path";
            logger.error(err + ", " + args);
            FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_ARGS, err);
            return;
        }
        
        FjServerToolkit.dscpRequest("cdb", dmsg.sid(), dmsg.inst(), args);
        waitSessionForResponse(server, dmsg);
    }
    
    private static void processUpdateDevDel(FjServer server, FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        if (!args.has("did")) {
            String err = "illegal arguments, no did";
            logger.error(err + ", " + args);
            FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_ARGS, err);
            return;
        }
        
        FjServerToolkit.dscpRequest("cdb", dmsg.sid(), dmsg.inst(), args);
        waitSessionForResponse(server, dmsg);
    }
    
    private static void processQueryPic(FjServer server, FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        if (!args.has("con") || !args.has("pf") || !args.has("pt")) {
            String err = "illegal arguments, no con, pf, pt";
            logger.error(err + ", " + args);
            FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_ARGS, err);
            return;
        }
        
        FjServerToolkit.dscpRequest("cdb", dmsg.sid(), dmsg.inst(), args);
        waitSessionForResponse(server, dmsg);
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
        
        FjServerToolkit.dscpRequest("cdb", dmsg.sid(), dmsg.inst(), args);
        waitSessionForResponse(server, dmsg);
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
    
    private static void processQueryDev(FjServer server, FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        
        FjDscpMessage req_cdb = FjServerToolkit.dscpRequest("cdb", dmsg.sid(), dmsg.inst(), args);
        server.onDscpSession(req_cdb.sid(), new FjServer.FjServerTask() {
            @Override
            public void onMessage(FjServer server, FjMessageWrapper wrapper) {
                FjDscpMessage dmsg1 = (FjDscpMessage) wrapper.message();
                JSONArray desc = (JSONArray) FjServerToolkit.dscpResponseDesc(dmsg1);
                JSONArray args = new JSONArray();
                for (int i = 0; i < desc.size(); i++) args.add(json_dev(desc.getJSONArray(i)));
                FjServerToolkit.dscpResponse(dmsg, FjServerToolkit.dscpResponseCode(dmsg1), args);
            }
            @Override
            public void initialize(FjServer server) {}
            @Override
            public void destroy(FjServer server) {}
        });
    }
    
    private static void processApplySubLibImport(FjServer server, FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        if (!args.has("pic_type")) {
            String err = "illegal arguments, no pic_type";
            logger.error(err + ", " + args);
            FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_ARGS, err);
            return;
        }
        switch (args.getInt("pic_type")) {
        case ISIS.FIELD_TYPE_MAN:
            processApplySubLibImportMan(server, dmsg);
            break;
        default:
            break;
        }
    }
    
    private static void processApplySubLibImportMan(FjServer server, FjDscpMessage dmsg) {
        JSONObject args = dmsg.argsToJsonObject();
        if (!args.has("pic_type") || !args.has("pic_path") || !args.has("slm_slid") || !args.has("slm_idno")) {
            String err = "illegal arguments, no pic_type, pic_path, slm_slid, slm_idno";
            logger.error(err + ", " + args);
            FjServerToolkit.dscpResponse(dmsg, FjISIS.CODE_ILLEGAL_ARGS, err);
            return;
        }
        
        // pic
        JSONObject args_cdb = new JSONObject();
        String pic_name = args.getString("pic_path").substring(args.getString("pic_path").lastIndexOf("/") + 1);
        if (args.has("slm_name")) pic_name = args.getString("slm_name");
        args_cdb.put("name", pic_name);
        args_cdb.put("size", ISIS.FIELD_PIC_SIZE_SMALL);
        args_cdb.put("type", args.getInt("pic_type"));
        args_cdb.put("path", args.getString("pic_path").replace("\\", "/"));
        args_cdb.put("fv",   args.getString("pic_fv"));
        args_cdb.put("vd",   args.getString("pic_fv").split(" ").length);
        FjDscpMessage req_cdb = FjServerToolkit.dscpRequest("cdb", dmsg.sid(), ISIS.INST_UPDATE_PIC, args_cdb);
        server.onDscpSession(req_cdb.sid(), new FjServer.FjServerTask() {
            @Override
            public void onMessage(FjServer server, FjMessageWrapper wrapper) {
                FjDscpMessage rsp_cdb = (FjDscpMessage) wrapper.message();
                if (FjISIS.CODE_SUCCESS != FjServerToolkit.dscpResponseCode(rsp_cdb)) {
                    logger.error("import failed by updating pic: " + rsp_cdb);
                    FjServerToolkit.dscpResponse(dmsg, FjServerToolkit.dscpResponseCode(rsp_cdb), FjServerToolkit.dscpResponseDesc(rsp_cdb));
                    return;
                }
                int pid = Integer.parseInt(((JSONArray)FjServerToolkit.dscpResponseDesc(rsp_cdb)).getString(0), 16);
                
                // sub man
                JSONObject args_cdb = new JSONObject();
                args_cdb.put("slid", args.getInt("slm_slid"));
                if (args.has("slm_name")) args_cdb.put("name", args.getString("slm_name"));
                String idno = args.getString("slm_idno");
                args_cdb.put("gender", getIdnoGender(idno));
                args_cdb.put("birth",  getIdnoBirth(idno));
                args_cdb.put("idno",   idno);
                if (args.has("slm_phone")) args_cdb.put("phone", args.getString("slm_phone"));
                if (args.has("slm_addr"))  args_cdb.put("addr", args.getString("slm_addr"));
                FjDscpMessage req_cdb = FjServerToolkit.dscpRequest("cdb", dmsg.sid(), ISIS.INST_UPDATE_SUB_MAN, args_cdb);
                server.onDscpSession(req_cdb.sid(), new FjServer.FjServerTask() {
                    @Override
                    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
                        FjDscpMessage rsp_cdb = (FjDscpMessage) wrapper.message();
                        if (FjISIS.CODE_SUCCESS != FjServerToolkit.dscpResponseCode(rsp_cdb)) {
                            logger.error("import failed by updating sub man: " + rsp_cdb);
                            FjServerToolkit.dscpResponse(dmsg, FjServerToolkit.dscpResponseCode(rsp_cdb), FjServerToolkit.dscpResponseDesc(rsp_cdb));
                            return;
                        }
                        int smid = Integer.parseInt(((JSONArray)FjServerToolkit.dscpResponseDesc(rsp_cdb)).getString(0), 16);
                        
                        // sub man pic
                        JSONObject args_cdb = new JSONObject();
                        args_cdb.put("smid", smid);
                        args_cdb.put("pid", pid);
                        FjDscpMessage req_cdb = FjServerToolkit.dscpRequest("cdb", dmsg.sid(), ISIS.INST_UPDATE_SUB_MAN_PIC, args_cdb);
                        server.onDscpSession(req_cdb.sid(), new FjServer.FjServerTask() {
                            @Override
                            public void onMessage(FjServer server, FjMessageWrapper wrapper) {
                                FjDscpMessage rsp_cdb = (FjDscpMessage) wrapper.message();
                                if (FjISIS.CODE_SUCCESS != FjServerToolkit.dscpResponseCode(rsp_cdb)) {
                                    logger.error("import failed by updating sub man pic: " + rsp_cdb);
                                    FjServerToolkit.dscpResponse(dmsg, FjServerToolkit.dscpResponseCode(rsp_cdb), FjServerToolkit.dscpResponseDesc(rsp_cdb));
                                    return;
                                }
                                // success
                                FjServerToolkit.dscpResponse(dmsg, FjServerToolkit.dscpResponseCode(rsp_cdb), FjServerToolkit.dscpResponseDesc(rsp_cdb));
                                logger.info("import sub success");
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
    
    private static JSONObject json_pic(JSONArray array) {
        JSONObject json = new JSONObject();
        int i = 0;
        json.put("pid",     array.getInt(i++));
        json.put("did",     array.getString(i++));
        json.put("name",    array.getString(i++));
        json.put("time",    array.getString(i++));
        json.put("size",    array.getInt(i++));
        json.put("type",    array.getInt(i++));
        json.put("path",    array.getString(i++));
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
    
    private static JSONObject json_dev(JSONArray array) {
        JSONObject json = new JSONObject();
        int i = 0;
        json.put("did",     array.getString(i++));
        json.put("path",    array.getString(i++));
        json.put("time",    array.getString(i++));
        json.put("ip",      array.getString(i++));
        json.put("port",    array.getInt(i++));
        json.put("user",    array.getString(i++));
        json.put("pass",    array.getString(i++));
        return json;
    }
}
