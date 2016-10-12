
fomjar.framework.phase.append('dom', build_searchbar);  
fomjar.framework.phase.append('dom', build_content);  
fomjar.framework.phase.append('ren', setup);  

function build_searchbar() {
    var bar = $('<div></div>');
    bar.addClass('index-searchbar');

    var cs  = $('<div></div>');
    cs.addClass('button button-major button-small');
    cs.text('分类');

    var inp = $('<input>');
    inp.attr('type', 'text');
    inp.attr('placeholder', '搜索');
    inp.bind('keydown', function() {if(13 == event.keyCode) setup();});

    bar.append([cs, inp]);

    $('.wechat .frame .body').append(bar);
}

function build_content() {
    var content = $('<div></div>');
    content.addClass('index-content');

    $('.wechat .frame .body').append(content);
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
    wechat.show_toast('正在获取...');
    $('.index-content').html('');
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_GAME, {word : word}, function(code, desc) {
        wechat.hide_toast();
        if (0 != code) {
            $('.index-content').text(desc);
            return;
        }

        $.each(desc, function(i, game) {
            var cell = wechat.create_list_cell_game(game);
            cell.bind('click', function() {window.location = 'query_game_by_gid.html?gid=' + game.gid.toString(16)});

            $('.index-content').append(cell);
        });
    });
}

