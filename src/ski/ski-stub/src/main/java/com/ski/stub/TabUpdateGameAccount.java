package com.ski.stub;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.DefaultComboBoxModel;

import com.ski.common.SkiCommon;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class TabUpdateGameAccount extends TabPaneBase {

    private static final long serialVersionUID = 4478338967110972515L;
    
    public TabUpdateGameAccount() {
        addField(CommonUI.createPanelLabelCombo("G       ID  ", new String[] {}));
        addField(CommonUI.createPanelLabelField("GA      ID  (十六进制/自动生成)"));
        addField(CommonUI.createPanelLabelField("用  户  名  (字符串)"));
        addField(CommonUI.createPanelLabelField("密  码  A   (字符串)"));
        addField(CommonUI.createPanelLabelField("密  码  B   (字符串)"));
        addField(CommonUI.createPanelLabelCombo("当前  密码  (字符串)", new String[] {}));
        addField(CommonUI.createPanelLabelField("出生  日期  (yyyy-mm-dd)"));
        
        DefaultComboBoxModel<String> current = (DefaultComboBoxModel<String>) getFieldToCombo(5).getModel();
        KeyListener listener = new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                current.removeAllElements();
                current.addElement(getFieldToField(3).getText());
                current.addElement(getFieldToField(4).getText());
            }
        };
        getField(3).addKeyListener(listener);
        getField(4).addKeyListener(listener);
    }
    
    @Override
    protected void update() {
        Service.updateGames();
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)getFieldToCombo(0).getModel();
        model.removeAllElements();
        
        if (!Service.map_game.isEmpty()) {
            Service.map_game.forEach((gid, description)->{
                model.addElement(String.format("0x%08X - %s", gid, description));
            });
            if (0 < model.getSize()) {
                getFieldToCombo(0).setEnabled(true);
                enableSubmit();
            }
        } else {
            getFieldToCombo(0).setEnabled(false);
            disableSubmit();
        }
    }

    @Override
    protected void submit() {
        JSONObject args = new JSONObject();
        String gid          = getFieldToCombo(0).getSelectedItem().toString().split(" ")[0].split("x")[1];
        if (0 < gid.length())           args.put("gid",         Integer.parseInt(gid, 16));
        String gaid         = getFieldToField(1).getText();
        if (0 < gaid.length())          args.put("gaid",        gaid);
        String user         = getFieldToField(2).getText();
        if (0 < user.length())          args.put("user",        user);
        String pass_a       = getFieldToField(3).getText();
        if (0 < pass_a.length())        args.put("pass_a",      pass_a);
        String pass_b       = getFieldToField(4).getText();
        if (0 < pass_b.length())        args.put("pass_b",      pass_b);
        String pass_curr    = getFieldToCombo(5).getSelectedItem().toString();
        if (0 < pass_curr.length())     args.put("pass_curr",   pass_curr);
        String birth        = getFieldToField(6).getText();
        if (0 < birth.length())         args.put("birth",   birth);
        
        FjDscpMessage rsp = null;
        if (Service.isResponseSuccess(rsp = Service.send(SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT, args)))
            setStatus("操作成功");
        else setStatus(rsp.args().toString());
    }

}
