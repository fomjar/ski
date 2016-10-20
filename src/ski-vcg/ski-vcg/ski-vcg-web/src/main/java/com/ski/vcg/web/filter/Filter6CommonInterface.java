package com.ski.vcg.web.filter;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ski.vcg.common.CommonDefinition;
import com.ski.vcg.common.CommonService;
import com.ski.vcg.common.bean.BeanChannelAccount;
import com.ski.vcg.common.bean.BeanChannelCommodity;
import com.ski.vcg.common.bean.BeanChatroom;
import com.ski.vcg.common.bean.BeanChatroomMember;
import com.ski.vcg.common.bean.BeanChatroomMessage;
import com.ski.vcg.common.bean.BeanCommodity;
import com.ski.vcg.common.bean.BeanGame;
import com.ski.vcg.common.bean.BeanGameAccount;
import com.ski.vcg.common.bean.BeanGameRentPrice;
import com.ski.vcg.common.bean.BeanPlatformAccount;
import com.ski.vcg.web.baidu.BaiduMapInterface;
import com.ski.vcg.web.wechat.WechatBusiness;
import com.ski.vcg.web.wechat.WechatInterface;

import fomjar.server.FjServerToolkit;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjHttpResponse;
import fomjar.server.msg.FjXmlMessage;
import fomjar.server.web.FjWebFilter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Filter6CommonInterface extends FjWebFilter {
    
    private static final Logger logger = Logger.getLogger(Filter6CommonInterface.class);
    
    public static final String URL_KEY = "/ski-web";
    
    private WechatBusiness wechat;
    
    public Filter6CommonInterface(WechatBusiness wechat) {
        this.wechat = wechat;
    }

    @Override
    public boolean filter(FjHttpResponse response, FjHttpRequest request, SocketChannel conn) {
        if (!request.path().startsWith(URL_KEY)) return true;
        
        logger.info(String.format("user common command: %s - %s", request.url(), request.contentToString().replace("\n", "")));
        
        switch (request.path()) {
        case URL_KEY + "/pay/recharge/success":
            processPayRechargeSuccess(response, request.contentToXml());
            break;
        default: {
            JSONObject args = request.argsToJson();
            if (args.has("inst")) {
                switch (getIntFromArgs(args, "inst")) {
                case CommonDefinition.ISIS.INST_ECOM_APPLY_MAKE_COVER:
                    processApplyMakeCover(response, request);
                    break;
                case CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY:
                    processApplyPlatformAccountMoney(response, request, conn);
                    break;
                case CommonDefinition.ISIS.INST_ECOM_APPLY_RENT_BEGIN:
                    processApplyRentBegin(response, request);
                    break;
                case CommonDefinition.ISIS.INST_ECOM_APPLY_RENT_END:
                    processApplyRentEnd(response, request);
                    break;
                case CommonDefinition.ISIS.INST_ECOM_QUERY_CHANNEL_ACCOUNT:
                    processQueryChannelAccount(response, request);
                    break;
                case CommonDefinition.ISIS.INST_ECOM_QUERY_CHATROOM:
                    processQueryChatroom(response, request);
                    break;
                case CommonDefinition.ISIS.INST_ECOM_QUERY_CHATROOM_MEMBER:
                    processQueryChatroomMember(response, request);
                    break;
                case CommonDefinition.ISIS.INST_ECOM_QUERY_CHATROOM_MESSAGE:
                    processQueryChatroomMessage(response, request);
                    break;
                case CommonDefinition.ISIS.INST_ECOM_QUERY_GAME:
                    processQueryGame(response, request);
                    break;
                case CommonDefinition.ISIS.INST_ECOM_QUERY_ORDER:
                    processQueryOrder(response, request);
                    break;
                case CommonDefinition.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT:
                    processQueryPlatformAccount(response, request);
                    break;
                case CommonDefinition.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT_MAP:
                    processQueryPlatformAccountMap(response, request);
                    break;
                case CommonDefinition.ISIS.INST_ECOM_UPDATE_CHATROOM:
                    processUpdateChatroom(response, request);
                    break;
                case CommonDefinition.ISIS.INST_ECOM_UPDATE_CHATROOM_MEMBER:
                    processUpdateChatroomMember(response, request);
                    break;
                case CommonDefinition.ISIS.INST_ECOM_UPDATE_CHATROOM_MESSAGE:
                    processUpdateChatroomMessage(response, request);
                    break;
                case CommonDefinition.ISIS.INST_ECOM_UPDATE_PLATFORM_ACCOUNT_MAP:
                    processUpdatePlatformAccountMap(response, request);
                    break;
                }
            }
            break;
        }
        }
        return true;
    }
    
    private Map<String, byte[]> cache_cover_string = new ConcurrentHashMap<String, byte[]>();
    
    private void processApplyMakeCover(FjHttpResponse response, FjHttpRequest request) {
        JSONObject args = request.argsToJson();
        if (args.has("string")) {
            String string = args.getString("string");
            string = Base64.getEncoder().encodeToString(string.getBytes());
            if (2 < string.length()) string = string.substring(0, 2);
            
            byte[] buf = null;
            
            if (cache_cover_string.containsKey(string)) {
                buf = cache_cover_string.get(string);
            } else {
                int width  = args.has("width") ? getIntFromArgs(args, "width") : 100;
                int height = args.has("height") ? getIntFromArgs(args, "height") : 100;
                int cell   = width / 10;
                int padding = width / 20;
                
                BufferedImage img0 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g0 = img0.createGraphics();
                Font font = new Font(null, Font.PLAIN, (width - padding * 2) / 2);
                g0.setFont(font);
                g0.setColor(Color.black);
                g0.fillRect(0, 0, width, height);
                g0.setColor(Color.white);
                g0.drawString(string, 
                        (width - g0.getFontMetrics(font).stringWidth(string)) / 2,
                        g0.getFontMetrics(font).getAscent() + (height - g0.getFontMetrics(font).getAscent()) / 2);
                g0.dispose();
                
                BufferedImage img1 = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g1 = img1.createGraphics();
                g1.setColor(Color.gray);
                g1.fillRect(0, 0, width, height);
                g1.setColor(Color.white);
                for (int i = 0; i < width / cell; i++) {
                    for (int j = 0; j < height / cell; j++) {
                        int tr = 0;
                        int tg = 0;
                        int tb = 0;
                        int count = 0;
                        for (int x = 0; x < cell; x++) {
                            for (int y = 0; y < cell; y++) {
                                int rgb = img0.getRGB(i * cell + x, j * cell + y);
                                tr += (rgb & 0xFF0000) >> 16;
                                tg += (rgb & 0x00FF00) >> 8;
                                tb += (rgb & 0x0000FF) >> 0;
                                count++;
                            }
                        }
                        tr /= count;
                        tg /= count;
                        tb /= count;
                        if (tr + tg + tb >= 255 / 2) {
                            g1.fillRect(i * cell, j * cell, cell, cell);
                        }
                    }
                }
                g1.dispose();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {ImageIO.write(img1, "jpg", baos);}
                catch (IOException e) {e.printStackTrace();}
                
                buf = baos.toByteArray();
                
                cache_cover_string.put(string, buf);
            }
            
            response.attr().put("Content-Type", "image/jpg");
            response.content(buf);
        }
    }
        
    private void processApplyPlatformAccountMoney(FjHttpResponse response, FjHttpRequest request, SocketChannel conn) {
        switch (request.path()) {
        case URL_KEY + "/pay/recharge/prepare":
            processApplyPlatformAccountMoney_Recharge_Prepare(response, request);
            break;
        case URL_KEY + "/pay/recharge/apply":
            processApplyPlatformAccountMoney_Recharge_Apply(response, request, conn);
            break;
        case URL_KEY + "/pay/refund":
            processApplyPlatformAccountMoney_Refund(response, request, conn);
            break;
        }
    }
    
    private void processApplyPlatformAccountMoney_Recharge_Prepare(FjHttpResponse response, FjHttpRequest request) {
        long timestamp  = System.currentTimeMillis() / 1000;
        String noncestr = Long.toHexString(System.currentTimeMillis());
        JSONObject desc = new JSONObject();
        desc.put("appid",       FjServerToolkit.getServerConfig("web.wechat.appid"));
        desc.put("timestamp",   String.valueOf(timestamp));
        desc.put("noncestr",    noncestr);
        desc.put("signature",   WechatInterface.createSignature4Config(noncestr,
                wechat.token_monitor().ticket(),
                timestamp,
                request.attr().get("Referer")));
        JSONObject args = new JSONObject();
        args.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
        args.put("desc", desc);
        response.attr().put("Content-Type", "application/json");
        response.content(args);
    }
        
    private Set<Integer> cache_user_recharge = new HashSet<Integer>();

    private void processApplyPlatformAccountMoney_Recharge_Apply(FjHttpResponse response, FjHttpRequest request, SocketChannel conn) {
        JSONObject args = request.argsToJson();
        int user = Integer.parseInt(request.cookie().get("user"), 16);
        String money = args.getString("money");
        String terminal = "127.0.0.1";
        try {terminal = ((InetSocketAddress) conn.getRemoteAddress()).getAddress().getHostAddress();}
        catch (IOException e) {logger.error("get user terminal address failed", e);}
        FjXmlMessage rsp = WechatInterface.prepay(
                "VC电玩-充值",
                "您已成功充值" + money + "元",
                (int) (Float.parseFloat(money) * 100),
                terminal,
                String.format("http://%s%s/pay/recharge/success", FjServerToolkit.getSlb().getAddress("web").host, URL_KEY),
                CommonService.getChannelAccountByCaid(user).c_user);
        
        String timeStamp    = String.valueOf(System.currentTimeMillis() / 1000);
        String nonceStr     = Long.toHexString(System.currentTimeMillis());
        JSONObject json_prepay = xml2json(rsp.xml());
        
        Map<String, String> map = new HashMap<String, String>();
        map.put("appId",        FjServerToolkit.getServerConfig("web.wechat.appid"));
        map.put("timeStamp",    timeStamp);
        map.put("nonceStr",     nonceStr);
        map.put("package",      "prepay_id=" + json_prepay.getString("prepay_id"));
        map.put("signType",     "MD5");
        String paySign = WechatInterface.createSignature4Pay(map);
        
        JSONObject json_pay = new JSONObject();
        json_pay.put("appId",       FjServerToolkit.getServerConfig("web.wechat.appid"));
        json_pay.put("timeStamp",   timeStamp);
        json_pay.put("nonceStr",    nonceStr);
        json_pay.put("package",     "prepay_id=" + json_prepay.getString("prepay_id"));
        json_pay.put("signType",    "MD5");
        json_pay.put("paySign",     paySign);
        json_prepay.put("pay", json_pay);
        
        cache_user_recharge.add(user);
        
        JSONObject args_rsp = new JSONObject();
        args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
        args_rsp.put("desc", json_prepay);
        
        response.attr().put("Content-Type", "application/json");
        response.content(args_rsp);
    }
    /**
     * <xml>
     * <appid><![CDATA[wx9c65a26e4f512fd4]]></appid>
     * <attach><![CDATA[您已成功充值1元]]></attach>
     * <bank_type><![CDATA[CFT]]></bank_type>
     * <cash_fee><![CDATA[100]]></cash_fee>
     * <device_info><![CDATA[WEB]]></device_info>
     * <fee_type><![CDATA[CNY]]></fee_type>
     * <is_subscribe><![CDATA[Y]]></is_subscribe>
     * <mch_id><![CDATA[1364744702]]></mch_id>
     * <nonce_str><![CDATA[155e55b5e2f4ec1f9d155793]]></nonce_str>
     * <openid><![CDATA[oRojEwPTK3o2cYrLsXuuX-FuypBM]]></openid>
     * <out_trade_no><![CDATA[20160714014338288]]></out_trade_no>
     * <result_code><![CDATA[SUCCESS]]></result_code>
     * <return_code><![CDATA[SUCCESS]]></return_code>
     * <sign><![CDATA[5B66E346DF7FAE2A508A933092AB6590]]></sign>
     * <time_end><![CDATA[20160714014342]]></time_end>
     * <total_fee>100</total_fee>
     * <trade_type><![CDATA[JSAPI]]></trade_type>
     * <transaction_id><![CDATA[4003922001201607148930843003]]></transaction_id>
     * </xml>
     * 
     * @param xml
     */
    private void processPayRechargeSuccess(FjHttpResponse response, Document xml) {
        JSONObject args = new JSONObject();
        NodeList nodes = xml.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (null == node.getFirstChild()) continue;
            args.put(node.getNodeName(), node.getFirstChild().getNodeValue());
        }

        List<BeanChannelAccount> users = CommonService.getChannelAccountByUserNChannel(args.getString("openid"), CommonService.CHANNEL_WECHAT);
        if (users.isEmpty()) return;
        
        BeanChannelAccount user = users.get(0);
        if (cache_user_recharge.contains(user.i_caid)) {
            cache_user_recharge.remove(user.i_caid);
            
            logger.error("user pay recharge: " + args);
            float money = ((float) args.getInt("total_fee")) / 100;
            JSONObject args_cdb = new JSONObject();
            args_cdb.put("caid",    user.i_caid);
            args_cdb.put("money",   money);
            FjDscpMessage msg_cdb = new FjDscpMessage();
            msg_cdb.json().put("fs",   FjServerToolkit.getAnyServer().name());
            msg_cdb.json().put("ts",   "bcs");
            msg_cdb.json().put("inst", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY);
            msg_cdb.json().put("args", args_cdb);
            FjServerToolkit.getAnySender().send(msg_cdb);
        }
        
        response.attr().put("Content-Type", "text/xml");
        response.content("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
    }
    
    private static JSONObject xml2json(Document xml) {
        JSONObject json = new JSONObject();
        NodeList nodes = xml.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (null == node.getFirstChild()) continue;
            json.put(node.getNodeName(), node.getFirstChild().getNodeValue());
        }
        return json;
    }

    private void processApplyPlatformAccountMoney_Refund(FjHttpResponse response, FjHttpRequest request, SocketChannel conn) {
        int user = Integer.parseInt(request.cookie().get("user"), 16);
        float money = CommonService.prestatementByCaid(user)[0];
        
        JSONObject args_bcs = new JSONObject();
        args_bcs.put("caid",    user);
        args_bcs.put("money",   -money);
        FjDscpMessage rsp = CommonService.send("bcs", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY, args_bcs);
        JSONObject args_rsp = rsp.argsToJsonObject();
        
        if (CommonService.isResponseSuccess(rsp)) {
            // 发红包
            String terminal = "127.0.0.1";
            try {terminal = ((InetSocketAddress) conn.getRemoteAddress()).getAddress().getHostAddress();}
            catch (IOException e) {logger.error("get user terminal address failed", e);}
            sendredpack(terminal, CommonService.getChannelAccountByCaid(user).c_user, money);
        }
        
        logger.error("user pay refund: " + args_rsp);
        response.attr().put("Content-Type", "application/json");
        response.content(args_rsp);
    }
    
    private static void sendredpack(String terminal, String user, float money) {
        float max = Float.parseFloat(FjServerToolkit.getServerConfig("web.wechat.redpack.max"));
        long  interval = Long.parseLong(FjServerToolkit.getServerConfig("web.wechat.redpack.interval"));
        new Thread(()->{
            try {
                float m = money;
                List<Float> moneys = new LinkedList<Float>();
                while (m > max) {
                    moneys.add(max);
                    m -= max;
                }
                moneys.add(m);
                
                for (int i = 0; i < moneys.size(); i++) {
                    FjXmlMessage rsp_redpack = WechatInterface.sendredpack("VC电玩",
                            user,
                            moneys.get(i),
                            String.format("VC电玩游戏退款(%d/%d)", i + 1, moneys.size()),
                            terminal,
                            "VC电玩活动送好礼",
                            "关注VC电玩");
                    logger.error("send red pack: " + rsp_redpack);
                    Thread.sleep(interval * 1000L);
                }
            } catch (Exception e) {logger.error("error occurs when send redpack", e);}
        }).start();
    }
    
    private void processApplyRentBegin(FjHttpResponse response, FjHttpRequest request) {
        JSONObject args = request.argsToJson();
        int user    = Integer.parseInt(request.cookie().get("user"), 16);
        int gid     = getIntFromArgs(args, "gid");
        int type    = getIntFromArgs(args, "type");
        JSONObject args_bcs = new JSONObject();
        args_bcs.put("platform",    CommonService.CHANNEL_WECHAT);
        args_bcs.put("caid",        user);
        args_bcs.put("gid",         gid);
        args_bcs.put("type",        type);
        FjDscpMessage rsp = CommonService.send("bcs", CommonDefinition.ISIS.INST_ECOM_APPLY_RENT_BEGIN, args_bcs);
        if (!CommonService.isResponseSuccess(rsp)) {
            logger.error("apply rent begin failed: " + rsp);
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonService.getResponseCode(rsp));
            args_rsp.put("desc", CommonService.getResponseDesc(rsp));
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
            return;
        }
        JSONObject args_rsp = new JSONObject();
        args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
        args_rsp.put("desc", null);
        response.attr().put("Content-Type", "application/json");
        response.content(args_rsp);
    }
    
    private void processApplyRentEnd(FjHttpResponse response, FjHttpRequest request) {
        JSONObject args = request.argsToJson();
        int user    = Integer.parseInt(request.cookie().get("user"), 16);
        int oid     = getIntFromArgs(args, "oid");
        int csn     = getIntFromArgs(args, "csn");
        JSONObject args_bcs = new JSONObject();
        args_bcs.put("caid",    user);
        args_bcs.put("oid",     oid);
        args_bcs.put("csn",     csn);
        FjDscpMessage rsp = CommonService.send("bcs", CommonDefinition.ISIS.INST_ECOM_APPLY_RENT_END, args_bcs);
        if (!CommonService.isResponseSuccess(rsp)) {
            logger.error("apply rent end failed: " + rsp);
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonService.getResponseCode(rsp));
            args_rsp.put("desc", CommonService.getResponseDesc(rsp));
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
            return;
        }
        JSONObject args_rsp = new JSONObject();
        args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
        args_rsp.put("desc", null);
        response.attr().put("Content-Type", "application/json");
        response.content(args_rsp);
    }
    
    private void processQueryChannelAccount(FjHttpResponse response, FjHttpRequest request) {
        JSONObject args = request.argsToJson();
        
        if (args.has("caid")) {
            int caid = getIntFromArgs(args, "caid");
            BeanChannelAccount user = null;
            if (null == (user = CommonService.getChannelAccountByCaid(caid))) {
                JSONObject args_rsp = new JSONObject();
                args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS);
                args_rsp.put("desc", "caid not exist");
                response.attr().put("Content-Type", "application/json");
                response.content(args_rsp);
                return;
            }
            
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
            args_rsp.put("desc", tojson(user));
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
        } else {
            JSONArray desc = new JSONArray();
            CommonService.getChannelAccountAll().values().forEach(user->desc.add(tojson(user)));
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
            args_rsp.put("desc", desc);
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
        }
    }
    
    private void processQueryChatroom(FjHttpResponse response, FjHttpRequest request) {
        JSONObject args = request.argsToJson();
        
        // query/update chatroom
        List<BeanChatroom> chatrooms = null;
        if (args.has("gid")) {
            int gid = getIntFromArgs(args, "gid");
            chatrooms = CommonService.getChatroomByGid(gid);
            if (null == chatrooms || chatrooms.isEmpty()) {
                processUpdateChatroom(response, request);
                CommonService.updateChatroom();
                
                chatrooms = CommonService.getChatroomByGid(gid);
                if (null == chatrooms || chatrooms.isEmpty()) return;
            }
        } else {
            logger.error("illegal arguments for query chatroom: " + args);
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS);
            args_rsp.put("desc", "非法参数");
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
            return;
        }
        
        JSONArray desc = new JSONArray();
        chatrooms.forEach(cr->desc.add(tojson(cr)));
        JSONObject args_rsp = new JSONObject();
        args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
        args_rsp.put("desc", desc);
        response.attr().put("Content-Type", "application/json");
        response.content(args_rsp);
    }
    
    private void processQueryChatroomMember(FjHttpResponse response, FjHttpRequest request) {
        JSONObject args = request.argsToJson();
        int user = Integer.parseInt(request.cookie().get("user"), 16);
        
        if (!args.has("crid")) {
            logger.error("illegal arguments for query chatroom member: " + args);
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS);
            args_rsp.put("desc", "非法参数");
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
            return;
        }
        
        int     crid    = getIntFromArgs(args, "crid");
        int     member  = CommonService.getPlatformAccountByCaid(user);
        boolean ismember = false;
        
        List<BeanChatroomMember> members = CommonService.getChatroomMemberByCrid(crid);
        for (BeanChatroomMember crm : members) {
            if (crm.i_member == member) {
                ismember = true;
                break;
            }
        }
        if (!ismember) {
            logger.error("non-member can not query chatroom member: " + args);
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_USER_NO_PRIVILEGE);
            args_rsp.put("desc", "非聊天室成员不能查看成员");
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
            return;
        }
        
        JSONArray desc = new JSONArray();
        members.forEach(m->desc.add(tojson(m)));
        JSONObject args_rsp = new JSONObject();
        args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
        args_rsp.put("desc", desc);
        response.attr().put("Content-Type", "application/json");
        response.content(args_rsp);
    }
    
    private void processQueryChatroomMessage(FjHttpResponse response, FjHttpRequest request) {
        JSONObject args = request.argsToJson();
        int user = Integer.parseInt(request.cookie().get("user"), 16);
        
        if (!args.has("crid")) {
            logger.error("illegal arguments for query chatroom message: " + args);
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS);
            args_rsp.put("desc", "非法参数");
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
            return;
        }
        
        int     crid    = getIntFromArgs(args, "crid");
        int     member  = CommonService.getPlatformAccountByCaid(user);
        boolean ismember = false;
        
        for (BeanChatroomMember crm : CommonService.getChatroomMemberByCrid(crid)) {
            if (crm.i_member == member) {
                ismember = true;
                break;
            }
        }
        if (!ismember) {
            logger.error("non-member can not query chatroom message: " + args);
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_USER_NO_PRIVILEGE);
            args_rsp.put("desc", "非聊天室成员不能查看消息");
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
            return;
        }
        
        if (args.has("mid")) {
            BeanChatroomMessage message = CommonService.getChatroomMessageByCridMid(crid, getIntFromArgs(args, "mid"));
            byte[] data = Base64.getDecoder().decode(message.c_message);
            
            switch (message.i_type) {
            case CommonService.CHATROOM_MESSAGE_TEXT:
                response.attr().put("Content-Type", "text/plain");
                response.attr().put("Content-Disposition", String.format("attachment; filename=\"%x_%x.txt\"", message.i_crid, message.i_mid));
                response.content(data);
                break;
            case CommonService.CHATROOM_MESSAGE_IMAGE:
                response.attr().put("Content-Type", "image/png");
                response.attr().put("Content-Disposition", String.format("attachment; filename=\"%x_%x.png\"", message.i_crid, message.i_mid));
                response.content(data);
                break;
            case CommonService.CHATROOM_MESSAGE_VOICE:
                response.attr().put("Content-Type", "audio/wav");
                response.attr().put("Content-Disposition", String.format("attachment; filename=\"%x_%x.wav\"", message.i_crid, message.i_mid));
                response.content(data);
                break;
            default:
                response.attr().put("Content-Type", "text/plain");
                response.attr().put("Content-Disposition", String.format("attachment; filename=\"%x_%x.txt\"", message.i_crid, message.i_mid));
                response.content(data);
                break;
            }
        } else {
            List<BeanChatroomMessage> messages = null;
            if (args.has("count") && args.has("time")) {
                messages = CommonService.getChatroomMessageByCrid(crid,
                        getIntFromArgs(args, "count"),
                        args.getString("time"));
            } else if (args.has("count")) {
                messages = CommonService.getChatroomMessageByCrid(crid,
                        getIntFromArgs(args, "count"));
            } else if (args.has("time")) {
                messages = CommonService.getChatroomMessageByCrid(crid,
                        args.getString("time"));
            } else {
                logger.error("illegal arguments for query chatroom message: " + args);
                JSONObject args_rsp = new JSONObject();
                args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS);
                args_rsp.put("desc", "非法参数");
                response.attr().put("Content-Type", "application/json");
                response.content(args_rsp);
                return;
            }
            
            JSONArray desc = new JSONArray();
            messages.forEach(m->desc.add(tojson(m)));
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
            args_rsp.put("desc", desc);
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
        }
    }
    
    private void processQueryGame(FjHttpResponse response, FjHttpRequest request) {
        JSONObject args = request.argsToJson();
        
        if (args.has("gid")) {
            int gid = getIntFromArgs(args, "gid");
            JSONObject desc = tojson(CommonService.getGameByGid(gid));
            if (request.cookie().containsKey("user")) {
                int user = Integer.parseInt(request.cookie().get("user"), 16);
                desc.put("ccs", tojson_ccs(user, gid, Integer.parseInt(FjServerToolkit.getServerConfig("web.cc.max"))));
            } else {
                desc.put("ccs", new JSONArray());
            }
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
            args_rsp.put("desc", desc);
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
        } else if (args.has("category")) {
            String[] categorys = args.getString("category").split("_");
            JSONArray desc = new JSONArray();
            CommonService.getGameByCategory(categorys).forEach(game->desc.add(tojson(game)));
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
            args_rsp.put("desc", desc);
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
        } else if (args.has("language")) {
            String language = args.getString("language");
            JSONArray desc = new JSONArray();
            CommonService.getGameByLanguage(language).forEach(game->desc.add(tojson(game)));
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
            args_rsp.put("desc", desc);
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
        } else if (args.has("tag")) {
            String tag = args.getString("tag");
            JSONArray desc = new JSONArray();
            CommonService.getGameByTag(tag).forEach(game->desc.add(tojson(game)));
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
            args_rsp.put("desc", desc);
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
        } else if (args.has("word")) {
            String word = args.getString("word");
            JSONArray desc = new JSONArray();
            CommonService.getGameAll().values()
                    .stream()
                    .filter(game->game.getDisplayName().toLowerCase().contains(word.toLowerCase()))
                    .forEach(game->desc.add(tojson(game)));
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
            args_rsp.put("desc", desc);
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
        } else if (args.has("vendor")) {
            String vendor = args.getString("vendor").replace("_", " ");
            JSONArray desc = new JSONArray();
            CommonService.getGameByVendor(vendor).forEach(game->desc.add(tojson(game)));
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
            args_rsp.put("desc", desc);
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
        } else {
            logger.error("illegal arguments for query game: " + args);
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS);
            args_rsp.put("desc", "非法参数");
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
        }
    }
    
    private void processQueryOrder(FjHttpResponse response, FjHttpRequest request) {
        JSONObject args = request.argsToJson();
        int user = Integer.parseInt(request.cookie().get("user"), 16);
        
        if (args.has("oid") && args.has("csn")) {
            int oid = getIntFromArgs(args, "oid");
            int csn = getIntFromArgs(args, "csn");
            BeanCommodity c = CommonService.getOrderByOid(oid).commodities.get(csn);
            
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
            args_rsp.put("desc", tojson(c));
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
        } else {
            JSONArray desc = new JSONArray();
            CommonService.getOrderByPaid(CommonService.getPlatformAccountByCaid(user)).forEach(o->{
                        o.commodities.values().forEach(c->desc.add(tojson(c)));
                    });
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
            args_rsp.put("desc", desc);
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
        }
    }
    
    private void processQueryPlatformAccount(FjHttpResponse response, FjHttpRequest request) {
        JSONObject args = request.argsToJson();
        if (args.has("caid")) {
            int caid = getIntFromArgs(args, "caid");
            BeanPlatformAccount bean = CommonService.getPlatformAccountByPaid(CommonService.getPlatformAccountByCaid(caid));
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
            args_rsp.put("desc", tojson(bean));
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
        } else {
            int caid = Integer.parseInt(request.cookie().get("user"), 16);
            BeanPlatformAccount bean = CommonService.getPlatformAccountByPaid(CommonService.getPlatformAccountByCaid(caid));
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
            args_rsp.put("desc", tojson(bean));
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
        }
    }
    
    private void processQueryPlatformAccountMap(FjHttpResponse response, FjHttpRequest request) {
        int user = Integer.parseInt(request.cookie().get("user"), 16);
        
        JSONObject args = new JSONObject();
        JSONArray desc = new JSONArray();
        CommonService.getChannelAccountRelatedAll(user).forEach(bean->{
            desc.add(tojson(bean));
        });
        args.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
        args.put("desc", desc);
        response.attr().put("Content-Type", "application/json");
        response.content(args);
    }
    
    private void processUpdateChatroom(FjHttpResponse response, FjHttpRequest request) {
        JSONObject args = request.argsToJson();
//        int user = Integer.parseInt(request.cookie().get("user"), 16);
        
        if (args.has("gid")) {
            int gid = getIntFromArgs(args, "gid");
            List<BeanChatroom> chatrooms = CommonService.getChatroomByGid(gid);
            if (null != chatrooms && !chatrooms.isEmpty()) {
                logger.info("chatroom already exist: gid = 0x" + Integer.toHexString(gid));
                JSONObject args_rsp = new JSONObject();
                args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
                args_rsp.put("desc", "聊天室已存在");
                response.attr().put("Content-Type", "application/json");
                response.content(args_rsp);
                return;
            }
            
            // update chatroom
            BeanGame game = CommonService.getGameByGid(gid);
            int crid = -1;
            JSONObject args_cdb = new JSONObject();
            args_cdb.put("name", game.c_name_zh_cn);
            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_CHATROOM, args_cdb);
            if (!CommonService.isResponseSuccess(rsp)) {
                logger.error("update chatroom failed: " + rsp);
                JSONObject args_rsp = new JSONObject();
                args_rsp.put("code", CommonService.getResponseCode(rsp));
                args_rsp.put("desc", "创建聊天室失败");
                response.attr().put("Content-Type", "application/json");
                response.content(args_rsp);
                return;
            }
            crid = Integer.parseInt(CommonService.getResponseDesc(rsp), 16);
            CommonService.updateChatroom();
            
            // update tag
            args_cdb.clear();
            args_cdb.put("type",        CommonService.TAG_CHATROOM);
            args_cdb.put("instance",    crid);
            args_cdb.put("tag",         Integer.toHexString(gid));
            rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_TAG, args_cdb);
            if (!CommonService.isResponseSuccess(rsp)) {
                logger.error("update tag failed: " + rsp);
                JSONObject args_rsp = new JSONObject();
                args_rsp.put("code", CommonService.getResponseCode(rsp));
                args_rsp.put("desc", "创建聊天室失败");
                response.attr().put("Content-Type", "application/json");
                response.content(args_rsp);
                return;
            }
            CommonService.updateTag();
            
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
            args_rsp.put("desc", null);
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
        }
    }
    
    private void processUpdateChatroomMember(FjHttpResponse response, FjHttpRequest request) {
        JSONObject args = request.argsToJson();
        int user = Integer.parseInt(request.cookie().get("user"), 16);
        
        if (!args.has("crid")) {
            logger.error("illegal arguments for update chatroom member: " + args);
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS);
            args_rsp.put("desc", "非法参数");
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
            return;
        }
        int crid = getIntFromArgs(args, "crid");
        int member = CommonService.getPlatformAccountByCaid(user);
        for (BeanChatroomMember crm : CommonService.getChatroomMemberByCrid(crid)) {
            if (crm.i_member == member) {
                JSONObject args_rsp = new JSONObject();
                args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
                args_rsp.put("desc", "已经是聊天室成员");
                response.attr().put("Content-Type", "application/json");
                response.content(args_rsp);
                return;
            }
        }
        
        JSONObject args_cdb = new JSONObject();
        args_cdb.put("crid",    crid);
        args_cdb.put("member",  member);
        FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_CHATROOM_MEMBER, args_cdb);
        if (!CommonService.isResponseSuccess(rsp)) {
            logger.error("update chatroom member failed: " + rsp);
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonService.getResponseCode(rsp));
            args_rsp.put("desc", "加入聊天室失败");
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
            return;
        }
        CommonService.updateChatroomMember();
        
        JSONObject args_rsp = new JSONObject();
        args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
        args_rsp.put("desc", null);
        response.attr().put("Content-Type", "application/json");
        response.content(args_rsp);
    }
    
    private void processUpdateChatroomMessage(FjHttpResponse response, FjHttpRequest request) {
        JSONObject args = request.argsToJson();
        int user = Integer.parseInt(request.cookie().get("user"), 16);
        
        if (!args.has("crid") || !args.has("type") || !args.has("message")) {
            logger.error("illegal arguments for update chatroom message: " + args);
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS);
            args_rsp.put("desc", "非法参数");
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
            return;
        }
        
        int     crid    = getIntFromArgs(args, "crid");
        int     member  = CommonService.getPlatformAccountByCaid(user);
        int     type    = getIntFromArgs(args, "type");
        String  message = args.getString("message");
        boolean ismember = false;
        
        for (BeanChatroomMember crm : CommonService.getChatroomMemberByCrid(crid)) {
            if (crm.i_member == member) {
                ismember = true;
                break;
            }
        }
        if (!ismember) {
            logger.error("non-member can not update chatroom message: " + args);
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_USER_NO_PRIVILEGE);
            args_rsp.put("desc", "非聊天室成员不能发送消息");
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
            return;
        }
        
        switch (type) {
        case CommonService.CHATROOM_MESSAGE_IMAGE: {
            byte[] image = WechatInterface.media(wechat.token_monitor().token(), message);
            JSONObject args_mma = new JSONObject();
            args_mma.put("type", "image");
            args_mma.put("data", Base64.getEncoder().encodeToString(image));
            FjDscpMessage rsp_mma = CommonService.send("mma", CommonDefinition.ISIS.INST_ECOM_APPLY_ENCODE, args_mma);
            message = CommonService.getResponseDesc(rsp_mma);
            break;
        }
        case CommonService.CHATROOM_MESSAGE_VOICE: {
            byte[] audio = WechatInterface.media(wechat.token_monitor().token(), message);
            JSONObject args_mma = new JSONObject();
            args_mma.put("type", "audio");
            args_mma.put("data", Base64.getEncoder().encodeToString(audio));
            FjDscpMessage rsp_mma = CommonService.send("mma", CommonDefinition.ISIS.INST_ECOM_APPLY_ENCODE, args_mma);
            message = CommonService.getResponseDesc(rsp_mma);
            break;
        }
        }
        
        JSONObject args_mma = new JSONObject();
        args_mma.put("crid",    crid);
        args_mma.put("member",  member);
        args_mma.put("type",    type);
        args_mma.put("message", message);
        
        FjDscpMessage req = new FjDscpMessage();
        req.json().put("fs",   FjServerToolkit.getAnyServer().name());
        req.json().put("ts",   "mma");
        req.json().put("inst", CommonDefinition.ISIS.INST_ECOM_UPDATE_CHATROOM_MESSAGE);
        req.json().put("args", args_mma);
        FjServerToolkit.getAnySender().send(req);
        
        JSONObject args_rsp = new JSONObject();
        args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
        args_rsp.put("desc", null);
        response.attr().put("Content-Type", "application/json");
        response.content(args_rsp);
    }
    
    private Map<Integer, String> cache_verify_code = new HashMap<Integer, String>();
    
    private void processUpdatePlatformAccountMap(FjHttpResponse response, FjHttpRequest request) {
        JSONObject args = request.argsToJson();
        int user = Integer.parseInt(request.cookie().get("user"), 16);
        
        if (args.has("phone") && !args.has("verify")) {
            String phone    = args.getString("phone");
            String time     = String.valueOf(System.currentTimeMillis());
            String verify   = time.substring(time.length() - 4);
            {
                JSONObject args_mma = new JSONObject();
                args_mma.put("user",    phone);
                args_mma.put("content", verify);
                FjDscpMessage rsp = CommonService.send("mma", CommonDefinition.ISIS.INST_ECOM_APPLY_AUTHORIZE, args_mma);
                if (!CommonService.isResponseSuccess(rsp)) {
                    logger.error("send verify code failed: " + rsp);
                    JSONObject args_rsp = new JSONObject();
                    args_rsp.put("code", CommonDefinition.CODE.CODE_USER_AUTHORIZE_FAILED);
                    args_rsp.put("desc", "发送失败，请稍候重试");
                    response.attr().put("Content-Type", "application/json");
                    response.content(args_rsp);
                    return;
                }
            }
            
            cache_verify_code.put(user, verify);
            
            {
                JSONObject args_rsp = new JSONObject();
                args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
                args_rsp.put("desc", null);
                response.attr().put("Content-Type", "application/json");
                response.content(args_rsp);
            }
            
            return;
        }
        
        if (!(args.has("psn_user")
                || (args.has("phone") && args.has("verify")))) {
            logger.error("illegal arguments for update platform account map: " + args);
            JSONObject args_rsp = new JSONObject();
            args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_ILLEGAL_ARGS);
            args_rsp.put("desc", "非法参数");
            response.attr().put("Content-Type", "application/json");
            response.content(args_rsp);
            return;
        }
        
        // 验证手机
        if (args.has("phone") && args.has("verify")) {
            String  phone    = args.getString("phone");
            String  verify   = args.getString("verify");
            if (!cache_verify_code.containsKey(user)
                    || !verify.equals(cache_verify_code.get(user))) {
                JSONObject args_rsp = new JSONObject();
                args_rsp.put("code", CommonDefinition.CODE.CODE_USER_AUTHORIZE_FAILED);
                args_rsp.put("desc", "校验失败");
                response.attr().put("Content-Type", "application/json");
                response.content(args_rsp);
                return;
            }
            cache_verify_code.remove(user);
            
            { // 更新手机号
                JSONObject args_cdb = new JSONObject();
                args_cdb.put("caid",  user);
                args_cdb.put("phone", phone);
                FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_CHANNEL_ACCOUNT, args_cdb);
                if (!CommonService.isResponseSuccess(rsp)) {
                    logger.error("update channel account failed: " + rsp);
                    JSONObject args_rsp = new JSONObject();
                    args_rsp.put("code", CommonDefinition.CODE.CODE_USER_AUTHORIZE_FAILED);
                    args_rsp.put("desc", "更新手机失败，请稍候重试");
                    response.attr().put("Content-Type", "application/json");
                    response.content(args_rsp);
                    return;
                }
            }
            
            List<BeanChannelAccount> users_taobao = CommonService.getChannelAccountByPhoneNChannel(phone, CommonService.CHANNEL_TAOBAO);
            if (1 == users_taobao.size()) { // 尝试关联
                BeanChannelAccount user_taobao = users_taobao.get(0);
                if (CommonService.getChannelAccountRelatedByCaidNChannel(user_taobao.i_caid, CommonService.CHANNEL_WECHAT).isEmpty()) { // 淘宝用户尚未被关联
                    JSONObject args_cdb = new JSONObject();
                    args_cdb.put("paid_from",   CommonService.getPlatformAccountByCaid(user));
                    args_cdb.put("paid_to",     CommonService.getPlatformAccountByCaid(user_taobao.i_caid));
                    FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE, args_cdb);
                    if (!CommonService.isResponseSuccess(rsp)) {
                        logger.error("apply platform account merge failed: " + rsp);
                        JSONObject args_rsp = new JSONObject();
                        args_rsp.put("code", CommonService.getResponseCode(rsp));
                        args_rsp.put("desc", "关联淘宝用户失败，请稍候重试");
                        response.attr().put("Content-Type", "application/json");
                        response.content(args_rsp);
                        return;
                    }
                }
            }
        }
        
        // 验证PSN帐号
        if (args.has("psn_user")) {
            String psn_user = args.getString("psn_user");
            List<BeanChannelAccount> users_psn = CommonService.getChannelAccountRelatedByCaidNChannel(user, CommonService.CHANNEL_PSN);
            // 已有关联帐号
            if (null != users_psn && !users_psn.isEmpty()) {
                // 只更新第一个
                BeanChannelAccount user_psn = users_psn.get(0);
                JSONObject args_cdb = new JSONObject();
                args_cdb.put("caid", user_psn.i_caid);
                args_cdb.put("user", psn_user);
                FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_CHANNEL_ACCOUNT, args_cdb);
                if (!CommonService.isResponseSuccess(rsp)) {
                    logger.error("update channel account failed: " + rsp);
                    JSONObject args_rsp = new JSONObject();
                    args_rsp.put("code", CommonService.getResponseCode(rsp));
                    args_rsp.put("desc", "更新PSN帐号失败，请稍后重试");
                    response.attr().put("Content-Type", "application/json");
                    response.content(args_rsp);
                    return;
                }
            } else {
                // 新建PSN帐号
                JSONObject args_cdb = new JSONObject();
                args_cdb.put("channel", CommonService.CHANNEL_PSN);
                args_cdb.put("user", psn_user);
                FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_CHANNEL_ACCOUNT, args_cdb);
                if (!CommonService.isResponseSuccess(rsp)) {
                    logger.error("update channel account failed: " + rsp);
                    JSONObject args_rsp = new JSONObject();
                    args_rsp.put("code", CommonService.getResponseCode(rsp));
                    args_rsp.put("desc", "更新PSN帐号失败，请稍后重试");
                    response.attr().put("Content-Type", "application/json");
                    response.content(args_rsp);
                    return;
                }
                CommonService.updatePlatformAccount();
                CommonService.updatePlatformAccountMap();
                // 关联新PSN帐号
                int caid_psn = Integer.parseInt(CommonService.getResponseDesc(rsp), 16);
                args_cdb.put("paid_from",   CommonService.getPlatformAccountByCaid(caid_psn));
                args_cdb.put("paid_to",     CommonService.getPlatformAccountByCaid(user));
                rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE, args_cdb);
                if (!CommonService.isResponseSuccess(rsp)) {
                    logger.error("apply platform account merge failed: " + rsp);
                    JSONObject args_rsp = new JSONObject();
                    args_rsp.put("code", CommonService.getResponseCode(rsp));
                    args_rsp.put("desc", "关联PSN用户失败，请稍候重试");
                    response.attr().put("Content-Type", "application/json");
                    response.content(args_rsp);
                    return;
                }
            }
        }
        
        JSONObject args_rsp = new JSONObject();
        args_rsp.put("code", CommonDefinition.CODE.CODE_SYS_SUCCESS);
        args_rsp.put("desc", null);
        response.attr().put("Content-Type", "application/json");
        response.content(args_rsp);
    }
    
    private static int getIntFromArgs(JSONObject args, String name) {
        if (!args.has(name)) return -1;
        
        Object obj = args.get(name);
        if (obj instanceof Integer) return (int) obj;
        else return Integer.parseInt(obj.toString(), 16);
    }
    
    private static JSONObject tojson(BeanPlatformAccount bean) {
        JSONObject json = new JSONObject();
        json.put("paid",    bean.i_paid);
        json.put("user",    bean.c_user);
        json.put("pass",    bean.c_pass);
        json.put("name",    bean.c_name);
        json.put("mobile",  bean.c_mobile);
        json.put("email",   bean.c_email);
        json.put("birth",   bean.t_birth);
        json.put("cash",    bean.i_cash);
        json.put("coupon",  bean.i_coupon);
        json.put("create",  bean.t_create);
        json.put("url_cover", bean.c_url_cover);
        
        float[] prestatement = CommonService.prestatementByPaid(bean.i_paid);
        json.put("cash_rt",     prestatement[0]);
        json.put("coupon_rt",   prestatement[1]);
        
        return json;
    }
    
    private static JSONObject tojson(BeanGame game) {
        JSONObject json = new JSONObject();
        json.put("gid",             game.i_gid);
        json.put("name_zh_cn",      game.c_name_zh_cn);
        json.put("name_zh_hk",      game.c_name_zh_hk);
        json.put("name_en",         game.c_name_en);
        json.put("name_ja",         game.c_name_ja);
        json.put("name_ko",         game.c_name_ko);
        json.put("name_other",      game.c_name_other);
        json.put("platform",        game.c_platform);
        json.put("category",        game.c_category);
        json.put("language",        game.c_language);
        json.put("size",            game.c_size);
        json.put("vendor",          game.c_vendor);
        json.put("sale",            game.t_sale);
        json.put("url_icon",        game.c_url_icon);
        json.put("url_cover",       game.c_url_cover);
        json.put("url_poster",      JSONArray.fromObject(game.c_url_poster.split(" ")));
        json.put("introduction",    game.c_introduction);
        json.put("version",         game.c_version);
        json.put("vedio",           game.c_vedio);
        
        json.put("display_name",    game.getDisplayName());
        BeanGameRentPrice rent_price_a = CommonService.getGameRentPriceByGid(game.i_gid, CommonService.RENT_TYPE_A);
        json.put("rent_price_a",    null != rent_price_a ? rent_price_a.i_price : 0.0f);
        BeanGameRentPrice rent_price_b = CommonService.getGameRentPriceByGid(game.i_gid, CommonService.RENT_TYPE_B);
        json.put("rent_price_b",    null != rent_price_b ? rent_price_b.i_price : 0.0f);
        
        json.put("rent_avail_a",    CommonService.getGameAccountByGidNRentState(game.i_gid, CommonService.RENT_STATE_IDLE, CommonService.RENT_TYPE_A).size() > 0);
        json.put("rent_avail_b",    CommonService.getGameAccountByGidNRentState(game.i_gid, CommonService.RENT_STATE_IDLE, CommonService.RENT_TYPE_B).size() > 0);
        
        return json;
    }
    
    private static JSONObject tojson_ccs(int caid, int cid, int max) {
        BeanChannelAccount user = CommonService.getChannelAccountByCaid(caid);
        List<BeanChannelCommodity> ccs = CommonService.getChannelCommodityByCid(cid);
        List<BeanChannelCommodity> cc_conv = new LinkedList<BeanChannelCommodity>(ccs);
        List<BeanChannelCommodity> cc_near = new LinkedList<BeanChannelCommodity>(ccs);
        List<BeanChannelCommodity> cc_trus = new LinkedList<BeanChannelCommodity>(ccs);
        List<BeanChannelCommodity> cc_sold = new LinkedList<BeanChannelCommodity>(ccs);
        cc_conv.sort((cc1, cc2)->{
            float p1 = Float.parseFloat(cc1.c_item_price.split("-")[0].trim()) + cc1.i_express_price;
            float p2 = Float.parseFloat(cc2.c_item_price.split("-")[0].trim()) + cc2.i_express_price;
            return (int) Math.ceil(p1 - p2);
        });
        cc_near.sort((cc1, cc2)->{
            int e = 10000;
            double d1 = getDistance(user.c_address, cc1.c_shop_addr);
            double d2 = getDistance(user.c_address, cc2.c_shop_addr);
            return (int) Math.ceil(d1 * e - d2 * e);
        });
        cc_trus.sort((c1, c2)->{
            int t1 = Arrays.asList(c1.c_shop_rate.split("\\|")).stream().filter(r->0 < r.length()).map(r->r.split(" ")).map(r->(r[0].contains("cap") ? 10000 : r[0].contains("blue") ? 100 : 1) * Integer.parseInt(r[1])).reduce(0, (r1, r2)->{return r1 + r2;});
            int t2 = Arrays.asList(c2.c_shop_rate.split("\\|")).stream().filter(r->0 < r.length()).map(r->r.split(" ")).map(r->(r[0].contains("cap") ? 10000 : r[0].contains("blue") ? 100 : 1) * Integer.parseInt(r[1])).reduce(0, (r1, r2)->{return r1 + r2;});
            return t2 - t1;
        });
        cc_sold.sort((c1, c2)->{
            return c2.i_item_sold - c1.i_item_sold;
        });
        
        if (max < ccs.size()) {
            cc_conv = cc_conv.subList(0, max);
            cc_near = cc_near.subList(0, max);
            cc_trus = cc_trus.subList(0, max);
            cc_sold = cc_sold.subList(0, max);
        }
        JSONObject json = new JSONObject();
        json.put("total", ccs.size());
        json.put("conv", JSONArray.fromObject(cc_conv.stream().map(cc->tojson(cc)).collect(Collectors.toList())));
        json.put("near", JSONArray.fromObject(cc_near.stream().map(cc->tojson(cc)).collect(Collectors.toList())));
        json.put("trus", JSONArray.fromObject(cc_trus.stream().map(cc->tojson(cc)).collect(Collectors.toList())));
        json.put("sold", JSONArray.fromObject(cc_sold.stream().map(cc->tojson(cc)).collect(Collectors.toList())));
        return json;
    }
    
    private static double getDistance(String place1, String place2) {
        Point2D.Double p1 = getCordinate(place1);
        Point2D.Double p2 = getCordinate(place2);
        return Math.sqrt((p1.getX() - p2.getX()) * (p1.getX() - p2.getX()) + (p1.getY() - p2.getY()) * (p1.getY() - p2.getY()));
    }
    
    private static Map<String, Point2D.Double> cache_cordinate = new ConcurrentHashMap<String, Point2D.Double>();
    
    private static Point2D.Double getCordinate(String place) {
        if (cache_cordinate.containsKey(place)) return cache_cordinate.get(place);
        else {
            Point2D.Double p = BaiduMapInterface.getCordinate(FjServerToolkit.getServerConfig("web.baidu.map.ak"), place);
            if (null != p) {
                cache_cordinate.put(place, p);
                return p;
            }
            return new Point2D.Double(0, 0);
        }
    }
    
    private static JSONObject tojson(BeanChannelCommodity bean) {
        JSONObject json = new JSONObject();
        json.put("time",            bean.t_time);
        json.put("channel",         bean.i_channel);
        json.put("item_url",        bean.c_item_url);
        json.put("item_cover",      bean.c_item_cover);
        json.put("item_name",       bean.c_item_name);
        json.put("item_remark",     bean.c_item_remark);
        json.put("item_sold",       bean.i_item_sold);
        json.put("item_price",      bean.c_item_price);
        json.put("express_price",   bean.i_express_price);
        json.put("shop_url",        bean.c_shop_url);
        json.put("shop_name",       bean.c_shop_name);
        json.put("shop_owner",      bean.c_shop_owner);
        json.put("shop_rate",       bean.c_shop_rate);
        json.put("shop_score",      bean.c_shop_score);
        json.put("shop_addr",       bean.c_shop_addr);
        return json;
    }
    
    private static JSONObject tojson(BeanCommodity bean) {
        JSONObject json = new JSONObject();
        json.put("oid",      bean.i_oid);
        json.put("csn",      bean.i_csn);
        json.put("count",    bean.i_count);
        json.put("price",    bean.i_price);
        json.put("begin",    bean.t_begin);
        json.put("end",      bean.t_end);
        json.put("expense",  bean.i_expense);
        json.put("remark",   bean.c_remark);
        json.put("arg0",     bean.c_arg0);
        json.put("arg1",     bean.c_arg1);
        json.put("arg2",     bean.c_arg2);
        json.put("arg3",     bean.c_arg3);
        json.put("arg4",     bean.c_arg4);
        json.put("arg5",     bean.c_arg5);
        json.put("arg6",     bean.c_arg6);
        json.put("arg7",     bean.c_arg7);
        json.put("arg8",     bean.c_arg8);
        json.put("arg9",     bean.c_arg9);
        
        BeanGameAccount account = CommonService.getGameAccountByGaid(Integer.parseInt(bean.c_arg0, 16));
        json.put("account",  account.c_user);
        json.put("type",     "A".equals(bean.c_arg1) ? "认证" : "B".equals(bean.c_arg1) ? "不认证" : "未知");
        json.put("game",     tojson(CommonService.getGameByGaid(account.i_gaid).get(0)));
        // 预结算
        if (!bean.isClose()) {
            json.put("expense", CommonService.prestatementByCommodity(bean));
            json.put("pass", account.c_pass);
        }
        
        return json;
    }
    
    private static JSONObject tojson(BeanChannelAccount bean) {
        JSONObject json = new JSONObject();
        json.put("caid",     bean.i_caid);
        json.put("channel",  bean.i_channel);
        json.put("user",     bean.c_user);
        json.put("name",     bean.c_name);
        json.put("phone",    bean.c_phone);
        json.put("gender",   bean.i_gender);
        json.put("birth",    bean.t_birth);
        json.put("address",  bean.c_address);
        json.put("zipcode",  bean.c_zipcode);
        json.put("create",   bean.t_create);
        json.put("url_cover", bean.c_url_cover);
        
        json.put("display_name", bean.getDisplayName());
        return json;
    }
    
    private static JSONObject tojson(BeanChatroom bean) {
        JSONObject json = new JSONObject();
        json.put("crid",    bean.i_crid);
        json.put("name",    bean.c_name);
        json.put("create",  bean.t_create);
        BeanGame game = CommonService.getGameByGid(Integer.parseInt(CommonService.getTagByTypeInstance(CommonService.TAG_CHATROOM, bean.i_crid).get(0).c_tag, 16));
        json.put("game",    tojson(game));
        return json;
    }
    
    private static JSONObject tojson(BeanChatroomMember bean) {
        JSONObject json = new JSONObject();
        json.put("crid",    bean.i_crid);
        json.put("member",  bean.i_member);
        
        json.put("pa",      tojson(CommonService.getPlatformAccountByPaid(bean.i_member)));
        return json;
    }
    
    private static JSONObject tojson(BeanChatroomMessage bean) {
        JSONObject json = new JSONObject();
        json.put("crid",    bean.i_crid);
        json.put("mid",     bean.i_mid);
        json.put("member",  bean.i_member);
        json.put("type",    bean.i_type);
        json.put("time",    bean.t_time);
        json.put("message", bean.c_message);
        json.put("arg0",    bean.c_arg0);
        json.put("arg1",    bean.c_arg1);
        json.put("arg2",    bean.c_arg2);
        json.put("arg3",    bean.c_arg3);
        json.put("arg4",    bean.c_arg4);
        List<BeanChannelAccount> users = CommonService.getChannelAccountByPaidNChannel(bean.i_member, CommonService.CHANNEL_WECHAT);
        if (null != users && !users.isEmpty()) {
            BeanChannelAccount user = users.get(0);
            json.put("mi", tojson(user));
        }
        return json;
    }
    
}
