define(['jquery', 'fomjar', 'ski', 'weui'],
function($, fomjar, ski){

$(document).ready(function() {
    init();
    setup();
});

function init() {
    $('#search_clear').bind('click', function() {
        $('#search_input').val('');
        $('#search_input').focus();
        setup();
    });
    $('#search_input')
            .bind('focus', function() {
                 $('#search_bar').addClass('weui_search_focusing');
            })
            .bind('blur', function() {
                if (0 == $('#search_input').val().length)
                    $('#search_bar').removeClass('weui_search_focusing');
            })
            .bind('keyup', function() {
                if(13 == event.keyCode) setup();
            });
}

function preset() {
    $('#result').html(
        "<div style='margin: 10px; margin-bottom: 0px'><a href='/wechat/query_game_by_tag.html?tag=十佳精彩游戏'>       <img width='100%' src='res/十佳精彩游戏.jpg'/></a></div>"
      + "<div style='margin: 10px; margin-bottom: 0px'><a href='/wechat/query_game_by_tag.html?tag=微信ONLY'>           <img width='100%' src='res/微信ONLY.jpg'/></a></div>"
      + "<div style='margin: 10px; margin-bottom: 0px'><a href='/wechat/query_game_by_tag.html?tag=大牌游戏'>           <img width='100%' src='res/大牌游戏.jpg'/></a></div>"
      + "<div style='margin: 10px; margin-bottom: 0px'><a href='/wechat/query_game_by_tag.html?tag=游戏艺术'>           <img width='100%' src='res/游戏艺术.jpg'/></a></div>"
      + "<div style='margin: 10px; margin-bottom: 0px'><a href='/wechat/query_game_by_category.html?category=动作'>     <img width='100%' src='res/动作游戏.jpg'/></a></div>"
      + "<div style='margin: 10px; margin-bottom: 0px'><a href='/wechat/query_game_by_category.html?category=赛车'>     <img width='100%' src='res/速度与激情.jpg'/></a></div>"
      + "<div style='margin: 10px; margin-bottom: 0px'><a href='/wechat/query_game_by_category.html?category=角色扮演'> <img width='100%' src='res/精彩的角色扮演游戏.jpg'/></a></div>"
    );
}

function search(word) {
    ski.ui.show_toast('正在搜索');
    ski.common.query_game({word : word}, function(code, desc) {
        ski.ui.hide_toast();
        if (0 != code) {
            ski.ui.show_dialog_alert({
                head : "搜索失败",
                body : desc
            });
            return;
        }
        var games = desc;
        var title = document.createElement('div');
        title.className = 'weui_panel_hd';
        title.innerText = '搜索结果';

        var cells = $('<div></div>');
        cells.className = 'weui_panel_bd';
        for (var i in games) {
            var game = games[i];
            var cell = ski.ui.cell_game(game);
            cells.append(cell);
        }
        $('#result').html(title);
        $('#result').append(cells);
    });
}

function setup() {
    var word   = $('#search_input').val().replace(/(^\s*)|(\s*$)/g, "");

    if (0 == word.length) {
        preset();
    } else {
        search(word);
    }

}
});
