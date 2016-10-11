
fomjar.framework.phase.append('dom', build_list);
fomjar.framework.phase.append('ren', setup);

function build_list() {
    var list = $('<div></div>');
    list.addClass('wechat-list');

    $('.wechat-frame-content').append(list);
}

function setup() {
    var key = fomjar.util.args().key;
    var val = fomjar.util.args().val;

    var title = val;
    title = title.replace(/_/g, ' ');
    if (0 == title.length) title = '所有游戏';
    fomjar.util.title(title);

    wechat.show_toast('正在加载...');
    var data = {};
    data[key] = val;
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_GAME, data, function(code, desc) {
        wechat.hide_toast();
        $.each(desc, function(i, game) {
            var cell = wechat.create_list_cell_game(game);
            cell.bind('click', function() {window.location = 'query_game_by_gid.html?gid=' + game.gid.toString(16);});

            $('.wechat-list').append(cell);
        });
    });

}


