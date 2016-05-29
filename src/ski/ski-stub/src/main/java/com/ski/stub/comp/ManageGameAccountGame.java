package com.ski.stub.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import com.fomjar.widget.FjList;
import com.fomjar.widget.FjListCellString;
import com.ski.common.SkiCommon;
import com.ski.stub.Service;
import com.ski.stub.UIToolkit;
import com.ski.stub.bean.BeanGame;
import com.ski.stub.bean.BeanGameAccount;

import fomjar.server.msg.FjDscpMessage;
import net.sf.json.JSONObject;

public class ManageGameAccountGame extends JDialog {

    private static final long serialVersionUID = -51034836551447291L;
    
    private final BeanGameAccount account;
    private JToolBar toolbar;
    private FjList<String> list;
    
    public ManageGameAccountGame(Window window, BeanGameAccount account) {
        super(window);
        this.account = account;
        toolbar = new JToolBar();
        toolbar.add(new JButton("添加游戏"));
        toolbar.setFloatable(false);
        list = new FjList<String>();
        list.setBorder(BorderFactory.createTitledBorder("此账号下的游戏"));
        
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(list, BorderLayout.CENTER);
        
        setTitle("管理账号“" + account.c_user + "”");
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(400, 200));
        setLocation(window.getX() - (getWidth() - window.getWidth()) / 2, window.getY() - (getHeight() - window.getHeight()) / 2);
        
        registerListener();
        
        updateAll();
    }
    
    private void registerListener() {
        ((JButton) toolbar.getComponent(0)).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BeanGame game = UIToolkit.chooseGame(ManageGameAccountGame.this);
                if (null == game) return;
                
                int gaid = account.i_gaid;
                int gid  = game.i_gid;
                JSONObject args = new JSONObject();
                args.put("gaid", gaid);
                args.put("gid", gid);
                FjDscpMessage rsp = Service.send("cdb", SkiCommon.ISIS.INST_ECOM_UPDATE_GAME_ACCOUNT_GAME, args);
                JOptionPane.showConfirmDialog(ManageGameAccountGame.this, rsp.toString(), "服务器响应", JOptionPane.CLOSED_OPTION);
                
                Service.updateGameAccountGame();
                updateAll();
            }
        });
    }
    
    private void updateAll() {
        list.removeAllCell();
        Service.set_game_account_game.forEach(pair->{
            if (account.i_gaid == pair.i_gaid) {
                BeanGame game = Service.map_game.get(pair.i_gid);
                list.addCell(new FjListCellString(String.format("0x%08X - %s", game.i_gid, game.c_name_zh)));
            }
        });
    }
    
}
