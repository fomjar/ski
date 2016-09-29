define(['jquery', 'jquery.json', 'jquery.cookie', 'jquery-ui', 'weui', 'weui.ext'],
function($) {

    $(document).ready(function() {
        setup();

        var toggle_categoires = function() {
            if ($('#categories').css('top').startsWith('-')) {
                $('.weui_mask_transparent').show(); 
                $('#categories').css({opacity : 0, top : '-'+$('#categories').height()+'px'});
                $('#categories').animate({opacity : 1, top : ($('#search_bar').outerHeight(true)-1)+'px'}, 'fast');
            } else {
                $('.weui_mask_transparent').hide(); 
                $('#categories').animate({opacity : '0', top : '-'+$('#categories').height()+'px'}, 'fast');
            }
        };
        $('.weuix-searchbar-btn').bind('click', toggle_categories);
        $('.weui_mask_transparent').bind('click', toggle_categories);

        $('.weui_search_input').bind('focus', function() {
            $('#search_bar').addClass('weui_search_focusing');
        });
        $('.weui_search_input').bind('blur', function() {
            if (0 == $('.weui_search_input').val().length) {
                $('#search_bar').removeClass('weui_search_focusing');
            }
        });
        $('.weui_search_input').bind('keyup', function(event) {
            if(13 == event.keyCode) setup();
        });

        $('.weui_search_inner.weui_icon_clear').bind('click', function() {
            $('.weui_search_input').val('');
            $('.weui_search_input').focus();
            setup();
        });
        $('.weui_search_cancel').bind('click', function() {
            $('.weui_search_input').val('');
            $('.weui_search_input').blur();
            setup();
        });
        $('#categories').menu();
    });

    function preset() {
        $('#result').html(
            "<div class='weuix-poster_big'><a href='query_game.html?key=tag&val=十佳精彩游戏'>  <img width='100%' src='res/十佳精彩游戏.jpg'/></a></div>"
          + "<div class='weuix-poster_big'><a href='query_game.html?key=tag&val=微信ONLY'>      <img width='100%' src='res/微信ONLY.jpg'/></a></div>"
          + "<div class='weuix-poster_big'><a href='query_game.html?key=tag&val=大牌游戏'>      <img width='100%' src='res/大牌游戏.jpg'/></a></div>"
          + "<div class='weuix-poster_big'><a href='query_game.html?key=tag&val=游戏艺术'>      <img width='100%' src='res/游戏艺术.jpg'/></a></div>"
          + "<div class='weuix-poster_big'><a href='query_game.html?key=category&val=动作'>     <img width='100%' src='res/动作游戏.jpg'/></a></div>"
          + "<div class='weuix-poster_big'><a href='query_game.html?key=category&val=赛车'>     <img width='100%' src='res/速度与激情.jpg'/></a></div>"
          + "<div class='weuix-poster_big'><a href='query_game.html?key=category&val=角色扮演'> <img width='100%' src='res/精彩的角色扮演游戏.jpg'/></a></div>"
        );
    }
    function search(word) {
        ski.ui.show_toast('正在搜索');
        ski.common.query_game({word : word}, function(args) {
            ski.ui.hide_toast();
            if (0 != args.code) {
                ski.ui.show_dialog_alert({
                    head : "搜索失败",
                    body : args.desc
                });
                return;
            }
            var games = args.desc;
            var title = document.createElement('div');
            title.className = 'weui_panel_hd';
            title.innerText = '搜索结果';
            var cells = document.createElement('div');
            cells.className = 'weui_panel_bd';
            for (var i in games) {
                var game = games[i];
                var cell = "<a class='weui_media_box weui_media_appmsg' href=\"javascript: ski.goto.game(" + game.gid + ");\">"
                             + "<div class='weui_media_hd'><img class='weui_media_appmsg_thumb' src='" + game.url_icon + "'></div>"
                             + "<div class='weui_media_bd'>"
                                + "<h4 class='weui_media_title'>" + game.name_zh_cn + "</h4>"
                                + "<p class='weui_media_desc'>" + game.introduction + "</p>"
                             + "</div>"
                             + "<div style='position: absolute; width: 70px; top: 20px; right: 20px'><table width='100%'><tr>"
                                 + "<td><div class='" + (game.rent_avail_a ? "game-rent_enable" : "game-rent_disable") + "'>认证</div></td>"
                                 + "<td><div class='" + (game.rent_avail_b ? "game-rent_enable" : "game-rent_disable") + "'>不认证</div></td>"
                             + "</tr></table></div>"
                         + "</a>";
                cells.innerHTML = cells.innerHTML + cell;
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


