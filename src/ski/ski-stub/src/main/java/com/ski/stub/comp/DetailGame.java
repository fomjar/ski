package com.ski.stub.comp;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JComponent;

import net.sf.json.JSONObject;

public class DetailGame extends JComponent {
    
    private static final long serialVersionUID = -1556501032491331496L;
    private BaseTextField c_country;
    private BaseTextField t_sale;
    private BaseTextField c_name_zh;
    
    public DetailGame() {
        c_country   = new BaseTextField();
        c_country.setTipText("国家");
        t_sale      = new BaseTextField();
        t_sale.setTipText("发售日期");
        c_name_zh   = new BaseTextField();
        c_name_zh.setTipText("简体中文名");
        
        setPreferredSize(new Dimension(200, 70));
        setLayout(new GridLayout(3, 1));
        add(c_country);
        add(t_sale);
        add(c_name_zh);
    }
    
    public JSONObject toJson() {
        JSONObject args = new JSONObject();
        if (0 != c_country.getText().length())  args.put("country", c_country.getText());
        if (0 != t_sale.getText().length())     args.put("sale", t_sale.getText());
        if (0 != c_name_zh.getText().length())  args.put("name_zh", c_name_zh.getText());
        return args;
    }

}
