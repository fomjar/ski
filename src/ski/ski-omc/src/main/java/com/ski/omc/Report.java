package com.ski.omc;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.ski.common.CommonService;
import com.ski.common.CommonReport;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.common.bean.BeanCommodity;
import com.ski.common.bean.BeanGame;
import com.ski.common.bean.BeanGameAccount;
import com.ski.common.bean.BeanPlatformAccount;

public class Report {

    public static String createOCR(BeanCommodity commodity) {
        StringBuilder ocr = new StringBuilder(1024);
        DecimalFormat df = new DecimalFormat("###,###,###.##");
        
        BeanChannelAccount user     = CommonService.getChannelAccountByCaid(CommonService.getOrderByOid(commodity.i_oid).i_caid);
        BeanPlatformAccount puser   = CommonService.getPlatformAccountByPaid(CommonService.getPlatformAccountByCaid(user.i_caid));
        BeanGameAccount account     = CommonService.getGameAccountByGaid(Integer.parseInt(commodity.c_arg0, 16));
        List<BeanGame>  games       = CommonService.getGameByGaid(account.i_gaid);
        
        ocr.append(CommonReport.createReportHead(String.format("%s的租赁报告", user.getDisplayName())));
        List<Object> rows = new LinkedList<Object>();
        rows.add(new String[] {"游戏账号",  account.c_user});
        rows.add(new String[] {"当前密码",  account.c_pass_curr});
        rows.add(new String[] {"租赁类型",  "A".equals(commodity.c_arg1) ? "认证" : "B".equals(commodity.c_arg1) ? "不认证" : "未知"});
        rows.add(new String[] {"包含游戏",  (null == games || games.isEmpty()) ? "-" : games.stream().map(game->game.c_name_zh).collect(Collectors.joining(", "))});
        rows.add(new String[] {"租赁单价",  df.format(commodity.i_price) + "元/天"});
        rows.add(new String[] {"起租时间",  commodity.t_begin});
        if (commodity.isClose()) rows.add(new String[] {"退租时间", commodity.t_end});
        if (commodity.isClose()) rows.add(new String[] {"消费小计", df.format(commodity.i_expense) + "元"});
        rows.add(new String[] {"备    注",    0 == commodity.c_remark.length() ? "-" : commodity.c_remark});
        rows.add(new String[] {"账户余额",  puser.i_cash + "元"});
        rows.add(new String[] {"优惠券余额", puser.i_coupon + "元"});
        ocr.append(CommonReport.createReportTable(null, rows, 2));
        return ocr.toString();
    }

}
