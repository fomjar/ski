(function($) {

fomjar.framework.phase.append('dom', build_list);
fomjar.framework.phase.append('dom', build_buttons);
fomjar.framework.phase.append('ren', setup);

function build_list() {
    var list = $('<div></div>');
    list.addClass('list');
    
    var tips = $('<div></div>');
    tips.addClass('tips');
    tips.html('体验类型说明：<br/>认证：即认证为常用主机，有奖杯<br/>不认证：即不认证为常用主机，无奖杯');

    var cell_game = $('<div></div>');
    cell_game.addClass('cell-kv');
    cell_game.append('<div>体验游戏</div>');
    cell_game.append("<div id='game'></div>");
    
    var cell_type = $('<div></div>');
    cell_type.addClass('cell-kv');
    cell_type.append('<div>体验类型</div>');
    cell_type.append("<select></select>");

    list.append([cell_game, cell_type]);

    $('.wechat .frame .body').append(tips);
    $('.wechat .frame .body').append(list);
}

function build_buttons() {
    var button = $('<div></div>');
    button.addClass('button button-major button-large');
    button.text('开始体验');
    button.bind('click', apply);

    $('.wechat .frame .body').append(button);
}

function setup() {
    var gid = fomjar.util.args().gid;
    wechat.show_toast('正在加载...');
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_GAME, {gid : gid}, function(code, desc) {
        wechat.hide_toast();
        if (0 != code) {
            wechat.show_toast('加载失败', 10000);
            return;
        }
        var game = desc;
        $('#game').text(game.name_zh_cn);
        if (game.rent_avail_a) $('select').append("<option value='0'>认证 - " + game.rent_price_a + "元/天</option>");
        if (game.rent_avail_b) $('select').append("<option value='1'>不认证 - " + game.rent_price_b + "元/天</option>");
        if (!game.rent_avail_a && !game.rent_avail_b) $('.button').hide();
    });
}

function apply() {
    var gid = fomjar.util.args().gid;
    wechat.show_toast('正在操作...');
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_APPLY_RENT_BEGIN, {gid : gid, type : $('select').val()}, function(code, desc) {
        wechat.hide_toast();
        if (0 != code) {
            wechat.show_toast('操作失败: '+desc, 10000);
            return;
        }
        wechat.show_toast('发号成功', 1000);
        setTimeout(function() {window.location = 'query_order.html';}, 1000);
    });

}


})(jQuery)

