package com.ski.wca.biz;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.common.bean.BeanGameAccount;
import com.ski.common.bean.BeanPlatformAccount;
import com.ski.wca.WechatForm;
import com.ski.wca.WechatInterface;

import fomjar.server.FjServerToolkit;
import fomjar.server.FjServerToolkit.FjAddress;
import fomjar.server.msg.FjDscpMessage;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjJsonMessage;
import net.sf.json.JSONObject;

public class WcWeb {
    
    private static final Logger logger = Logger.getLogger(WcWeb.class);
    public static final String URL_KEY = "/ski-wcweb";
    private static final String ROOT = "conf/form";
    
    private WcWeb() {}
    
    public static String generateUrl(String server, int inst, int user) {
        FjAddress addr = FjServerToolkit.getSlb().getAddress(server);
        return String.format("http://%s%s%s?inst=%s&user=%s",
                addr.host,
                80 == addr.port ? "" : (":" + addr.port),
                URL_KEY,
                Integer.toHexString(inst),
                Integer.toHexString(user));
    }
    
    public static void dispatch(FjHttpRequest req, SocketChannel conn) {
        logger.info(String.format("user request url: %s", req.url()));
        
        FjJsonMessage jreq = req.toJsonMessage(conn);
        
        String[]  form = null;
        if (jreq.json().has("inst") && jreq.json().has("user")) {
            int inst = Integer.parseInt(jreq.json().getString("inst"), 16);
            int user = Integer.parseInt(jreq.json().getString("user"), 16);
            switch(inst) {
            case CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE:
                form = processApplyPlatformAccountMerge(user, jreq.json());
                break;
            case CommonDefinition.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT:
                form = processQueryPlatformAccount(user);
                break;
            case CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY:
                form = processApplyPlatformAccountMoney(user, jreq.json());
                break;
            }
        } else {
            form = fetchFile(req.url());
        }
        WechatInterface.sendResponse(form[0], form[1], conn);
    }
    
    private static String[] processApplyPlatformAccountMerge(int user, JSONObject args) {
        String step = args.has("step") ? args.getString("step") : "setup";
        String ct   = null;
        String form = null;
        if (1 < CommonService.getChannelAccountRelated(user).size()) {  // 用户已经绑定过了
            ct = FjHttpRequest.CT_HTML;
            form = WechatForm.createFormMessage(WechatForm.MESSAGE_SUCCESS, "关联成功", "您的微信和淘宝已成功关联，现在可以到“我的账户”中查看相关信息，感谢您的支持", null, null);
        } else {
            switch (step) {
            case "setup":
                ct = FjHttpRequest.CT_HTML;
                String[] file = fetchFile("/apply_platform_account_merge.html");
                ct      = file[0];
                form    = String.format(file[1], CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE, user);
                break;
            case "apply":
                ct = FjHttpRequest.CT_TEXT;
                String to_user  = args.getString("to_user");
                String to_phone = args.getString("to_phone");
                List<BeanChannelAccount> user_taobaos = CommonService.getChannelAccountByUser(to_user);
                if (user_taobaos.isEmpty()) {
                    form = "我们没有招待过此淘宝用户，请重新输入";
                    break;
                }
                
                BeanChannelAccount user_taobao = user_taobaos.get(0);
                if (CommonService.CHANNEL_TAOBAO != user_taobao.i_channel) {
                    form = "我们没有招待过此淘宝用户，请重新输入";
                    break;
                }
                if (!user_taobao.c_phone.equals(to_phone)) {
                    form = "填写的手机号跟淘宝上使用的手机不匹配，请重新输入";
                    break;
                }
                
                {
                    JSONObject args_cdb = new JSONObject();
                    args_cdb.put("paid_to",     CommonService.getPlatformAccountByCaid(user_taobao.i_caid));
                    args_cdb.put("paid_from",   CommonService.getPlatformAccountByCaid(user));
                    FjDscpMessage req_cdb = new FjDscpMessage();
                    req_cdb.json().put("fs", "wcweb");
                    req_cdb.json().put("ts", "cdb");
                    req_cdb.json().put("inst", CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE);
                    req_cdb.json().put("args", args_cdb);
                    FjServerToolkit.getAnySender().send(req_cdb);
                    form = "success";
                }
                break;
            case "success":
                ct = FjHttpRequest.CT_HTML;
                form = WechatForm.createFormMessage(WechatForm.MESSAGE_SUCCESS, "关联成功", "您的微信和淘宝已成功关联，现在可以到“我的账户”中查看相关信息，感谢您的支持", null, null);
                break;
            }
        }
        return new String[] {ct, form};
    }
    
    private static String[] processQueryPlatformAccount(int user) {
        BeanPlatformAccount puser = CommonService.getPlatformAccountByPaid(CommonService.getPlatformAccountByCaid(user));
        StringBuilder sb = new StringBuilder();
        sb.append(WechatForm.createFormHead(WechatForm.FORM_CELL, "账户明细"));
        {
            List<String[]> cells = new LinkedList<String[]>();
            cells.add(new String[] {"现金", puser.i_cash + "元"});
            cells.add(new String[] {"优惠券", puser.i_coupon + "元"});
            float[] prestatement = CommonService.prestatement(user);
            cells.add(new String[] {"现金(实时)", prestatement[0] + "元"});
            cells.add(new String[] {"优惠券(实时)", prestatement[1] + "元"});
            sb.append(WechatForm.createFormCellGroup("我的账户", cells, null));
        }
        {
            CommonService.getOrderByCaid(user)
                    .stream()
                    .filter(o->!o.isClose())
                    .forEach(o->{
                        o.commodities.values()
                                .stream()
                                .filter(c->!c.isClose())
                                .forEach(c->{
                                    BeanGameAccount account = CommonService.getGameAccountByGaid(Integer.parseInt(c.c_arg0, 16));
                                    String games = CommonService.getGameByGaid(account.i_gaid).stream().map(g->g.c_name_zh).collect(Collectors.joining("; "));
                                    {
                                        List<String[]> cells = new LinkedList<String[]>();
                                        cells.add(new String[] {"游戏账号", account.c_user});
                                        cells.add(new String[] {"当前密码", account.c_pass_curr});
                                        cells.add(new String[] {"租赁类型", "A".equals(c.c_arg1) ? "认证" : "B".equals(c.c_arg1) ? "不认证" : "未知"});
                                        cells.add(new String[] {"包含游戏", games});
                                        cells.add(new String[] {"租赁单价", c.i_price + "元/天"});
                                        cells.add(new String[] {"起租时间", c.t_begin});
                                        sb.append(WechatForm.createFormCellGroup("在租游戏：" + games, cells, null));
                                        cells.clear();
                                        cells.add(new String[] {"账号操作", "退租", "javascript:;"});
                                        sb.append(WechatForm.createFormCellAccessGroup(null, cells, null));
                                    }
                                });
                    });
        }
        sb.append(WechatForm.createFormFoot());
        return new String[] {FjHttpRequest.CT_HTML, sb.toString()};
    }
    
    private static String[] processApplyPlatformAccountMoney(int user, JSONObject args) {
//        BeanPlatformAccount puser = CommonService.getPlatformAccountByPaid(CommonService.getPlatformAccountByCaid(user));
        String step = args.has("step") ? args.getString("step") : "setup";
        String ct   = null;
        String form = null;
        switch (step) {
        case "setup":
            ct = FjHttpRequest.CT_HTML;
            String[] file = fetchFile("/apply_platform_account_money.html");
            ct      = file[0];
            form    = String.format(file[1], CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY, user);
            break;
        case "apply":
            break;
        }
        return new String[] {ct, form};
    }
    
    private static String[] fetchFile(String url) {
        File file = new File(ROOT + (url.startsWith(URL_KEY) ? url.substring(URL_KEY.length()) : url));
        if (!file.isFile()) {
            logger.warn("not such file to fetch: " + file.getPath());
            return new String[] {FjHttpRequest.CT_TEXT, ""};
        }
        
        FileInputStream         fis = null;
        ByteArrayOutputStream   baos = null;
        try {
            byte[]  buf = new byte[1024];
            int     len = -1;
            fis     = new FileInputStream(file);
            baos    = new ByteArrayOutputStream();
            while (0 < (len = fis.read(buf))) baos.write(buf, 0, len);
            return new String[] {getFileMime(file.getName()), baos.toString("utf-8")};
        } catch (IOException e) {logger.error("fetch file failed, url: " + url, e);}
        finally {
            try {
                fis.close();
                baos.close();
            } catch (IOException e) {e.printStackTrace();}
        }
        return new String[] {FjHttpRequest.CT_TEXT, ""};
    }
    
    private static String getFileMime (String name) {
        if (!name.contains(".")) return FjHttpRequest.CT_TEXT;
        
        String ext = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
        switch (ext) {
        case "html":
        case "htm":     return FjHttpRequest.CT_HTML;
        case "js":      return FjHttpRequest.CT_JS;
        case "css":
        case "less":    return FjHttpRequest.CT_CSS;
        case "xml":     return FjHttpRequest.CT_XML;
        case "json":    return FjHttpRequest.CT_JSON;
        default:    return FjHttpRequest.CT_TEXT;
        }
    }

}
