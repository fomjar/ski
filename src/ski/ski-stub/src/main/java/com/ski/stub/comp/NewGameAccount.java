package com.ski.stub.comp;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JComponent;

import com.fomjar.widget.FjTextField;

import net.sf.json.JSONObject;

public class NewGameAccount extends JComponent {
    
    private static final long serialVersionUID = -1556501032491331496L;
    private FjTextField c_user;
    private FjTextField c_pass;
    private FjTextField t_birth;
    
    public NewGameAccount() {
        c_user   = new FjTextField();
        c_user.setDefaultTips("账号");
        c_pass      = new FjTextField();
        c_pass.setDefaultTips("密码");
        t_birth   = new FjTextField();
        t_birth.setDefaultTips("出生日期");
        
        setPreferredSize(new Dimension(200, 70));
        setLayout(new GridLayout(3, 1));
        add(c_user);
        add(c_pass);
        add(t_birth);
    }
    
    public JSONObject toJson() {
        JSONObject args = new JSONObject();
        if (0 != c_user.getText().length())     args.put("user", c_user.getText());
        if (0 != c_pass.getText().length())     args.put("pass_curr", c_pass.getText());
        if (0 != t_birth.getText().length())    args.put("birth", t_birth.getText());
        return args;
    }

}
