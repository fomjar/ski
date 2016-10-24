package com.ski.vcg.omc.comp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JToolBar;

import com.fomjar.widget.FjListPane;
import com.fomjar.widget.FjSearchBar;
import com.ski.vcg.common.CommonService;
import com.ski.vcg.common.bean.BeanGame;
import com.ski.vcg.omc.UIToolkit;

public class ListGame extends JDialog {

    private static final long serialVersionUID = -4974710094129285415L;

    private static ListGame instance = null;
    public static synchronized ListGame getInstance() {
        if (null == instance) instance = new ListGame();
        return instance;
    }

    private JToolBar                toolbar;
    private FjListPane<BeanGame>    pane;

    private ListGame() {
        super(MainFrame.getInstance(), "游戏清单");
        setModal(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(600, 600));
        Dimension owner = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((owner.width - getWidth()) / 2, (owner.height - getHeight()) / 2);

        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new JButton("新游戏"));

        pane = new FjListPane<BeanGame>();
        pane.enableSearchBar();
        pane.getSearchBar().setSearchTypes(new String[] {"游戏名", "标签名"});
        pane.getSearchBar().setSearchTips("键入关键字搜索");

        ((JButton) toolbar.getComponent(0)).addActionListener(e->{
            UIToolkit.createGame();
            refresh();
            ListGame.getInstance().refresh();
        });

        pane.getSearchBar().addSearchListener(new FjSearchBar.FjSearchAdapterForFjList<BeanGame>(pane.getList()) {
            @Override
            public boolean isMatch(String type, String[] words, BeanGame celldata) {
                switch (type) {
                case "游戏名": {
                    int count = 0;
                    for (String word : words) if (celldata.getDisplayName().toLowerCase().contains(word.toLowerCase())) count++;
                    return count == words.length;
                }
                case "标签名": {
                    return 0 < CommonService.getTagByType(CommonService.TAG_GAME)
                            .stream()
                            .filter(tag->{
                                int count = 0;
                                for (String word : words) if (tag.c_tag.toLowerCase().contains(word.toLowerCase())) count++;
                                return count == words.length;
                            })
                            .filter(tag->tag.i_instance == celldata.i_gid)
                            .count();
                }
                default:
                    return true;
                }
            }
        });

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(pane, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        pane.getList().removeAllCell();
        CommonService.getGameAll().values().forEach(data->pane.getList().addCell(new ListCellGame(data)));
    }

}
