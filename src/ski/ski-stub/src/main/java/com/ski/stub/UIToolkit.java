package com.ski.stub;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.UIManager;

import com.fomjar.widget.FjListCellString;
import com.fomjar.widget.FjListPane;
import com.fomjar.widget.FjSearchBar.FjSearchListener;
import com.ski.stub.bean.BeanGame;

public class UIToolkit {
    
    public static final Font FONT = new Font("仿宋", Font.PLAIN, 14);

    public static final Color COLOR_MODIFYING = Color.blue;
    
    static {
        UIManager.getLookAndFeelDefaults().put("Label.font",        FONT);
        UIManager.getLookAndFeelDefaults().put("Table.font",        FONT);
        UIManager.getLookAndFeelDefaults().put("TableHeader.font",  FONT);
        UIManager.getLookAndFeelDefaults().put("TextField.font",    FONT);
        UIManager.getLookAndFeelDefaults().put("TextArea.font",     FONT);
        UIManager.getLookAndFeelDefaults().put("TitledBorder.font", FONT);
        UIManager.getLookAndFeelDefaults().put("CheckBox.font",     FONT);
        UIManager.getLookAndFeelDefaults().put("RadioButton.font",  FONT);
        UIManager.getLookAndFeelDefaults().put("ComboBox.font",     FONT);
        UIManager.getLookAndFeelDefaults().put("Button.font",       FONT);
    }
    
    
    public static BeanGame chooseGame(Window window) {
        FjListPane<String> pane = new FjListPane<String>();
        // 启用搜索框
        pane.enableSearchBar();
        pane.getSearchBar().setSearchTips("键入关键词搜索");
        pane.getSearchBar().addSearchListener(new FjSearchListener() {
            @Override
            public void searchPerformed(String type, String[] words) {
                if (null == words || 0 == words.length) {
                    pane.getList().getCells().forEach(cell->cell.setVisible(true));
                    return;
                }
                pane.getList().getCells().forEach(cell->{
                    int count = 0;
                    for (String word : words) if (cell.getData().contains(word)) count++;
                    if (count == words.length) cell.setVisible(true);
                    else cell.setVisible(false);
                });
            }
        });
        
        // 创建弹框
        JDialog chooseGame = new JDialog(window, "选择游戏");
        chooseGame.setModal(true);
        chooseGame.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        chooseGame.setSize(400, 500);
        chooseGame.setLocation(window.getX() - (chooseGame.getWidth() - window.getWidth()) / 2,
                window.getY() - (chooseGame.getHeight() - window.getHeight()) / 2);
        chooseGame.getContentPane().setLayout(new BorderLayout());
        chooseGame.add(pane, BorderLayout.CENTER);
        
        Wrapper<BeanGame> wrapper = new Wrapper<BeanGame>();
        
        // 添加游戏列表
        Service.map_game.values().forEach(game->{
            FjListCellString cell = new FjListCellString(String.format("0x%08X - %s", game.i_gid, game.c_name_zh));
            cell.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int gid  = Integer.parseInt(cell.getData().split(" ")[0].split("x")[1], 16);
                    wrapper.obj = Service.map_game.get(gid);
                    chooseGame.dispose();
                }
            });
            pane.getList().addCell(cell);
        });
        
        chooseGame.setVisible(true);
        
        return wrapper.obj;
    }
    
    private static class Wrapper<E> {
        public E obj;
    }
}