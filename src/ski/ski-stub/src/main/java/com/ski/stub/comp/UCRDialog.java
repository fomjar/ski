package com.ski.stub.comp;

import java.awt.Dimension;
import java.awt.Window;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import com.ski.stub.Service;
import com.ski.stub.bean.BeanChannelAccount;
import com.ski.stub.bean.BeanOrder;

public class UCRDialog extends JDialog {

    private static final long serialVersionUID = 5478304934566261533L;

    public UCRDialog(Window owner, BeanChannelAccount user) {
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(400, 500));
        setLocation(owner.getX() - (getWidth() - owner.getWidth()) / 2, owner.getY() - (getHeight() - owner.getHeight()) / 2);
        
        List<BeanOrder> orders = Service.map_order.values()
                .stream()
                .filter(order->{return order.i_caid == user.i_caid;})
                .collect(Collectors.toList());
        
        String text_buy         = generateBuy(orders);
        String text_recharge    = generateRecharge(orders);
        String text_rent        = generateRent(orders);
        String text_coupon      = generateCoupon(orders);
        String text_total       = generateTotal(orders);
        
        String text_main = String.format(
                  "用户 %s 的消费报告\n"
                + "========================================\n"
                + "【购买清单】\n"
                + "%s\n"
                + "【充值清单】\n"
                + "%s\n"
                + "【租赁清单】\n"
                + "%s\n"
                + "【优惠券】\n"
                + "%s\n"
                + "========================================\n"
                + "【总计】\n"
                + "%s",
                user.c_user,
                text_buy,
                text_recharge,
                text_rent,
                text_coupon,
                text_total);
        
        JEditorPane jep = new JEditorPane("text/html", text_main);
        JScrollPane jsp = new JScrollPane(jep);
        jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    }
    
    private static String generateBuy(List<BeanOrder> orders) {
        StringBuilder buf = new StringBuilder();
        float total = 0.0f;
        orders.forEach(order->{
            order.order_items.values().forEach(item->{
            });
        });
        return null;
    }
    
    private static String generateRecharge(List<BeanOrder> orders) {
        return null;
    }
    
    private static String generateRent(List<BeanOrder> orders) {
        return null;
    }
    
    private static String generateCoupon(List<BeanOrder> orders) {
        return null;
    }
    
    private static String generateTotal(List<BeanOrder> orders) {
        return null;
    }
}
