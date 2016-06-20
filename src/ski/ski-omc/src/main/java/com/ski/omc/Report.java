package com.ski.omc;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.ski.omc.bean.BeanChannelAccount;
import com.ski.omc.bean.BeanCommodity;
import com.ski.omc.bean.BeanGame;
import com.ski.omc.bean.BeanGameAccount;

public class Report {

    public static String createOCR(BeanCommodity commodity) {
        StringBuilder ocr = new StringBuilder(1024);
        DecimalFormat df = new DecimalFormat("###,###,###.##");
        
        BeanChannelAccount user = Service.map_channel_account.get(Service.map_order.get(commodity.i_oid).i_caid);
        BeanGameAccount account = Service.map_game_account.get(Integer.parseInt(commodity.c_arg0, 16));
        List<BeanGame>  games   = Service.getGameAccountGames(account.i_gaid);
        
        ocr.append(createReportHead(String.format("%s租赁报告", user.c_user)));
        List<Object> rows = new LinkedList<Object>();
        rows.add(new String[] {"游戏账号",  account.c_user});
        rows.add(new String[] {"当前密码",  account.c_pass_curr});
        rows.add(new String[] {"租赁类型",  commodity.c_arg1});
        rows.add(new String[] {"包含游戏",  (null == games || games.isEmpty()) ? "-" : games.stream().map(game->game.c_name_zh).collect(Collectors.joining(", "))});
        rows.add(new String[] {"租赁单价",  df.format(commodity.i_price) + "元/天"});
        rows.add(new String[] {"起租时间",  commodity.t_begin});
        if (commodity.isClose()) rows.add(new String[] {"退租时间", commodity.t_end});
        if (commodity.isClose()) rows.add(new String[] {"消费小计", df.format(commodity.i_expense)});
        rows.add(new String[] {"备    注",     0 == commodity.c_remark.length() ? "-" : commodity.c_remark});
        ocr.append(createReportTable(null, rows, 2));
        return ocr.toString();
    }
    
    private static String createReportHead(String title) {
        return String.format("<table><tr><td><h1>%s</h1>"
                + "<div style='text-align: right; font-size: 8px'>——此报告由\"SKI系统\"于 %s 自动生成</div></td></tr></table>",
                title,
                new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
    }
    
    private static String createReportTable(String category, List<Object> data, int maxcol) {
        StringBuilder sbtable = new StringBuilder(512);
        sbtable.append("<table>");
        if (null != category && 0 < category.length())
            sbtable.append(String.format("<tr><td colspan='%d' class='category'><h2>%s</h2></td></tr>", maxcol, category));
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
            sbtable.append(sbrow);
        }
        sbtable.append("</table>");
        return sbtable.toString();
    }

}
