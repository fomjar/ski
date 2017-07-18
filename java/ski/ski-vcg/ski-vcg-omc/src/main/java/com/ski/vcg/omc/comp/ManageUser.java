package com.ski.vcg.omc.comp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.fomjar.widget.FjEditLabel;
import com.ski.vcg.common.CommonDefinition;
import com.ski.vcg.common.CommonService;
import com.ski.vcg.common.bean.BeanChannelAccount;
import com.ski.vcg.omc.UIToolkit;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ManageUser extends JDialog {

    private static final long serialVersionUID = -1539252669784510561L;

    private BeanChannelAccount  user;
    private FjEditLabel         i_caid;
    private FjEditLabel         c_user;
    private FjEditLabel         i_channel;
    private FjEditLabel         c_name;
    private FjEditLabel         i_gender;
    private FjEditLabel         c_phone;
    private FjEditLabel         c_address;
    private FjEditLabel         c_zipcode;
    private FjEditLabel         t_birth;

    public ManageUser(int caid) {
        super(MainFrame.getInstance());

        user = CommonService.getChannelAccountByCaid(caid);

        i_caid      = new FjEditLabel(false);
        i_caid.setForeground(Color.gray);
        c_user      = new FjEditLabel();
        i_channel   = new FjEditLabel(false);
        c_name      = new FjEditLabel();
        i_gender    = new FjEditLabel();
        c_phone     = new FjEditLabel();
        c_address   = new FjEditLabel();
        c_zipcode   = new FjEditLabel();
        t_birth     = new FjEditLabel();

        JPanel panel_basic = new JPanel();
        panel_basic.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel_basic.setLayout(new BoxLayout(panel_basic, BoxLayout.Y_AXIS));
        panel_basic.add(UIToolkit.createBasicInfoLabel("用户编号", i_caid));
        panel_basic.add(UIToolkit.createBasicInfoLabel("账    号", c_user));
        panel_basic.add(UIToolkit.createBasicInfoLabel("来源平台", i_channel));
        panel_basic.add(UIToolkit.createBasicInfoLabel("姓    名", c_name));
        panel_basic.add(UIToolkit.createBasicInfoLabel("性    别", i_gender));
        panel_basic.add(UIToolkit.createBasicInfoLabel("电    话", c_phone));
        panel_basic.add(UIToolkit.createBasicInfoLabel("地    址", c_address));
        panel_basic.add(UIToolkit.createBasicInfoLabel("邮    编", c_zipcode));
        panel_basic.add(UIToolkit.createBasicInfoLabel("出生日期", t_birth));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel_basic, BorderLayout.NORTH);

        setTitle(String.format("用户基本信息 - %s", user.getDisplayName()));
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setSize(new Dimension(300, getHeight()));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);

        listen();

        updateBasicPane();
    }

    private JSONObject args = new JSONObject();

    private void listen() {
        c_user.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("caid", user.i_caid);
                args.put("user", new_value);
                updateChannelAccount();
                args.clear();
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_name.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("caid", user.i_caid);
                args.put("name", new_value);
                updateChannelAccount();
                args.clear();
            }
            @Override
            public void cancelEdit(String value) {}
        });
        i_gender.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("caid", user.i_caid);
                args.put("gender", "女".equals(new_value) ? 0 : "男".equals(new_value) ? 1 : 2);
                updateChannelAccount();
                args.clear();
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_phone.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("caid", user.i_caid);
                args.put("phone", new_value);
                updateChannelAccount();
                args.clear();
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_address.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("caid", user.i_caid);
                args.put("address", new_value);
                updateChannelAccount();
                args.clear();
            }
            @Override
            public void cancelEdit(String value) {}
        });
        c_zipcode.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("caid", user.i_caid);
                args.put("zipcode", new_value);
                updateChannelAccount();
                args.clear();
            }
            @Override
            public void cancelEdit(String value) {}
        });
        t_birth.addEditListener(new FjEditLabel.EditListener() {
            @Override
            public void startEdit(String value) {}
            @Override
            public void finishEdit(String old_value, String new_value) {
                args.put("caid", user.i_caid);
                args.put("birth", new_value);
                updateChannelAccount();
                args.clear();
            }
            @Override
            public void cancelEdit(String value) {}
        });
    }

    private void updateChannelAccount() {
        if (args.isEmpty()) {
            JOptionPane.showMessageDialog(ManageUser.this, "没有可更新的内容", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        FjDscpMessage rsp = CommonService.send("cdb", CommonDefinition.ISIS.INST_ECOM_UPDATE_CHANNEL_ACCOUNT, args);
        UIToolkit.showServerResponse(rsp);
    }

    private void updateBasicPane() {
        user = CommonService.getChannelAccountByCaid(user.i_caid);

        i_caid.setText(String.format("0x%08X", user.i_caid));
        c_user.setText(user.c_user);
        i_channel.setText(getChannel2String(user.i_channel));
        c_name.setText(0 == user.c_name.length() ? "(没有姓名)" : user.c_name);
        i_gender.setText(CommonService.GENDER_FEMALE == user.i_gender ? "女" : CommonService.GENDER_MALE == user.i_gender ? "男" : "人妖");
        c_phone.setText(0 == user.c_phone.length() ? "(没有电话)" : user.c_phone);
        c_address.setText(0 == user.c_address.length() ? "(没有地址)" : user.c_address);
        c_zipcode.setText(0 == user.c_zipcode.length() ? "(没有邮编)" : user.c_zipcode);
        t_birth.setText(0 == user.t_birth.length() ? "(没有生日)" : user.t_birth);
    }

    private static String getChannel2String(int channel) {
        switch (channel) {
        case CommonService.CHANNEL_TAOBAO: return "淘  宝";
        case CommonService.CHANNEL_WECHAT: return "微  信";
        case CommonService.CHANNEL_ALIPAY: return "支付宝";
        default: return "未  知";
        }
    }

}
