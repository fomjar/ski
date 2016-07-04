package com.ski.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CommonReport {
    
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
