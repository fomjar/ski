(function($) {

fomjar.framework.phase.append('dom', build_list);
fomjar.framework.phase.append('dom', build_buttons);
fomjar.framework.phase.append('ren', setup);

function build_list() {
    var list = $('<div></div>');
    list.addClass('list');

    var cell_game = $('<div></div>');
    cell_game.addClass('cell-kv');
    cell_game.append('<div>游戏</div>');
    cell_game.append("<div id='game'></div>");
    var cell_acco = $('<div></div>');
    cell_acco.addClass('cell-kv');
    cell_acco.append('<div>帐号</div>');
    cell_acco.append("<div id='account'></div>");
    var cell_type = $('<div></div>');
    cell_type.addClass('cell-kv');
    cell_type.append('<div>体验类型</div>');
    cell_type.append("<div id='type'></div>");
    var cell_begi = $('<div></div>');
    cell_begi.addClass('cell-kv');
    cell_begi.append('<div>开始时间</div>');
    cell_begi.append("<div id='begin'></div>");
    var cell_expe = $('<div></div>');
    cell_expe.addClass('cell-kv');
    cell_expe.append('<div>消费总计</div>');
    cell_expe.append("<div id='expense'></div>");

    list.append([cell_game, cell_acco, cell_type, cell_begi, cell_expe]);

    $('.vcg .frame .body').append(list);
}

function build_buttons() {
    var button = $('<div></div>');
    button.addClass('button button-major button-large');
    button.text('确认归还');
    button.bind('touchend', apply);

    $('.vcg .frame .body').append(button);
}

function setup() {
    var oid = fomjar.util.args().oid;
    var csn = fomjar.util.args().csn;
    vcg.show_toast('正在加载...');
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_ORDER, {oid : oid, csn : csn}, function(code, desc) {
        vcg.hide_toast();
        if (0 != code) {
            vcg.show_toast('加载失败', 10000);
            return;
        }
        var order = desc;
        $('#game').text(order.game.name_zh_cn);
        $('#account').text(order.account);
        $('#type').text(order.type + '(' + order.price + '元/天)');
        $('#begin').text(order.begin);
        $('#expense').text(order.expense + '元');
        if (0 < order.end.length) {
            $('.button').hide();
        }
    });

}

function apply() {
    var oid = fomjar.util.args().oid;
    var csn = fomjar.util.args().csn;
    vcg.show_toast('正在操作...');
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_APPLY_RENT_END, {oid : oid, csn : csn}, function(code, desc) {
        vcg.hide_toast();
        if (0 != code) {
            vcg.show_toast('操作失败: ' + desc, 10000);
            return;
        }
        vcg.show_toast('归还成功', 1000);
        setTimeout(function() {window.location = 'query_order.html'}, 1000);
    });

}


})(jQuery)
