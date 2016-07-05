package com.ski.omc;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.ski.common.CommonService;
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
        
        ocr.append(createReportHead(String.format("%s的租赁报告", user.getDisplayName())));
        List<Object> rows = new LinkedList<Object>();
        rows.add(new String[] {"<div bgcolor='#AAAAFF'>游戏账号</div>",  "<div bgcolor='#AAAAFF'>" + account.c_user + "</div>"});
        rows.add(new String[] {"<div bgcolor='#AAAAFF'>当前密码</div>",  "<div bgcolor='#AAAAFF'>" + account.c_pass_curr + "</div>"});
        rows.add(new String[] {"租赁类型",  "A".equals(commodity.c_arg1) ? "认证" : "B".equals(commodity.c_arg1) ? "不认证" : "未知"});
        rows.add(new String[] {"包含游戏",  (null == games || games.isEmpty()) ? "-" : games.stream().map(game->game.c_name_zh).collect(Collectors.joining(", "))});
        rows.add(new String[] {"租赁单价",  df.format(commodity.i_price) + "元/天"});
        rows.add(new String[] {"起租时间",  commodity.t_begin});
        if (commodity.isClose()) rows.add(new String[] {"退租时间", commodity.t_end});
        if (commodity.isClose()) rows.add(new String[] {"消费小计", df.format(commodity.i_expense) + "元"});
        rows.add(new String[] {"备    注",    0 == commodity.c_remark.length() ? "-" : commodity.c_remark});
        rows.add("&nbsp;");
        rows.add(new String[] {"账户余额",  puser.i_cash + "元"});
        if (0 != puser.i_coupon) rows.add(new String[] {"优惠券余额", puser.i_coupon + "元"});
        ocr.append(createReportTable(null, rows, 2));
        return ocr.toString();
    }
    
    public static String createReportHead(String title) {
        return String.format("<html><head><meta charset=\"utf-8\" />"
                + "<style type=\"text/css\">"
                + "body {width: 300px; text-align: center}"
                + "table {width: 100%%; border-collapse: collapse; border-spacing: 0}"
                + "td {vertical-align: middle; border: 1px solid black; text-align: center; background-color: #EEEEEE}"
                + ".title {color: #884444}"
                + ".category {text-align: left; padding-left: 8px; background-color: #444488; color: #EEEEEE}"
                + "</style>"
                + "<title>%s</title></head>"
                + "<body><table><tr><td class='title'><h1>%s</h1>"
                + "<div style='text-align: right; font-size: 8px'>——此报告由\"SKI系统\"于 %s 自动生成</div></td></tr></table>",
                title,
                title,
                new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
    }
    
    public static String createReportTable(String category, List<Object> data, int maxcol) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("<table>");
        if (null != category && 0 < category.length())
            sb.append(String.format("<tr><td colspan='%d' class='category'><h2>%s</h2></td></tr>", maxcol, category));
        for (Object row : data) {
            StringBuilder sbrow = new StringBuilder(128);
            sbrow.append("<tr>");
            if (row instanceof String[]) {
                for (String col : (String[]) row) {
                    sbrow.append(String.format("<td>%s</td>", col));
                }
            } else {
                sbrow.append(String.format("<td colspan='%d' align='right' cellpadding='8px'>%s</td>", maxcol, row.toString()));
            }
            sbrow.append("</tr>");
            sb.append(sbrow);
        }
        sb.append("</table>");
        return sb.toString();
    }
}
