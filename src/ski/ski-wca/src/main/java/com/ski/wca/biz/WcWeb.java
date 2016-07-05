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
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjJsonMessage;

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
        FjJsonMessage jreq = req.toJsonMessage(conn);
        
        String  form = null;
        if (jreq.json().has("inst") && jreq.json().has("user")) {
            int inst = Integer.parseInt(jreq.json().getString("inst"), 16);
            int user = Integer.parseInt(jreq.json().getString("user"), 16);
            logger.info(String.format("web user request: 0x%08X:0x%08X", user, inst));
            switch(inst) {
            case CommonDefinition.ISIS.INST_ECOM_UPDATE_PLATFORM_ACCOUNT_MAP:
                form = processUpdatePlatformAccountMap(user);
                break;
            case CommonDefinition.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MERGE: {
                String to_user  = jreq.json().getString("to_user");
                String to_phone = jreq.json().getString("to_phone");
                form = processUpdatePlatformAccountMerge(user, to_user, to_phone);
                break;
            }
            case CommonDefinition.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT:
                form = processQueryPlatformAccount(user);
                break;
            case CommonDefinition.CODE.CODE_SYS_SUCCESS: {
                form = WechatForm.createFormMessage(WechatForm.MESSAGE_SUCCESS, "操作成功", null, null, null);
                break;
            }
            }
        } else {
            form = fetchFile(req.url());
        }
        WechatInterface.sendResponse(form, conn);
    }
    
    private static String processUpdatePlatformAccountMap(int user) {
        if (1 < CommonService.getChannelAccountRelated(user).size())    // 用户已经绑定过了，不能再绑定
            return WechatForm.createFormMessage(WechatForm.MESSAGE_WARN, "错误", "您已经关联过其他账号，请不要重复关联", null, null);
        
        return String.format(fetchFile("/update_platform_account_map.html"), user);
    }
    
    private static String processUpdatePlatformAccountMerge(int user, String to_user, String to_phone) {
        List<BeanChannelAccount> users = CommonService.getChannelAccountByUser(to_user);
        if (users.isEmpty()) return "我们没有见过这个淘宝用户";
        return null;
    }
    
    private static String processQueryPlatformAccount(int user) {
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
        return sb.toString();
    }
    
    private static String fetchFile(String url) {
        File file = new File(ROOT + (url.startsWith(URL_KEY) ? url.substring(URL_KEY.length()) : url));
        if (!file.isFile()) {
            logger.warn("not such file to fetch: " + file.getPath());
            return "";
        }
        
        FileInputStream         fis = null;
        ByteArrayOutputStream   baos = null;
        try {
            byte[]  buf = new byte[1024];
            int     len = -1;
            fis     = new FileInputStream(file);
            baos    = new ByteArrayOutputStream();
            while (0 < (len = fis.read(buf))) baos.write(buf, 0, len);
            return baos.toString("utf-8");
        } catch (IOException e) {logger.error("fetch file failed, url: " + url, e);}
        finally {
            try {
                fis.close();
                baos.close();
            } catch (IOException e) {e.printStackTrace();}
        }
        return "";
    }

}
