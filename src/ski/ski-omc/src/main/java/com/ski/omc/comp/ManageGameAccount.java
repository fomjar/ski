package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.fomjar.widget.FjEditLabel;
import com.fomjar.widget.FjEditLabel.EditListener;
import com.fomjar.widget.FjListCellString;
import com.fomjar.widget.FjListPane;
import com.ski.common.CommonDefinition;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanGame;
import com.ski.common.bean.BeanGameAccount;
import com.ski.omc.UIToolkit;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ManageGameAccount extends JDialog {

    private static final long serialVersionUID = -51034836551447291L;
    
    private JToolBar    toolbar;
    private FjEditLabel i_gaid;
    private FjEditLabel c_user;
    private FjEditLabel c_pass;
    private FjEditLabel t_birth;
    private FjListPane<String> pane_games;
    
    public ManageGameAccount(int gaid) {
        super(MainFrame.getInstance());
        
        BeanGameAccount account = CommonService.getGameAccountByGaid(gaid);
        
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        toolbar.add(new JButton("更新到DB"));
        toolbar.add(new JButton("更新到PS和DB"));
        toolbar.add(new JButton("测试账号"));
        toolbar.addSeparator();
        toolbar.add(new JButton("添加游戏"));
        i_gaid = new FjEditLabel(String.format("0x%08X", account.i_gaid), false);
        c_user = new FjEditLabel(account.c_user);
        c_pass = new FjEditLabel(account.c_pass_curr);
        t_birth = new FjEditLabel(0 == account.t_birth.length() ? "(没有生日)" : account.t_birth);
        
        pane_games = new FjListPane<String>();
        pane_games.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "包含游戏"));
        
        JPanel panel_basic = new JPanel();
        panel_basic.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "基本信息"));
        panel_basic.setLayout(new BoxLayout(panel_basic, BoxLayout.Y_AXIS));
        panel_basic.add(UIToolkit.createBasicInfoLabel("GAID", i_gaid));
        panel_basic.add(UIToolkit.createBasicInfoLabel("账号", c_user));
        panel_basic.add(UIToolkit.createBasicInfoLabel("密码", c_pass));
        panel_basic.add(UIToolkit.createBasicInfoLabel("生日", t_birth));
        
        JPanel panel_north = new JPanel();
        panel_north.setLayout(new BoxLayout(panel_north, BoxLayout.Y_AXIS));
        panel_north.add(toolbar);
        panel_north.add(panel_basic);
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel_north, BorderLayout.NORTH);
        getContentPane().add(pane_games, BorderLayout.CENTER);
        
        setTitle(String.format("管理账号 - %s", account.c_user));
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(400, 320));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);
        
        registerListener();
        
        updateGameAccountGame();
    }
    
    private JSONObject args = new JSONObject();
    
    private void registerListener() {
        c_user.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gaid", Integer.parseInt(i_gaid.getText().split("x")[1], 16));
                args.put("user", new_value);
                c_user.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_pass.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gaid", Integer.parseInt(i_gaid.getText().split("x")[1], 16));
                args.put("pass_curr", new_value);   // for cdb
                args.put("user", c_user.getText()); // for wa
                args.put("pass", old_value);        // for wa
                args.put("pass_new", new_value);    // for wa
                c_pass.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        t_birth.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gaid", Integer.parseInt(i_gaid.getText().split("x")[1], 16));
                args.put("birth", new_value);
                t_birth.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        ((JButton) toolbar.getComponent(0)).addActionListener(e->{
            if (args.isEmpty()) {
                JOptionPane.showMessageDialog(ManageGameAccount.this, "没有可更新的内容", "信息", JOptionPane.PLAIN_MESSAGE);
                return;
            }
            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
            if (!UIToolkit.showServerResponse(rsp)) return;
            
            if (args.has("user"))       c_user.setForeground(Color.darkGray);
            if (args.has("pass_curr"))  c_pass.setForeground(Color.darkGray);
            if (args.has("birth"))      t_birth.setForeground(Color.darkGray);
            args.clear();
        });
        ((JButton) toolbar.getComponent(1)).addActionListener(e->{
            UIToolkit.doLater(()->{
                if (args.isEmpty()) {
                    JOptionPane.showMessageDialog(ManageGameAccount.this, "没有可更新的内容", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!args.has("pass")) {
                    JOptionPane.showMessageDialog(ManageGameAccount.this, "PlayStation网站上只可以更新密码", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                ((JButton) toolbar.getComponent(1)).setEnabled(false);
                FjDscpMessage rsp_wa = CommonService.send("wa", CommonDefinition.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
                
                if (!UIToolkit.showServerResponse(rsp_wa)) return;
                
                FjDscpMessage rsp_cdb = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
                if (!UIToolkit.showServerResponse(rsp_cdb)) return;
                
                if (args.has("user"))   c_user.setForeground(Color.darkGray);
                if (args.has("pass"))   c_pass.setForeground(Color.darkGray);
                if (args.has("birth"))  t_birth.setForeground(Color.darkGray);
                args.clear();
                ((JButton) toolbar.getComponent(1)).setEnabled(true);
            });
        });
        ((JButton) toolbar.getComponent(2)).addActionListener(e->{
            UIToolkit.doLater(()->{
                ((JButton) toolbar.getComponent(2)).setEnabled(false);
                args.put("user", c_user.getText());
                args.put("pass", c_pass.getText());
                FjDscpMessage rsp = CommonService.send("wa", CommonDefinition.ISIS.INST_ECOM_APPLY_GAME_ACCOUNT_VERIFY, args);
                if (CommonService.isResponseSuccess(rsp) && rsp.toString().contains(" binded"))
                    JOptionPane.showMessageDialog(ManageGameAccount.this, "此账号已绑定", "信息", JOptionPane.PLAIN_MESSAGE);
                else if (CommonService.isResponseSuccess(rsp) && rsp.toString().contains(" unbinded"))
                    JOptionPane.showMessageDialog(ManageGameAccount.this, "此账号没有绑定", "信息", JOptionPane.PLAIN_MESSAGE);
                else
                    UIToolkit.showServerResponse(rsp);
                
                args.clear();
                ((JButton) toolbar.getComponent(2)).setEnabled(true);
            });
        });
        ((JButton) toolbar.getComponent(4)).addActionListener(e->{
            BeanGame game = UIToolkit.chooseGame();
            if (null == game) return;
            
            if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(ManageGameAccount.this, "即将添加游戏：" + game.c_name_zh, "信息", JOptionPane.OK_CANCEL_OPTION))
                return;
            
            int gaid = Integer.parseInt(i_gaid.getText().split("x")[1], 16);
            int gid  = game.i_gid;
            JSONObject args = new JSONObject();
            args.put("gaid", gaid);
            args.put("gid", gid);
            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT_GAME, args);
            CommonService.updateGameAccountGame();
            UIToolkit.showServerResponse(rsp);
            updateGameAccountGame();
        });
    }
    
    private void updateGameAccountGame() {
        int gaid = Integer.parseInt(i_gaid.getText().split("x")[1], 16);

        pane_games.getList().removeAllCell();
        CommonService.getGameAccountGameAll().forEach(bean->{
            if (gaid == bean.i_gaid) {
                BeanGame game = CommonService.getGameByGid(bean.i_gid);
                pane_games.getList().addCell(new FjListCellString(String.format("0x%08X - %s", game.i_gid, game.c_name_zh)));
            }
        });
    }
    
}
