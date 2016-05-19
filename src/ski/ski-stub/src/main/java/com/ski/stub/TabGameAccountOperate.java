package com.ski.stub;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.ski.stub.bean.BeanGameAccount;

public class TabGameAccountOperate extends TabPaneBase {

    private static final long serialVersionUID = 685344416802802241L;
    
    private BeanGameAccount current;
    private JPanel          select;
    private JScrollPane     detail;
    private JTextField      result;
    
    public TabGameAccountOperate() {
        addField(CommonUI.createPanelLabelCombo("G       ID", new String[] {}));
        addField(CommonUI.createPanelLabelCombo("GA      ID", new String[] {}));
        addField(detail = CommonUI.createPanelTitleArea("响应消息", false));
        addField(select = CommonUI.createPanelRadioButton("选择想要执行的操作", new String[] {
                "修改此游戏账号的当前密码为A密码",
                "修改此游戏账号的当前密码为B密码",
                "验证此游戏账号是否存在已绑定的设备"}));
        addField(result = CommonUI.createPanelTitleField("执行结果", false));
        
        getFieldToCombo(0).addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (ItemEvent.DESELECTED == e.getStateChange()) return;
                
                int gid = Integer.parseInt(getFieldToCombo(0).getSelectedItem().toString().split(" ")[0].split("x")[1], 16);
                Service.updateGameAccount(gid);
                
                DefaultComboBoxModel<String> model_gaid = (DefaultComboBoxModel<String>)getFieldToCombo(1).getModel();
                model_gaid.removeAllElements();
                
                if (!Service.map_game_account.isEmpty()) {
                    Service.map_game_account.forEach((gaid, game_account)->{
                        model_gaid.addElement(String.format("0x%08X - %s", gaid, game_account.c_user));
                    });
                    if (0 < model_gaid.getSize()) getFieldToCombo(1).setEnabled(true);
                    else getFieldToCombo(1).setEnabled(false);
                } else getFieldToCombo(1).setEnabled(false);
                
                if (Service.map_product.isEmpty()
                        || Service.map_game_account.isEmpty()) {
                    disableSubmit();
                } else enableSubmit();
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

    @Override
    protected void submit() {
        if (((JRadioButton)select.getComponent(0)).isSelected()) {
            if (current.c_pass_a.equals(current.c_pass_curr)) setStatus("此游戏账号的当前密码已经是A密码");
            else {
                setStatus("操作成功");
            }
        } else if (((JRadioButton)select.getComponent(1)).isSelected()) {
            if (current.c_pass_b.equals(current.c_pass_curr)) setStatus("此游戏账号的当前密码已经是B密码");
            else {
                setStatus("操作成功");
            }
        } else if (((JRadioButton)select.getComponent(2)).isSelected()) {
            
        } else {
            setStatus("请先选择一项操作再执行提交");
        }
    }

}
