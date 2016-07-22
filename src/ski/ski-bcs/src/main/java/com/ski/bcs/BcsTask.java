package com.ski.bcs;

import org.apache.log4j.Logger;

import com.ski.bcs.monitor.BcsMonitor;
import com.ski.common.CommonDefinition;

import fomjar.server.FjMessage;
import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServer;
import fomjar.server.FjServer.FjServerTask;
import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class BcsTask implements FjServerTask {
    
    private static final Logger logger = Logger.getLogger(BcsTask.class);
    
    public BcsTask() {
        new BcsMonitor().start();
    }

    @Override
    public void onMessage(FjServer server, FjMessageWrapper wrapper) {
        FjMessage msg = wrapper.message();
        if (!(msg instanceof FjDscpMessage)) {
            logger.error("unsupported format message, raw data:\n" + wrapper.attachment("raw"));
            return;
        }
        
        FjDscpMessage dmsg = (FjDscpMessage) msg;
        int         inst = dmsg.inst();
        JSONObject  args = dmsg.argsToJsonObject();
        switch (inst) {
        case CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY:
            logger.error("request platform account money: " + msg);
            processApplyPlatformAccountMoney(server.name(), args);
            break;
        case CommonDefinition.ISIS.INST_ECOM_APPLY_RENT_BEGIN:
            logger.error("request rent begin: " + msg);
            processApplyRentBegin(server.name(), dmsg);
            break;
        case CommonDefinition.ISIS.INST_ECOM_APPLY_RENT_END:
            logger.error("request rent end: " + msg);
            processApplyRentEnd(server.name(), dmsg);
            break;
        }
    }
    
    private static void processApplyRentBegin(String server, FjDscpMessage request) {
        JSONObject args = request.argsToJsonObject();
        int caid    = args.getInt("caid");
        int gid     = args.getInt("gid");
        int type    = args.getInt("type"); // a or b

        // check cash
        // choose account
    }
    
    private static void processApplyRentEnd(String server, FjDscpMessage request) {
        
    }
    
    private static void processApplyPlatformAccountMoney(String server, JSONObject args) {
        float money = Float.parseFloat(args.getString("money"));
        
        if (money > 0)      processApplyPlatformAccountMoney_Recharge(server, args);
        else if (money < 0) processApplyPlatformAccountMoney_Refund(server, args);
        else ;// discard
    }
    
    private static void processApplyPlatformAccountMoney_Recharge(String server, JSONObject args) {
        
    }
    
    private static void processApplyPlatformAccountMoney_Refund(String server, JSONObject args) {
        
    }
    
//    private static void processMoneyTransfer(String server, JSONObject args) {
//        String  user    = args.getString("user");
//        String  name    = args.getString("name");
//        String  money   = args.getString("money");
//        String  remark  = args.getString("remark");
//        
//        Map<String, String> params = new HashMap<String, String>();
//        // 基本参数
//        params.put("service",           "batch_trans_notify");    // 接口名称
//        params.put("partner",           FjServerToolkit.getServerConfig("bcs.alipay.pid"));   // 合作身份者ID
//        params.put("_input_charset",    "utf-8");  // 参数编码字符集
//        params.put("sign_type",         "MD5"); // 签名方式
//        params.put("sign",              "");  // 签名
//        FjServerToolkit.FjAddress addr = FjServerToolkit.getSlb().getAddress(server);
//        params.put("notify_url",        String.format("http://%s:%d/", addr.host, addr.port));   // 服务器异步通知页面路径
//        // 业务参数
//        params.put("account_name",      FjServerToolkit.getServerConfig("bcs.alipay.displayname")); // 付款账号名
//        params.put("detail_data",       String.format("%d^%s^%s^%s^%s",
//                System.currentTimeMillis(),
//                user,
//                name,
//                money,
//                remark));   // 付款详细数据
//        params.put("batch_no",          String.valueOf(System.currentTimeMillis()));   // 批量付款批次号
//        params.put("batch_num",         "1");   // 付款总笔数
//        params.put("batch_fee",         money); // 付款总金额
//        params.put("email",             FjServerToolkit.getServerConfig("bcs.alipay.user"));    // 付款账号
//        params.put("pay_date",          new SimpleDateFormat("yyyyMMdd").format(new Date()));   // 支付日期
//        // 签名
//        params.put("sign", createSignature(params, FjServerToolkit.getServerConfig("bcs.alipay.key")));
//        
//        String url = "https://mapi.alipay.com/gateway.do?" + params.entrySet()
//                .stream()
//                .map(entry->entry.getKey() + "=" + entry.getValue())
//                .collect(Collectors.joining("&"));
//        FjMessage rsp = FjSender.sendHttpRequest(new FjHttpRequest("GET", url));
//        logger.error("pay response: " + rsp);
//    }
//    
//    private static String createSignature(Map<String, String> params, String key) {
//        List<String> keys = new LinkedList<String>(params.keySet());
//        Collections.sort(keys);
//        StringBuilder sb = new StringBuilder();
//        keys.forEach(k->{
//            if (k.equals("sing")) return;
//            if (k.equals("sign_type")) return;
//            
//            if (0 < sb.length()) sb.append("&");
//            sb.append(k);
//            sb.append("=");
//            sb.append(params.get(k));
//        });
//        sb.append(key);
//        try {
//            MessageDigest digest = MessageDigest.getInstance("MD5");
//            digest.update(sb.toString().getBytes());
//            byte [] digestbyte = digest.digest();
//            StringBuilder result = new StringBuilder();
//            for (byte db : digestbyte) {
//                String dbh = Integer.toHexString(db & 0xFF);
//                if (dbh.length() < 2) result.append("0");
//                result.append(dbh);
//            }
//            return result.toString();
//        } catch (NoSuchAlgorithmException e) {logger.error("create signature by md5 failed", e);}
//        return "";
//    }

}
