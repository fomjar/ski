(function($) {

fomjar.framework.phase.append('dom', build_searchbar);  
fomjar.framework.phase.append('dom', build_content);  
fomjar.framework.phase.append('dom', build_menus);  
fomjar.framework.phase.append('ren', setup);  

function build_searchbar() {
    var bar = $('<div></div>');
    bar.addClass('index-searchbar');

    var cs  = $('<div></div>');
    cs.addClass('button button-major button-small');
    cs.text('分类 V');

    var inp = $('<input>');
    inp.attr('type', 'text');
    inp.attr('placeholder', '搜索');
    inp.bind('keydown', function() {if(13 == event.keyCode) setup();});

    bar.append([cs, inp]);

    $('.vcg .frame .body').append(bar);
}

function build_content() {
    var content = $('<div></div>');
    content.addClass('index-content');

    $('.vcg .frame .body').append(content);
}

function build_menus() {
    var m2 = null;
    var m1 = create_menu([
        {name : '最近上新'      , action : function() {window.location = 'query_game.html?key=tag&val=最近上新';}},
        {name : '十佳精彩游戏'  , action : function() {window.location = 'query_game.html?key=tag&val=十佳精彩游戏';}},
        {name : '速度与激情'    , action : function() {window.location = 'query_game.html?key=tag&val=速度与激情';}},
        {name : '游戏艺术'      , action : function() {window.location = 'query_game.html?key=tag&val=游戏艺术';}},
        {name : '按类型分 V'     , action : function() {
            if (null != m2) m2.remove();
            m2 = create_menu([
                {name : '动作'      , action : function() {window.location = 'query_game.html?key=category&val=动作';}},
                {name : '射击'      , action : function() {window.location = 'query_game.html?key=category&val=射击';}},
                {name : '角色扮演'  , action : function() {window.location = 'query_game.html?key=category&val=角色扮演';}},
                {name : '冒险'      , action : function() {window.location = 'query_game.html?key=category&val=冒险';}},
                {name : '赛车'      , action : function() {window.location = 'query_game.html?key=category&val=赛车';}},
                {name : '策略'      , action : function() {window.location = 'query_game.html?key=category&val=策略';}},
                {name : '格斗'      , action : function() {window.location = 'query_game.html?key=category&val=格斗';}},
                {name : '益智'      , action : function() {window.location = 'query_game.html?key=category&val=益智';}},
                {name : '恐怖'      , action : function() {window.location = 'query_game.html?key=category&val=恐怖';}},
                {name : '模拟'      , action : function() {window.location = 'query_game.html?key=category&val=模拟';}},
                {name : '休闲'      , action : function() {window.location = 'query_game.html?key=category&val=休闲';}},
                {name : '音乐'      , action : function() {window.location = 'query_game.html?key=category&val=音乐';}},
                {name : '体育'      , action : function() {window.location = 'query_game.html?key=category&val=体育';}},
                {name : '其他'      , action : function() {window.location = 'query_game.html?key=category&val=其他';}},
            ]);
            m2.css({'z-index' : '4', 'top' : '3em'});
            $('.vcg .frame .body').append(m2);
            setTimeout(function() {m2.css('top', '6em');}, 0);
        }},
        {name : '公司作品 V',     action : function() {
            if (null != m2) m2.remove();
            m2 = create_menu([
                {name : 'EA'                        , action : function() {window.location = 'query_game.html?key=vendor&val=Electronic_Arts';}},
                {name : 'CAPCOM(卡普空)'            , action : function() {window.location = 'query_game.html?key=vendor&val=CAPCOM';}},
                {name : 'Ubisoft(育碧)'             , action : function() {window.location = 'query_game.html?key=vendor&val=Ubisoft_Entertainment';}},
                {name : 'Bethesda'                  , action : function() {window.location = 'query_game.html?key=vendor&val=Bethesda_Softworks';}},
                {name : 'Naughty Dog(顽皮狗)'       , action : function() {window.location = 'query_game.html?key=vendor&val=Naughty_Dog';}},
                {name : 'Square-Enix'               , action : function() {window.location = 'query_game.html?key=vendor&val=SQUARE_ENIX';}},
                {name : 'Blizzard(动视暴雪)'        , action : function() {window.location = 'query_game.html?key=vendor&val=Blizzard_Entertainment';}},
                {name : 'SEGA(世嘉)'                , action : function() {window.location = 'query_game.html?key=vendor&val=SEGA_Games';}},
                {name : 'BANDAI NAMCO(百代南梦宫)'  , action : function() {window.location = 'query_game.html?key=vendor&val=BANDAI_NAMCO_Entertainment';}},
                {name : 'KONAMI(柯纳米)'            , action : function() {window.location = 'query_game.html?key=vendor&val=Konami_Digital';}},
            ]);
            m2.css({'z-index' : '4', 'top' : '3em'});
            $('.vcg .frame .body').append(m2);
            setTimeout(function() {m2.css('top', '6em');}, 0);
        }},
    ]);
    m1.css({'z-index' : '5', 'top' : '0px'});
    $('.vcg .frame .body').append(m1);
    $('.index-searchbar .button').bind('click', function() {
        if ('0px' == m1.css('top')) setTimeout(function() {m1.css('top', '3em');}, 0);
        else {
            if (null != m2) m2.remove();
            setTimeout(function() {m1.css('top', '0px');}, 0);
        }
    });
}

function create_menu(items) {
    var menu = $('<div></div>');
    menu.addClass('index-menu');
    var tr = $('<tr></tr>');
    $.each(items, function(i, mi) {
        var td = $('<td></td>');
        td.text(mi.name);
        td.bind('click', mi.action);
        tr.append(td);
    });
    var table = $('<table></table>');
    table.append(tr);
    menu.append(table);
    return menu;
}



function setup() {
    var word = $('input').val().replace(/(^\s*)|(\s*$)/g, '');
    if (0 == word.length) {
        $('.index-content').removeClass('list');
        preset();
    } else {
        $('.index-content').addClass('list');
        search(word);
    }
}

function preset() {
    var posters = [
        {img : 'res/十佳精彩游戏.jpg',
            url : 'query_game.html?key=tag&val=十佳精彩游戏'},
        {img : 'res/微信ONLY.jpg',
            url : 'query_game.html?key=tag&val=微信ONLY'},
        {img : 'res/大牌游戏.jpg',
            url : 'query_game.html?key=tag&val=大牌游戏'},
        {img : 'res/游戏艺术.jpg',
            url : 'query_game.html?key=tag&val=游戏艺术'},
        {img : 'res/动作游戏.jpg',
            url : 'query_game.html?key=category&val=动作'},
        {img : 'res/速度与激情.jpg',
            url : 'query_game.html?key=category&val=赛车'},
        {img : 'res/精彩的角色扮演游戏.jpg',
            url : 'query_game.html?key=category&val=角色扮演'}
    ];

    $('.index-content').html('');
    $.each(posters, function(i, data) {
        var poster = $('<div></div>')
        poster.addClass('index-poster');
        var poster_a = $('<a></a>');
        poster_a.attr('href', data.url);
        var poster_i = $('<img />');
        poster_i.attr('src', data.img);

        poster_a.append(poster_i);
        poster.append(poster_a);

        $('.index-content').append(poster);
    })
}

function search(word) {
    vcg.show_toast('正在获取...');
    $('.index-content').html('');
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_GAME, {word : word}, function(code, desc) {
        vcg.hide_toast();
        if (0 != code) {
            $('.index-content').text(desc);
            return;
        }

        $.each(desc, function(i, game) {
            var cell = vcg.create_list_cell_game(game);
            cell.bind('click', function() {window.location = 'query_game_by_gid.html?gid=' + game.gid.toString(16)});

            $('.index-content').append(cell);
        });
    });
}


})(jQuery)

