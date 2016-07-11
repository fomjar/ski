package com.ski.omc.comp;

import java.awt.BorderLayout;
import java.text.SimpleDateFormat;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.fomjar.widget.FjList;
import com.ski.common.CommonService;
import com.ski.common.bean.BeanChannelAccount;
import com.ski.common.bean.BeanCommodity;
import com.ski.omc.UIToolkit;

public class UserDetail extends JPanel {

    private static final long serialVersionUID = -8052693881269820372L;
    private BeanChannelAccount          user;
    
    private FjList<BeanChannelAccount>  basic;
    private FjList<BeanChannelAccount>  binds;
    private FjList<BeanCommodity>       rents;
    
    public UserDetail() {
        basic   = new FjList<BeanChannelAccount>();
        binds   = new FjList<BeanChannelAccount>();
        rents   = new FjList<BeanCommodity>();
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(UIToolkit.createDetailArea("用户基本信息", basic,
                UIToolkit.createDetailButton("充值", e->{
                    if (null != user) UIToolkit.userRecharge(CommonService.getPlatformAccountByCaid(user.i_caid));
                }),
                UIToolkit.createDetailButton("关联", e->{
                    if (null != user) UIToolkit.userBind(user);
                })));
        panel.add(UIToolkit.createDetailArea("用户关联用户", binds));
        panel.add(UIToolkit.createDetailArea("用户在租游戏", rents,
                UIToolkit.createDetailButton("起租", e->{
                    if (null != user) {
                        UIToolkit.openCommodity(user.i_caid);
                        MainFrame.getInstance().setDetailUser(user.i_caid);
                    }
                })));
        setLayout(new BorderLayout());
        add(panel, BorderLayout.NORTH);
    }
    
    public void setUser(int caid) {
        this.user = CommonService.getChannelAccountByCaid(caid);
        
        basic.removeAllCell();
        basic.addCell(new ListCellDetailUser(user));
        
        binds.removeAllCell();
        CommonService.getChannelAccountRelated(user.i_caid).forEach(user2->{
            if (user2.i_caid == user.i_caid) return;
            
            binds.addCell(new ListCellDetailUser(user2));
        });
        
        rents.removeAllCell();
        CommonService.getChannelAccountRelated(user.i_caid).forEach(user2->{
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
        
        revalidate();
    }
    
    public BeanChannelAccount getUser() {return user;}
    
}
