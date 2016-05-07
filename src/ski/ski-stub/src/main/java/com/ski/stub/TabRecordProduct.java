package com.ski.stub;

public class TabRecordProduct extends TabPaneBase {

    private static final long serialVersionUID = 4478338967110972515L;
    
    public TabRecordProduct() {
        addField(CommonUI.createPanelLabelField("P       ID  (自动生成)"));
        addField(CommonUI.createPanelLabelCombo("产品  类型  (整数)", new String[] {"PS4游戏A租赁", "PS4游戏B租赁", "PS4主机租赁", "PS4主机出售"}));
        addField(CommonUI.createPanelLabelCombo("实      例  (整数)", new String[] {}));
    }

}
