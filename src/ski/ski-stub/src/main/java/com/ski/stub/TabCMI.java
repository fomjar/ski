package com.ski.stub;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.ski.common.SkiCommon;

import fomjar.server.FjMessage;
import net.sf.json.JSONObject;

public class TabCMI extends TabPaneBase {

    private static final long serialVersionUID = -6109753399383105070L;
    
    private JTextField  input;
    private JScrollPane output;
    
    public TabCMI() {
        addField(CommonUI.createPanelLabelCombo("REPORT      (目标服务)", new String[] {"cdb", "game", "mma", "omc", "wa", "wca", "wsi"}));
        addField(CommonUI.createPanelLabelCombo("INSTRUCTION (请求指令)", Arrays.asList(SkiCommon.ISIS.class.getFields()).stream().map(field->{
            int     inst = -1;
            try {inst = field.getInt(null);} catch (Exception e) {e.printStackTrace();}
            String  name = field.getName();
            return String.format("0x%08X - %s", inst, name);
        }).collect(Collectors.toList()).toArray(new String[] {})));
        addField(input  = CommonUI.createPaneTitleField("请求消息体(JSON对象)"));
        addField(output = CommonUI.createPaneTitleArea("响应消息", false));

        getFieldToCombo(0).setSelectedItem("cdb");
    }
    
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @Override
    protected void submit() {
        JTextArea jta = (JTextArea) output.getViewport().getView();
        
        jta.append("================================[" + sdf.format(new Date()) + "]================================\n");
        
        FjMessage rsp = Service.send(getFieldToCombo(0).getSelectedItem().toString(),
                Integer.parseInt(getFieldToCombo(1).getSelectedItem().toString().split(" ")[0].split("x")[1], 16),
                0 == input.getText().length() ? null : JSONObject.fromObject(input.getText()));
        if (null != rsp) {
            jta.append(rsp.toString().replace("\\t", "\t").replace("\\n", "\n") + "\n");
        }
        
        jta.setSelectionStart(jta.getText().length());
    }

}
