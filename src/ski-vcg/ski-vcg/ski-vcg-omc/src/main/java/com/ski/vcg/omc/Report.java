package com.ski.vcg.omc;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.ski.vcg.common.CommonService;
import com.ski.vcg.common.bean.BeanChannelAccount;
import com.ski.vcg.common.bean.BeanCommodity;
import com.ski.vcg.common.bean.BeanGame;
import com.ski.vcg.common.bean.BeanGameAccount;
import com.ski.vcg.common.bean.BeanPlatformAccount;

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
        rows.add(new String[] {"<div style='font-weight: bold; background-color: #FFFF00'>游戏账号</div>",
                "<div style='font-weight: bold; background-color: #FFFF00'>" + account.c_user + "</div>"});
        if (!commodity.isClose()) rows.add(new String[] {"<div style='font-weight: bold; background-color: #FFFF00'>当前密码</div>",
                "<div style='font-weight: bold; background-color: #FFFF00'>" + account.c_pass + "</div>"});
        rows.add(new String[] {"租赁类型",  "A".equals(commodity.c_arg1) ? "认证" : "B".equals(commodity.c_arg1) ? "不认证" : "未知"});
        rows.add(new String[] {"包含游戏",  (null == games || games.isEmpty()) ? "-" : games.stream().map(game->game.c_name_zh_cn).collect(Collectors.joining(", "))});
        rows.add(new String[] {"租赁单价",  df.format(commodity.i_price) + "元/天"});
        rows.add(new String[] {"起租时间",  commodity.t_begin});
        if (commodity.isClose()) rows.add(new String[] {"退租时间", commodity.t_end});
        if (commodity.isClose()) rows.add(new String[] {"消费小计", df.format(commodity.i_expense) + "元"});
        rows.add(new String[] {"备&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;注",    0 == commodity.c_remark.length() ? "-" : commodity.c_remark});
        rows.add("&nbsp;");
        rows.add(new String[] {"账户余额",  puser.i_cash + "元"});
        if (0 != puser.i_coupon) rows.add(new String[] {"优惠券余额", puser.i_coupon + "元"});
        ocr.append(createReportTable("租赁信息", rows, 2));
        
        if (!commodity.isClose()) {
            rows.clear();
            switch (commodity.c_arg1) {
            case "A":
                rows.add("1.<span style='color: #BB0000'>认证租，请认证主机为常用PS4</span>，用自己帐号玩游戏；<br/>"
                       + "2.<span style='color: #BB0000'>“此账户再其他PS4登陆”属正常现象</span>，不认证用户变更导致。<br/>"
                       + "点击“确定”即可，勿切换回租赁帐号。否则会影响不认真玩家；<br/>"
                       + "3.VC电玩十分感谢您的配合！");
                break;
            case "B":
                rows.add("1.<span style='color: #BB0000'>不认证租，请不要认证主机为常用PS4</span>，否则会影响认证玩家；<br/>"
                       + "2.<span style='color: #BB0000'>“账号在其他PS4登陆”属正常现象</span>，认证用户变更导致。<br/>"
                       + "请10分钟再登，勿立刻登入，否则协商会耽误您宝贵时间；<br/>"
                       + "3.VC电玩 十分感谢您的配合！");
                break;
            }
            ocr.append(createReportTable("玩前必读", rows, 1));
        }
        ocr.append(createReportFoot(user.getDisplayName()));
        return ocr.toString();
    }
    
    public static String createReportHead(String title) {
        return String.format("<html><head><meta charset=\"utf-8\" />"
                + "<style type=\"text/css\">"
                + "body {width: 380px; text-align: center}"
                + "table {width: 100%%; border-collapse: collapse; border-spacing: 0; font-family: 微软雅黑; font-size: 12px}"
                + "td {vertical-align: middle; border: 1px solid #888888; padding: 4px; text-align: center; background-color: #EEEEEE; color: #888888}"
                + ".title {color: #444488}"
                + ".category {height: 30px; text-align: left; padding-left: 8px; background-color: #888888; color: #EEEEEE; font-size: 105%%}"
                + "</style>"
                + "<title>%s</title></head>"
                + "<body><table><tr><td class='title'><h1>%s</h1></td></tr></table>",
                title,
                title);
    }
    
    public static String createReportTable(String category, List<Object> data, int maxcol) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("<table>");
        if (null != category && 0 < category.length())
            sb.append(String.format("<tr><td colspan='%d' class='category'><strong>%s</strong></td></tr>", maxcol, category));
        for (Object row : data) {
            StringBuilder sbrow = new StringBuilder(128);
            sbrow.append("<tr>");
            if (row instanceof String[]) {
                for (String col : (String[]) row) {
                    sbrow.append(String.format("<td>%s</td>", col));
                }
            } else {
                sbrow.append(String.format("<td colspan='%d' style='text-align: left'>%s</td>", maxcol, row.toString()));
            }
            sbrow.append("</tr>");
            sb.append(sbrow);
        }
        sb.append("</table>");
        return sb.toString();
    }
    
    public static String createReportFoot(String foot) {
        return String.format("<table style='font-size: 90%%'><tr>"
                + "<td style='background-color: #888888; color: #EEEEEE; padding: 0px; padding-left: 4px; text-align: left'>%s</td>"
                + "<td style='background-color: #888888; color: #EEEEEE; padding: 0px; padding-right: 4px; text-align: right'>此报告由 SKI系统 于 %s 自动生成</td>"
                + "</tr></table>",
                foot,
                new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
    }
}
