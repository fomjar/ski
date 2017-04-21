
(function($) {

fomjar.framework.phase.append('dom', frsmain);

var menu = fomjar.util.args.menu || 'dev';

function frsmain() {
    build_head();
    build_body();
}

function build_head() {
    frs.ui.head().style_default();
    var menu_dev = frs.ui.head().append_item('设备管理', function() {window.location = window.location.pathname + '?menu=dev';});
    var menu_pic = frs.ui.head().append_item('图库管理', function() {window.location = window.location.pathname + '?menu=pic';});
    switch (menu) {
    case 'dev': menu_dev.addClass('active'); break;
    case 'pic': menu_pic.addClass('active'); break;
    }
}

function build_body() {
    switch (menu) {
    case 'dev': build_body_dev(); break;
    case 'pic': build_body_pic(); break;
    }
}

function build_body_dev() {
    frs.ui.layout.lr(frs.ui.body());
    
    build_body_dev_l();
}

function build_body_dev_l() {
    var oper = $('<div></div>');
    oper.addClass('oper');
    oper.append(new frs.ui.Button(new frs.ui.shape.Plus('1px', '#333344', '1.5em', '1.5em')));
    
    frs.ui.body().l.append(oper);
}

function build_body_pic() {
    var oper = $('<div></div>');
    oper.addClass('oper');
    
    oper.append(new frs.ui.Button('批量导入'));
    oper.append(new frs.ui.Button('批量删除'));
    
    frs.ui.body().append(oper);
}

})(jQuery)

