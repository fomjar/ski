package com.ski.vcg.omc.comp;

import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.NoSuchElementException;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.fomjar.widget.FjList;
import com.ski.vcg.common.CommonService;
import com.ski.vcg.common.bean.BeanChannelAccount;
import com.ski.vcg.common.bean.BeanCommodity;
import com.ski.vcg.common.bean.BeanPlatformAccount;
import com.ski.vcg.common.bean.BeanTicket;
import com.ski.vcg.omc.UIToolkit;

public class MainDetailPane extends JPanel {

    private static final long serialVersionUID = -8052693881269820372L;
    private BeanChannelAccount          user;

    private FjList<BeanChannelAccount>  basic;
    private FjList<BeanChannelAccount>  binds;
    private FjList<BeanCommodity>       rents;
    private FjList<BeanTicket>            tickets;
    private DetailPane                  pane_basic;
    private DetailPane                  pane_binds;
    private DetailPane                  pane_rents;
    private DetailPane                    pane_tickets;

    public MainDetailPane() {
        basic   = new FjList<BeanChannelAccount>();
        binds   = new FjList<BeanChannelAccount>();
        rents   = new FjList<BeanCommodity>();
        tickets = new FjList<BeanTicket>();
        pane_basic = new DetailPane("用户基本信息", basic,
                DetailPane.createToolBarButton("充值", e->UIToolkit.userMoney(CommonService.MONEY_CASH,    CommonService.getPlatformAccountByCaid(user.i_caid))),
                DetailPane.createToolBarButton("充券", e->UIToolkit.userMoney(CommonService.MONEY_COUPON,  CommonService.getPlatformAccountByCaid(user.i_caid))),
                DetailPane.createToolBarButton("退款", e->UIToolkit.userMoney(CommonService.MONEY_CONSUME, CommonService.getPlatformAccountByCaid(user.i_caid))),
                DetailPane.createToolBarButton("合并", e->UIToolkit.userMerge(CommonService.getPlatformAccountByCaid(user.i_caid))));
        pane_binds = new DetailPane("用户关联用户", binds);
        pane_rents = new DetailPane("用户在租游戏", rents,
                DetailPane.createToolBarButton("起租", e->{
                    UIToolkit.openCommodity(user.i_caid);
                    MainFrame.getInstance().setDetailUser(user.i_caid);
                }));
        pane_tickets = new DetailPane("用户关联工单", tickets,
                DetailPane.createToolBarButton("新工单", e->UIToolkit.createTicket(getUser())));

        JPanel panel = new JPanel();
        panel.setVisible(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(pane_basic);
        panel.add(pane_binds);
        panel.add(pane_rents);
        panel.add(pane_tickets);
        setLayout(new BorderLayout());
        add(panel, BorderLayout.NORTH);
    }

    public void setUser(int caid) {
        user = CommonService.getChannelAccountByCaid(caid);
        getComponent(0).setVisible(true);

        basic.removeAllCell();
        basic.addCell(new ListCellDetailUser(user));
        BeanPlatformAccount puser = CommonService.getPlatformAccountByPaid(CommonService.getPlatformAccountByCaid(caid));
        float[]     prestatement = CommonService.prestatementByCaid(user.i_caid);
        pane_basic.setTitle(String.format("用户基本信息 (当前余额：%.2f元/%.2f元|实时结算：%.2f元/%.2f元)", puser.i_cash, puser.i_coupon, prestatement[0], prestatement[1]));

        binds.removeAllCell();
        CommonService.getChannelAccountRelatedAll(user.i_caid)
                .stream()
                .filter(user2->user2.i_caid != user.i_caid)
                .forEach(user2->binds.addCell(new ListCellDetailUser(user2)));

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

        tickets.removeAllCell();
        CommonService.getTicketByPaid(CommonService.getPlatformAccountByCaid(getUser().i_caid)).forEach(t->{
            tickets.addCell(new ListCellDetailTicket(t));
        });
        pane_tickets.setTitle(String.format("用户关联工单 (%s)", getUserTicket(user)));

        revalidate();
    }

    public BeanChannelAccount getUser() {return user;}

    private static String getUserRent(BeanChannelAccount user) {
        int paid = CommonService.getPlatformAccountByCaid(user.i_caid);
        int renting = CommonService.getGameAccountByPaid(paid, CommonService.RENT_TYPE_A).size()
                + CommonService.getGameAccountByPaid(paid, CommonService.RENT_TYPE_B).size();
        int all = 0;
        try {all = CommonService.getOrderByPaid(paid).stream().map(order->order.commodities.size()).reduce(0, (c1, c2)->c1 + c2).intValue();}
        catch (NoSuchElementException e) {}
        return String.format("%d/%d", renting, all);
    }

    private static String getUserTicket(BeanChannelAccount user) {
        int paid = CommonService.getPlatformAccountByCaid(user.i_caid);
        int all = CommonService.getTicketByPaid(paid).size();
        int open = (int) CommonService.getTicketByPaid(paid).stream().filter(t->t.i_state == CommonService.TICKET_STATE_OPEN).count();
        return String.format("%d/%d", open, all);
    }

}
