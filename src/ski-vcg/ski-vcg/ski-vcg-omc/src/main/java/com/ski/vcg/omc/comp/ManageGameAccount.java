package com.ski.vcg.omc.comp;

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
import com.ski.vcg.common.CommonDefinition;
import com.ski.vcg.common.CommonService;
import com.ski.vcg.common.bean.BeanGame;
import com.ski.vcg.common.bean.BeanGameAccount;
import com.ski.vcg.omc.UIToolkit;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ManageGameAccount extends JDialog {

    private static final long serialVersionUID = -51034836551447291L;

    private int gaid;
    private JToolBar    toolbar;
    private FjEditLabel i_gaid;
    private FjEditLabel c_user;
    private FjEditLabel c_pass;
    private FjEditLabel c_name;
    private FjEditLabel c_remark;
    private FjEditLabel t_birth;
    private FjListPane<String> pane_games;

    public ManageGameAccount(int gaid) {
        super(MainFrame.getInstance());

        this.gaid = gaid;

        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        toolbar.add(new JButton("更新到DB"));
        toolbar.add(new JButton("更新到PS和DB"));
        toolbar.add(new JButton("测试账号"));
        toolbar.addSeparator();
        toolbar.add(new JButton("添加游戏"));
        toolbar.addSeparator();
        toolbar.add(new JButton("查看流水"));
        i_gaid = new FjEditLabel(false);
        c_user = new FjEditLabel();
        c_pass = new FjEditLabel();
        c_name = new FjEditLabel();
        c_remark = new FjEditLabel();
        t_birth = new FjEditLabel();

        pane_games = new FjListPane<String>();
        pane_games.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "包含游戏"));

        JPanel panel_basic = new JPanel();
        panel_basic.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "基本信息"));
        panel_basic.setLayout(new BoxLayout(panel_basic, BoxLayout.Y_AXIS));
        panel_basic.add(UIToolkit.createBasicInfoLabel("GAID", i_gaid));
        panel_basic.add(UIToolkit.createBasicInfoLabel("账号", c_user));
        panel_basic.add(UIToolkit.createBasicInfoLabel("密码", c_pass));
        panel_basic.add(UIToolkit.createBasicInfoLabel("昵称", c_name));
        panel_basic.add(UIToolkit.createBasicInfoLabel("备注", c_remark));
        panel_basic.add(UIToolkit.createBasicInfoLabel("生日", t_birth));

        JPanel panel_north = new JPanel();
        panel_north.setLayout(new BoxLayout(panel_north, BoxLayout.Y_AXIS));
        panel_north.add(toolbar);
        panel_north.add(panel_basic);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel_north, BorderLayout.NORTH);
        getContentPane().add(pane_games, BorderLayout.CENTER);

        setTitle("管理账号");
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(400, 320));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);

        registerListener();

        updateGameAccount();
        updateGameAccountGame();
    }

    private JSONObject args = new JSONObject();

    private void registerListener() {
        c_user.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gaid", gaid);
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
                args.put("gaid",        gaid);
                args.put("user",        c_user.getText());
                args.put("pass",        CommonService.getGameAccountByGaid(gaid).c_pass);
                args.put("pass_new",    new_value);
                c_pass.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_name.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gaid", gaid);
                args.put("name", new_value);
                c_name.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_remark.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gaid", gaid);
                args.put("remark", new_value);
                c_remark.setForeground(UIToolkit.COLOR_MODIFYING);
            }
            @Override
            public void cancelEdit(String value) {}
        });
        t_birth.addEditListener(new EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("gaid", gaid);
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
            if (args.has("pass_new")) args.put("pass", args.getString("pass_new"));
            FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
            CommonService.updateGameAccount();
            if (!UIToolkit.showServerResponse(rsp)) return;

            if (args.has("user"))   c_user.setForeground(Color.darkGray);
            if (args.has("pass"))   c_pass.setForeground(Color.darkGray);
            if (args.has("name"))   c_name.setForeground(Color.darkGray);
            if (args.has("remark")) c_remark.setForeground(Color.darkGray);
            if (args.has("birth"))  t_birth.setForeground(Color.darkGray);
            args.clear();

            updateGameAccount();
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

                args.put("pass", args.getString("pass_new"));
                FjDscpMessage rsp_cdb = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
                CommonService.updateGameAccount();
                if (!UIToolkit.showServerResponse(rsp_cdb)) return;

                if (args.has("user"))   c_user.setForeground(Color.black);
                if (args.has("pass"))   c_pass.setForeground(Color.black);
                if (args.has("name"))   c_name.setForeground(Color.black);
                if (args.has("remark")) c_remark.setForeground(Color.black);
                if (args.has("birth"))  t_birth.setForeground(Color.black);
                args.clear();
                ((JButton) toolbar.getComponent(1)).setEnabled(true);

                updateGameAccount();
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

            if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(ManageGameAccount.this, "即将添加游戏：" + game.c_name_zh_cn, "信息", JOptionPane.OK_CANCEL_OPTION))
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
        ((JButton) toolbar.getComponent(6)).addActionListener(e->{
            new ManageGameAccountRentHistory(this.gaid).setVisible(true);
        });
    }

    private void updateGameAccount() {
        BeanGameAccount account = CommonService.getGameAccountByGaid(gaid);

        i_gaid.setText(String.format("0x%08X", account.i_gaid));
        c_user.setText(account.c_user);
        c_pass.setText(account.c_pass);
        c_name.setText(account.c_name);
        c_remark.setText(account.c_remark);
        t_birth.setText(account.t_birth);
    }

    private void updateGameAccountGame() {
        int gaid = Integer.parseInt(i_gaid.getText().split("x")[1], 16);

        pane_games.getList().removeAllCell();
        CommonService.getGameAccountGameAll().forEach(bean->{
            if (gaid == bean.i_gaid) {
                BeanGame game = CommonService.getGameByGid(bean.i_gid);
                pane_games.getList().addCell(new FjListCellString(String.format("0x%08X - %s", game.i_gid, game.c_name_zh_cn)));
            }
        });
    }

}
