package com.ski.wca.biz;

import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.ski.common.CommonDefinition;
import com.ski.common.CommonReport;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanCommodity;
import com.ski.common.bean.BeanGameAccount;
import com.ski.common.bean.BeanPlatformAccount;
import com.ski.wca.WechatInterface;

import fomjar.server.FjMessageWrapper;
import fomjar.server.FjServerToolkit;
import fomjar.server.FjServerToolkit.FjAddress;
import fomjar.server.msg.FjHttpRequest;
import fomjar.server.msg.FjJsonMessage;

public class WcWeb {
    
    private static final Logger logger = Logger.getLogger(WcWeb.class);
    public static final String URL_KEY = "/ski-wcweb";
    
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
    
    public static void dispatch(FjMessageWrapper wrapper) {
        FjJsonMessage req = ((FjHttpRequest) wrapper.message()).toJsonMessage((SocketChannel) wrapper.attachment("conn"));
        if (!req.json().has("inst") || !req.json().has("user")) {
            logger.error("illegal wechat web message: " + req);
            return;
        }
        
        int inst = Integer.parseInt(req.json().getString("inst"), 16);
        int user = Integer.parseInt(req.json().getString("user"), 16);
        String  report = null;
        switch(inst) {
        case CommonDefinition.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT:
            report = createReportQueryPlatformAccount(user);
            break;
        }
        WechatInterface.sendResponse(report, (SocketChannel) wrapper.attachment("conn"));
    }
    
    private static String createReportQueryPlatformAccount(int user) {
        BeanPlatformAccount puser = CommonService.getPlatformAccountByPaid(CommonService.getPlatformAccountByCaid(user));
        
        StringBuilder sb = new StringBuilder();
        sb.append(CommonReport.createReportHead("我的账户"));
        {
            List<Object> data = new LinkedList<Object>();
            float[] prestatement = CommonService.prestatement(user);
            data.add(new String[] {"账户余额",          puser.i_balance + "元"});
            data.add(new String[] {"优惠券余额",         puser.i_coupon + "元"});
            data.add(new String[] {"账户余额(实时)",      prestatement[0] + "元"});
            data.add(new String[] {"优惠券余额(实时)",     prestatement[1] + "元"});
            sb.append(CommonReport.createReportTable("我的账户", data, 2));
        }
        {
            List<Object> data = new LinkedList<Object>();
            List<BeanCommodity> commodities = new LinkedList<BeanCommodity>();
            CommonService.getOrderByCaid(user)
                    .stream()
                    .filter(order->!order.isClose())
                    .forEach(order->{
                        commodities.addAll(order.commodities.values()
                                .stream()
                                .filter(commodity->!commodity.isClose())
                                .collect(Collectors.toList()));
                    });
            data.add(new String[] {"账号", "密码", "租赁类型", "包含游戏", "单价", "起租时间", "备注"});
            if (commodities.isEmpty()) {
                data.add("没有在租账号");
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                commodities
                        .stream()
                        .sorted((c1, c2)->{
                            try {return (int) (sdf.parse(c1.t_begin).getTime() - sdf.parse(c2.t_begin).getTime());}
                            catch (ParseException e) {e.printStackTrace();}
                            return 0;
                        })
                        .forEach(c->{
                            BeanGameAccount account = CommonService.getGameAccountByGaid(Integer.parseInt(c.c_arg0, 16));
                            data.add(new String[] {
                                    account.c_user,
                                    account.c_pass_curr,
                                    c.c_arg1,
                                    CommonService.getGameByGaid(account.i_gaid).stream().map(game->game.c_name_zh).collect(Collectors.joining("; ")),
                                    c.i_price + "元/天",
                                    c.t_begin,
                                    0 == c.c_remark.length() ? "-" : c.c_remark});
                        });
            }
            sb.append(CommonReport.createReportTable("我的游戏", data, 7));
        }
        return sb.toString();
    }

}
