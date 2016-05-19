package com.ski.stub;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;

import com.ski.common.SkiCommon;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class TabUpdateGameAccountRent extends TabPaneBase {

    private static final long serialVersionUID = 4478338967110972515L;
    
    public TabUpdateGameAccountRent() {
        addField(CommonUI.createPanelLabelCombo("P       ID  (整数)", new String[] {}));
        addField(CommonUI.createPanelLabelCombo("GA      ID  (整数)", new String[] {}));
        addField(CommonUI.createPanelLabelCombo("CA      ID  (整数)", new String[] {}));
        addField(CommonUI.createPanelLabelCombo("租赁  状态  (整数)", new String[] {"0 - 空闲", "1 - 租用", "2 - 锁定"}));
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
                        || Service.map_game_account.isEmpty()
                        || Service.map_channel_account.isEmpty()) {
                    disableSubmit();
                } else enableSubmit();
            }
        });
    }

    @Override
    protected void update() {
        Service.updateProduct();
        Service.updateChannelAccount();
        
        DefaultComboBoxModel<String> model_pid = (DefaultComboBoxModel<String>)getFieldToCombo(0).getModel();
        model_pid.removeAllElements();
        
        if (!Service.map_product.isEmpty()) {
            Service.map_product.forEach((pid, product)->{
                model_pid.addElement(String.format("0x%08X", pid));
            });
            if (0 < model_pid.getSize()) getFieldToCombo(0).setEnabled(true);
            else {
                getFieldToCombo(0).setEnabled(false);
                getFieldToCombo(1).setEnabled(false);
            }
        } else {
            getFieldToCombo(0).setEnabled(false);
            getFieldToCombo(1).setEnabled(false);
        }
        
        DefaultComboBoxModel<String> model_caid = (DefaultComboBoxModel<String>)getFieldToCombo(2).getModel();
        model_caid.removeAllElements();
        
        if (!Service.map_channel_account.isEmpty()) {
            Service.map_channel_account.forEach((caid, channel_account)->{
                model_caid.addElement(String.format("0x%08X - %s", caid, channel_account.c_user));
            });
            if (0 < model_caid.getSize()) getFieldToCombo(2).setEnabled(true);
            else getFieldToCombo(2).setEnabled(false);
        } else getFieldToCombo(2).setEnabled(false);
        
        if (Service.map_product.isEmpty()
                || Service.map_game_account.isEmpty()
                || Service.map_channel_account.isEmpty()) {
            disableSubmit();
        } else enableSubmit();
    }

    @Override
    protected void submit() {
        JSONObject args = new JSONObject();
        String pid          = getFieldToCombo(0).getSelectedItem().toString().split(" ")[0].split("x")[1];
        args.put("pid",     Integer.parseInt(pid, 16));
        String gaid         = getFieldToCombo(1).getSelectedItem().toString().split(" ")[0].split("x")[1];
        args.put("gaid",    Integer.parseInt(gaid, 16));
        String caid         = getFieldToCombo(2).getSelectedItem().toString().split(" ")[0].split("x")[1];
        args.put("caid",    Integer.parseInt(caid, 16));
        String state        = getFieldToCombo(3).getSelectedItem().toString().split(" ")[0];
        args.put("state",   Integer.parseInt(state, 16));
        
        FjDscpMessage rsp = null;
        if (Service.isResponseSuccess(rsp = Service.send(SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT_RENT, args)))
            setStatus("操作成功");
        else setStatus(rsp.args().toString());
    }

}
