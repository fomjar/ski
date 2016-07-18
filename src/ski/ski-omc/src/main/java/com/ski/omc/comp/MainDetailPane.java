package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.fomjar.widget.FjList;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.common.bean.BeanCommodity;
import com.ski.common.bean.BeanPlatformAccount;
import com.ski.omc.UIToolkit;

public class MainDetailPane extends JPanel {

    private static final long serialVersionUID = -8052693881269820372L;
    private BeanChannelAccount          user;
    
    private FjList<BeanChannelAccount>  basic;
    private FjList<BeanChannelAccount>  binds;
    private FjList<BeanCommodity>       rents;
    private DetailPane                  pane_basic;
    private DetailPane                  pane_binds;
    private DetailPane                  pane_rents;
    
    public MainDetailPane() {
        basic   = new FjList<BeanChannelAccount>();
        binds   = new FjList<BeanChannelAccount>();
        rents   = new FjList<BeanCommodity>();
        pane_basic = new DetailPane("用户基本信息", basic,
                DetailPane.createToolBarButton("充值", e->UIToolkit.userRecharge(CommonService.MONEY_CASH,    CommonService.getPlatformAccountByCaid(user.i_caid))),
                DetailPane.createToolBarButton("充券", e->UIToolkit.userRecharge(CommonService.MONEY_COUPON,  CommonService.getPlatformAccountByCaid(user.i_caid))),
                DetailPane.createToolBarButton("退款", e->UIToolkit.userRecharge(CommonService.MONEY_CONSUME, CommonService.getPlatformAccountByCaid(user.i_caid))),
                DetailPane.createToolBarButton("关联", e->UIToolkit.userBind(user)));
        pane_binds = new DetailPane("用户关联用户", binds);
        pane_rents = new DetailPane("用户在租游戏", rents,
                DetailPane.createToolBarButton("起租", e->{
                    UIToolkit.openCommodity(user.i_caid);
                    MainFrame.getInstance().setDetailUser(user.i_caid);
                }));
        
        JPanel panel = new JPanel();
        panel.setVisible(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(pane_basic);
        panel.add(pane_binds);
        panel.add(pane_rents);
        setLayout(new BorderLayout());
        add(panel, BorderLayout.NORTH);
    }
    
    public void setUser(int caid) {
        user = CommonService.getChannelAccountByCaid(caid);
        getComponent(0).setVisible(true);
        
        basic.removeAllCell();
        basic.addCell(new ListCellDetailUser(user));
        BeanPlatformAccount puser = CommonService.getPlatformAccountByPaid(CommonService.getPlatformAccountByCaid(caid));
        float[]     prestatement = CommonService.prestatement(user.i_caid);
        pane_basic.setTitle(String.format("用户基本信息 (当前余额：%.2f元/%.2f元|实时结算：%.2f元/%.2f元)", puser.i_cash, puser.i_coupon, prestatement[0], prestatement[1]));
        
        binds.removeAllCell();
        CommonService.getChannelAccountRelatedAll(user.i_caid).forEach(user2->{
            if (user2.i_caid == user.i_caid) return;
            
            binds.addCell(new ListCellDetailUser(user2));
        });
        
        rents.removeAllCell();
        CommonService.getChannelAccountRelatedAll(user.i_caid).forEach(user2->{
            CommonService.getOrderByCaid(user2.i_caid).forEach(order->{
                order.commodities.values()
                        .stream()
                        .sorted((c1, c2)->{
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {return - (int) (sdf.parse(c1.t_begin).getTime() - sdf.parse(c2.t_begin).getTime());}
                            catch (Exception e) {e.printStackTrace();}
                            return 0;
                        })
                        .forEach(c->{
                            rents.addCell(new ListCellDetailCommodity(c));
                        });
            });
        });
        pane_rents.setTitle(String.format("用户在租游戏 (%s)", getUserRent(user)));
        
        revalidate();
    }
    
    public BeanChannelAccount getUser() {return user;}
    
    private static String getUserRent(BeanChannelAccount data) {
        int renting = CommonService.getRentGameAccountByCaid(data.i_caid, CommonService.RENT_TYPE_A).size()
                + CommonService.getRentGameAccountByCaid(data.i_caid, CommonService.RENT_TYPE_B).size();
        int all = 0;
        try {all = CommonService.getOrderByCaid(data.i_caid).stream().map(order->order.commodities.size()).reduce(0, (c1, c2)->c1 + c2).intValue();}
        catch (NoSuchElementException e) {}
        return String.format("%d/%d", renting, all);
    }

}
