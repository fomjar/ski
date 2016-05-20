package com.ski.stub;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.ski.common.SkiCommon;
import com.ski.stub.bean.BeanGameAccount;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class TabGameAccountOperate extends TabPaneBase {

    private static final long serialVersionUID = 685344416802802241L;
    
    private BeanGameAccount current;
    private JPanel          select;
    private JScrollPane     detail;
    private JScrollPane     output;
    
    public TabGameAccountOperate() {
        addField(CommonUI.createPanelLabelCombo("G       ID  (游戏ID)", new String[] {}));
        addField(CommonUI.createPanelLabelCombo("GA      ID  (游戏账户ID)", new String[] {}));
        addField(detail = CommonUI.createPanelTitleArea("响应消息", false));
        ((JTextArea) detail.getViewport().getView()).setRows(7);
        addField(select = CommonUI.createPanelRadioButton("选择想要执行的操作", new String[] {
                "在PlayStation网站上创建此游戏账号(暂未支持)",
                "修改此游戏账号的当前密码为A密码",
                "修改此游戏账号的当前密码为B密码",
                "验证此游戏账号是否存在已绑定的设备"}));
        addField(output = CommonUI.createPanelTitleArea("执行结果", false));
        ((JTextArea) output.getViewport().getView()).setRows(4);
        
        getFieldToCombo(0).addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (ItemEvent.DESELECTED == e.getStateChange()) return;
                
                refreshCurrentGameAccount();
            }
        });
        getFieldToCombo(1).addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (ItemEvent.DESELECTED == e.getStateChange()) return;
                
                current = Service.map_game_account.get(Integer.parseInt(getFieldToCombo(1).getSelectedItem().toString().split(" ")[0].split("x")[1], 16));
                JTextArea jta = (JTextArea) detail.getViewport().getView();
                jta.setText(String.format(
                          "游戏ID    (GID) ：0x%08X\n"
                        + "游戏账户ID(GAID)：0x%08X\n"
                        + "账号            ：%s\n"
                        + "密码A           ：%s\n"
                        + "密码B           ：%s\n"
                        + "当前密码        ：%s\n"
                        + "生日            ：%s",
                        current.i_gid,
                        current.i_gaid,
                        current.c_user,
                        current.c_pass_a,
                        current.c_pass_b,
                        current.c_pass_curr,
                        current.t_birth));
            }
        });
        getFieldToCombo(1).addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("enabled".equals(evt.getPropertyName())) {
                    if (!((Boolean)evt.getNewValue()).booleanValue()) {
                        ((JTextArea) detail.getViewport().getView()).setText(null);
                    }
                }
            }
        });
    }

    @Override
    protected void update() {
        Service.updateGame();
        
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)getFieldToCombo(0).getModel();
        model.removeAllElements();
        
        if (!Service.map_game.isEmpty()) {
            Service.map_game.forEach((gid, game)->{
                model.addElement(String.format("0x%08X - %s(%s)", gid, game.c_name_zh, game.c_name_en));
            });
            if (0 < model.getSize()) getFieldToCombo(0).setEnabled(true);
            else {
                getFieldToCombo(0).setEnabled(false);
                getFieldToCombo(1).setEnabled(false);
            }
        } else {
            getFieldToCombo(0).setEnabled(false);
            getFieldToCombo(1).setEnabled(false);
        }
        
        if (Service.map_game.isEmpty()
                || Service.map_game_account.isEmpty()) {
            disableSubmit();
        } else enableSubmit();
    }
    
    private void refreshCurrentGameAccount() {
        int gid = Integer.parseInt(getFieldToCombo(0).getSelectedItem().toString().split(" ")[0].split("x")[1], 16);
        
        Service.updateGameAccount(gid);
        
        int index = getFieldToCombo(1).getSelectedIndex();
        
        DefaultComboBoxModel<String> model_gaid = (DefaultComboBoxModel<String>)getFieldToCombo(1).getModel();
        model_gaid.removeAllElements();
        
        if (!Service.map_game_account.isEmpty()) {
            Service.map_game_account.forEach((gaid, game_account)->{
                model_gaid.addElement(String.format("0x%08X - %s", gaid, game_account.c_user));
            });
            if (0 < model_gaid.getSize()) getFieldToCombo(1).setEnabled(true);
            else getFieldToCombo(1).setEnabled(false);
            
            if (-1 != index && index < Service.map_game_account.size()) getFieldToCombo(1).setSelectedIndex(index);
        } else getFieldToCombo(1).setEnabled(false);
        
        if (Service.map_game.isEmpty()
                || Service.map_game_account.isEmpty()) {
            disableSubmit();
        } else enableSubmit();
    }

    @Override
    protected void submit() {
        if (((JRadioButton)select.getComponent(0)).isSelected()) {
            setStatus("操作成功");
        } else if (((JRadioButton)select.getComponent(1)).isSelected()) {
            if (current.c_pass_a.equals(current.c_pass_curr)) setStatus("此游戏账号的当前密码已经是A密码");
            else {
                JSONObject args = new JSONObject();
                args.put("user",        current.c_user);
                args.put("pass",        current.c_pass_curr);
                args.put("pass_new",    current.c_pass_a);
                FjDscpMessage rsp = Service.send("wa", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
                appendOutput(rsp.toString());
                
                if (!Service.isResponseSuccess(rsp)) {
                    setStatus(rsp.args().toString());
                    return;
                }
                
                args.clear();
                args.put("gid", current.i_gid);
                args.put("gaid", current.i_gaid);
                args.put("pass_curr", current.c_pass_a);
                rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
                appendOutput(rsp.toString());
                
                if (!Service.isResponseSuccess(rsp)) {
                    setStatus(rsp.args().toString());
                    return;
                }
                
                setStatus("操作成功");
                refreshCurrentGameAccount();
            }
        } else if (((JRadioButton)select.getComponent(2)).isSelected()) {
            if (current.c_pass_b.equals(current.c_pass_curr)) setStatus("此游戏账号的当前密码已经是B密码");
            else {
                JSONObject args = new JSONObject();
                args.put("user",        current.c_user);
                args.put("pass",        current.c_pass_curr);
                args.put("pass_new",    current.c_pass_b);
                FjDscpMessage rsp = Service.send("wa", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
                appendOutput(rsp.toString());
                
                if (!Service.isResponseSuccess(rsp)) {
                    setStatus(rsp.args().toString());
                    return;
                }
                
                args.clear();
                args.put("gid", current.i_gid);
                args.put("gaid", current.i_gaid);
                args.put("pass_curr", current.c_pass_b);
                rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args);
                appendOutput(rsp.toString());
                
                if (!Service.isResponseSuccess(rsp)) {
                    setStatus(rsp.args().toString());
                    return;
                }
                
                setStatus("操作成功");
                refreshCurrentGameAccount();
            }
        } else if (((JRadioButton)select.getComponent(3)).isSelected()) {
            JSONObject args = new JSONObject();
            args.put("user",        current.c_user);
            args.put("pass",        current.c_pass_curr);
            FjDscpMessage rsp = Service.send("wa", SkiCommon.ISIS.INST_ECOM_VERIFY_ACCOUNT, args);
            appendOutput(rsp.toString());
            
            if (Service.isResponseSuccess(rsp)) setStatus("操作成功");
            else setStatus(rsp.args().toString());
        } else {
            setStatus("请先选择一项操作再执行提交");
        }
    }
    
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private void appendOutput(String text) {
        JTextArea jta = (JTextArea) output.getViewport().getView();
        jta.append("================================[" + sdf.format(new Date()) + "]================================\n");
        jta.append(text + "\n");
        jta.setSelectionStart(jta.getText().length());
    }

}
