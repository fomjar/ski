package com.ski.wca;

import java.util.List;

public class WechatForm {
    
    public static final String FORM_CELL    = "cell";
    public static final String FORM_MESSAGE = "msg";
    
    public static final String BUTTON_PRIMARY   = "weui_btn_primary";
    public static final String BUTTON_WARN      = "weui_btn_warn";
    public static final String BUTTON_DEFAULT   = "weui_btn_default";
    
    public static final String MESSAGE_SUCCESS      = "weui_icon_msg weui_icon_success";
    public static final String MESSAGE_INFO         = "weui_icon_msg weui_icon_info";
    public static final String MESSAGE_WARN         = "weui_icon_msg weui_icon_warn";
    public static final String MESSAGE_WAITING      = "weui_icon_msg weui_icon_waiting";
    public static final String MESSAGE_SAFE_SUCCESS = "weui_icon_safe weui_icon_safe_success";
    public static final String MESSAGE_SAFE_WARN    = "weui_icon_safe weui_icon_safe_warn";
    
    public static String createFormHead(String type, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<!DOCTYPE html><html>"
                + "<head>"
                + "<meta charset='utf-8'/>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1,user-scalable=0'/>"
                + "<title>%s</title>"
                + "<link rel='stylesheet' href='/ski-wcweb/weui/dist/style/weui.css'/>"
                + "<link rel='stylesheet' href='/ski-wcweb/weui/dist/example/example.css'/>"
                + "<script type='text/javascript' src='/ski-wcweb/jquery-3.0.0.min.js'></script>"
                + "</head>"
                + "<body><div class='%s'>", title, type));
        switch (type) {
        case FORM_CELL:
            sb.append(String.format("<div class='hd'><h1 class='page_title'>%s</h1></div><div class='bd'>", title));
            break;
        case FORM_MESSAGE:
            sb.append("<div class='weui_msg'>");
            break;
        }
        return sb.toString();
    }
    
    public static String createFormFoot() {
        return "</div></div></body></html>";
    }
    
    /**
     * 
     * @param group
     * @param cells [primary, secondary]
     * @return
     */
    public static String createFormCellGroup(String group, List<String[]> cells, String tip) {
        StringBuilder sb = new StringBuilder();
        if (null == group) group = "";
        sb.append(String.format("<div class='weui_cells_title'>%s</div><div class='weui_cells'>", group));
        if (null != cells) {
            for (String[] cell : cells) {
                if (0 == cell.length) continue;
                if (1 == cell.length) sb.append(createFormCell(cell[0], null));
                else sb.append(createFormCell(cell[0], cell[1]));
            }
        }
        sb.append("</div>");
        if (null != tip) sb.append(String.format("<div class='weui_cells_tips'>%s</div>", tip));
        return sb.toString();
    }
    
    public static String createFormCell(String primary, String secondary) {
        if (null == primary)    primary     = "";
        if (null == secondary)  secondary   = "";
        return String.format("<div class='weui_cell'><div class='weui_cell_bd weui_cell_primary'><p>%s</p></div><div class='weui_cell_ft'>%s</div></div>", primary, secondary);
    }
    
    /**
     * 
     * @param group
     * @param cells [primary, secondary, href] or [primary, href]
     * @return
     */
    public static String createFormCellAccessGroup(String group, List<String[]> cells, String tip) {
        if (null == group) group = "";
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<div class='weui_cells_title'>%s</div><div class='weui_cells weui_cells_access'>", group));
        if (null != cells) {
            for (String[] cell : cells) {
                if (1 >= cell.length) continue;
                if (2 == cell.length) sb.append(createFormCellAccess(cell[0], null, cell[1]));
                else sb.append(createFormCellAccess(cell[0], cell[1], cell[2]));
            }
        }
        sb.append("</div>");
        if (null != tip) sb.append(String.format("<div class='weui_cells_tips'>%s</div>", tip));
        return sb.toString();
    }
    
    public static String createFormCellAccess(String primary, String secondary, String href) {
        if (null == primary)    primary     = "";
        if (null == secondary)  secondary   = "";
        if (null == href)       href        = "";
        return String.format("<a class='weui_cell' href='%s'><div class='weui_cell_bd weui_cell_primary'><p>%s</p></div><div class='weui_cell_ft'>%s</div></a>", href, primary, secondary);
    }
    
    /**
     * 
     * @param group
     * @param cells [id, primary, placeholder]
     * @return
     */
    public static String createFormCellFormGroup(String group, List<String[]> cells, String tip) {
        StringBuilder sb = new StringBuilder();
        if (null == group) group = "";
        sb.append(String.format("<div class='weui_cells_title'>%s</div><div class='weui_cells weui_cells_form'>", group));
        if (null != cells) {
            for (String[] cell : cells) {
                if (3 != cell.length) continue;
                sb.append(createFormCellForm(cell[0], cell[1], cell[2]));
            }
        }
        sb.append("</div>");
        if (null != tip) sb.append(String.format("<div class='weui_cells_tips'>%s</div>", tip));
        return sb.toString();
    }
    
    public static String createFormCellForm(String id, String primary, String placeholder) {
        if (null == id)             id          = "";
        if (null == primary)        primary     = "";
        if (null == placeholder)    placeholder = "";
        return String.format("<div class='weui_cell'>"
                + "<div class='weui_cell_hd'><label class='weui_label'>%s</label></div>"
                + "<div class='weui_cell_bd weui_cell_primary'><input class='weui_input' placeholder='%s' id='%s'></input></div>"
                + "</div>",
                primary,
                placeholder,
                id);
    }
    
    /**
     * 
     * @param type
     * @param name
     * @param href javascript code
     * @return
     */
    public static String createFormButton(String type, String name, String href) {
        if (null == href) href = "";
        return String.format("<div class='weui_btn_area'><a class='weui_btn %s' href=\"javascript:%s\">%s</a></div>", type, href, name);
    }
    
    /**
     * 
     * @param type
     * @param title
     * @param description
     * @param buttons [name, type, href]
     * @param detail_href
     * @return
     */
    public static String createFormMessage(String type, String title, String description, List<String[]> buttons, String detail_href) {
        StringBuilder sb = new StringBuilder();
        if (null == title)          title       = "";
        if (null == description)    description = "";
        sb.append(createFormHead(FORM_MESSAGE, title));
        sb.append(String.format("<div class='weui_icon_area'><i class='%s'></i></div>", type));
        sb.append(String.format("<div class='weui_text_area'><h2 class='weui_msg_title'>%s</h2><p class='weui_msg_desc'>%s</p></div>", title, description));
        if (null != buttons) {
            sb.append("<div class='weui_opr_area'><p class='weui_btn_area'>");
            for (String[] button : buttons) {
                sb.append(String.format("<a href='%s' class='weui_btn %s'>%s</a>", button[2], button[1], button[0]));
            }
            sb.append("</p></div>");
        }
        if (null != detail_href) sb.append(String.format("<div class='weui_extra_area'><a href='%s'>查看详情</a></div>", detail_href));
        sb.append(createFormFoot());
        return sb.toString();
        
    }

}
