
fomjar.framework.phase.append('dom', build_tab);

function build_tab() {
    var tab = $('<div></div>');
    tab.addClass('wechat-tab');
    // head
    tab.append('<div><div>正在玩</div><div>已归还</div></div>');
    // body
    tab.append('<div></div>');

    $('.wechat-frame-content').append(tab);
}

