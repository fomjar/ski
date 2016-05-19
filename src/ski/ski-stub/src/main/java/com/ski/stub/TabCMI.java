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
        addField(CommonUI.createPanelLabelCombo("REPORT      (目标服务)", new String[] {
                "CDB    - 中央数据库",
                "GAME   - 游戏业务处理中心",
                "MMA    - 多媒体接入",
                "OMC    - 操作维护中心",
                "STUB   - 人工接口与消息桩",
                "WA     - WEB自动化",
                "WCA    - 微信接入",
                "WSI    - WEB服务接口"}));
        addField(CommonUI.createPanelLabelCombo("INSTRUCTION (请求指令)", Arrays.asList(SkiCommon.ISIS.class.getFields()).stream().map(field->{
            int     inst = -1;
            try {inst = field.getInt(null);} catch (Exception e) {e.printStackTrace();}
            String  name = field.getName();
            return String.format("0x%08X - %s", inst, name);
        }).collect(Collectors.toList()).toArray(new String[] {})));
        addField(input  = CommonUI.createPanelTitleField("请求消息体(JSON对象)", true));
        addField(output = CommonUI.createPanelTitleArea("响应消息", false));
        
        getFieldToCombo(0).setSelectedItem("CDB");
    }
    
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void submit() {
        JTextArea jta = (JTextArea) output.getViewport().getView();
        
        jta.append("================================[" + sdf.format(new Date()) + "]================================\n");
        
        FjMessage rsp = Service.send(getFieldToCombo(0).getSelectedItem().toString().split(" ")[0],
                Integer.parseInt(getFieldToCombo(1).getSelectedItem().toString().split(" ")[0].split("x")[1], 16),
                0 == input.getText().length() ? null : JSONObject.fromObject(input.getText()));
        if (null != rsp) {
            jta.append(rsp.toString().replace("\\t", "\t").replace("\\n", "\n") + "\n");
            jta.setSelectionStart(jta.getText().length());
            setStatus("操作成功");
        } else setStatus("操作失败，请重试");
        
    }

}
