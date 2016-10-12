
fomjar.framework.phase.append('dom', build_tab);
fomjar.framework.phase.append('ren', setup);

function build_tab() {
    var tab = wechat.create_tab([
        {
            head : '正在玩',
            body : build_tab_body_now()
        },
        {
            head : '已归还',
            body : build_tab_body_old() 
        }
    ]);

    $('.wechat .frame .body').append(tab);
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
    wechat.show_toast('正在获取...');
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_ORDER, function(code, desc) {
        wechat.hide_toast();
        if (0 != code) {
            wechat.show_toast(desc, 10000);
            return;
        }

        $.each(desc, function(i, c) {
            if (0 == c.end.length) { // now
            } else { // old
            }
        });
    });
}
