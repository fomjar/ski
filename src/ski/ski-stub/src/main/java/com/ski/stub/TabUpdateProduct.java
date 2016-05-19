package com.ski.stub;

import javax.swing.DefaultComboBoxModel;

import com.ski.common.SkiCommon;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class TabUpdateProduct extends TabPaneBase {

    private static final long serialVersionUID = 4478338967110972515L;
    
    public TabUpdateProduct() {
        addField(CommonUI.createPanelLabelField("P       ID  (自动生成/自动生成)"));
        addField(CommonUI.createPanelLabelCombo("产品  类型  (整数)", new String[] {"0 - PS4游戏A租赁", "1 - PS4游戏B租赁", "2 - PS4主机租赁", "3 - PS4主机出售"}));
        addField(CommonUI.createPanelLabelCombo("实      例  (整数)", new String[] {}));
    }

    @Override
    protected void update() {
        Service.updateGame();
        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)getFieldToCombo(2).getModel();
        model.removeAllElements();
        
        if (!Service.map_game.isEmpty()) {
            Service.map_game.forEach((gid, game)->{
                model.addElement(String.format("0x%08X - %s(%s)", gid, game.c_name_zh, game.c_name_en));
            });
            if (0 < model.getSize()) {
                getFieldToCombo(2).setEnabled(true);
                enableSubmit();
            }
        } else {
            getFieldToCombo(2).setEnabled(false);
            disableSubmit();
        }
    }

    @Override
    protected void submit() {
        JSONObject args = new JSONObject();
        String pid          = getFieldToField(0).getText();
        if (0 < pid.length())   args.put("pid", pid);
        String prod_type    = getFieldToCombo(1).getSelectedItem().toString().split(" ")[0];
        args.put("prod_type",   Integer.parseInt(prod_type, 16));
        String prod_inst    = getFieldToCombo(2).getSelectedItem().toString().split(" ")[0].split("x")[1];
        args.put("prod_inst",   Integer.parseInt(prod_inst, 16));
        
        FjDscpMessage rsp = null;
        if (Service.isResponseSuccess(rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_PRODUCT, args)))
            setStatus("操作成功");
        else setStatus(rsp.args().toString());
    }

}
