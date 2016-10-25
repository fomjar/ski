(function($) {

fomjar.framework.phase.append('dom', build_tab);
fomjar.framework.phase.append('ren', setup);

function build_tab() {
    var tab = vcg.create_tab([
        {
            head : '正在玩',
            body : build_tab_body_now()
        },
        {
            head : '已归还',
            body : build_tab_body_old() 
        }
    ]);

    $('.vcg .frame .body').append(tab);
}

function build_tab_body_now() {
    var list = $('<div></div>');
    list.addClass('list');
    return list;
}

function build_tab_body_old() {
    var list = $('<div></div>');
    list.addClass('list');
    return list;
}

function setup() {
    vcg.show_toast('正在获取...');
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_ORDER, function(code, desc) {
        vcg.hide_toast();
        if (0 != code) {
            vcg.show_toast(desc, 10000);
            return;
        }

        $.each(desc, function(i, c) {
            if (0 == c.end.length) { // now
                var ext = $('<div></div>');
                ext.addClass('list');
                ext.append("<div class='cell-kv'><div>体验类型</div><div>"+c.type+' ('+c.price+"元/天)</div></div>");
                ext.append("<div class='cell-kv'><div>费用小计</div><div>"+c.expense+"元</div></div>");
                var button_view = $('<div></div>');
                button_view.addClass('button button-major button-small');
                button_view.text('查看游戏');
                button_view.bind('touchend', function() {window.location = 'query_game_by_gid.html?gid='+c.game.gid.toString(16);});
                var button_end = $('<div></div>');
                button_end.addClass('button button-major button-small');
                button_end.text('我要归还');
                button_end.bind('touchend', function() {window.location = 'apply_rent_end.html?oid='+c.oid.toString(16)+'&csn='+c.csn.toString(16);});
                var buttons = $('<table></table>');
                var tr = $('<tr></tr>');
                var td_v = $('<td></td>');
                var td_e = $('<td></td>');
                td_v.append(button_view);
                td_e.append(button_end);
                tr.append([td_v, td_e]);
                buttons.append(tr);
                ext.append(buttons);
                ext.hide();

                var cell = vcg.create_list_cell_order_now(c);
                cell.append("<div class='m1'>点击展开</div>");
                cell.bind('click', function() {
                    if (ext.is(':visible')) ext.hide();
                    else ext.show();
                });

                $($('.tab >div >div >.list')[0]).append([cell, ext]);
            } else { // old
                var cell = vcg.create_list_cell_order_old(c);
                cell.bind('click', function () {window.location = 'query_game_by_gid.html?gid='+c.game.gid.toString(16);});
                $($('.tab >div >div >.list')[1]).append(cell);
            }
        });
    });
}


})(jQuery)

