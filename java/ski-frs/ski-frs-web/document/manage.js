
(function($) {

fomjar.framework.phase.append('dom', frsmain);

var menu = fomjar.util.args.menu || 'dev';

function frsmain() {
    build_head();
    build_body();
}

function build_head() {
    var menu_dev = frs.ui.head().add_item('设备', function() {window.location = window.location.pathname + '?menu=dev';});
    var menu_pic = frs.ui.head().add_item('图片', function() {window.location = window.location.pathname + '?menu=pic';});
    var menu_sub = frs.ui.head().add_item('主体', function() {window.location = window.location.pathname + '?menu=sub';});
    switch (menu) {
    case 'dev': menu_dev.addClass('active'); break;
    case 'pic': menu_pic.addClass('active'); break;
    case 'sub': menu_sub.addClass('active'); break;
    }
}

function build_body() {
    switch (menu) {
    case 'dev': build_body_dev(); break;
    case 'pic': build_body_pic(); break;
    case 'sub': build_body_sub(); break;
    }
}

function build_body_dev() {
    var dev = $('<div></div>');
    dev.addClass('dev');
    
    var l = $('<div></div>');
    l.addClass('l');
    var r = $('<div></div>');
    r.addClass('r');
    
    dev.append([l, r]);
    frs.ui.body().append(dev);
    
    build_body_dev_l();
}

function build_body_dev_l() {
    var oper = $('<div></div>');
    oper.addClass('oper');
    oper.append(new frs.ui.Button(new frs.ui.shape.Plus('1px', 'white', '2em', '2em')));
    
    var list = new frs.ui.List().to_dark();
    list.append_cell({major : '..'});
    list.append_cell({major : 'XXXXXX小区', accessory : true});
    list.append_cell({major : 'SSSSSS街道', accessory : true});
    list.append_cell({major : 'TTTTTT门禁', accessory : true});
    
    $('.frs .body .dev .l').append([oper, list]);
}

function build_body_pic() {
    var pic = $('<div></div>');
    pic.addClass('pic');
    
    var oper = $('<div></div>');
    oper.addClass('oper');
    var cont = $('<div></div>');
    cont.addClass('cont');
    
    pic.append([oper, cont]);
    frs.ui.body().append(pic);
    
    oper.append(new frs.ui.Button('批量上传').to_major());
    oper.append(new frs.ui.Button('批量修改').to_major());
    oper.append(new frs.ui.Button('手工修改').to_minor());
}

function build_body_sub() {
    var sub = $('<div></div>');
    sub.addClass('dev');
    
    var l = $('<div></div>');
    l.addClass('l');
    var r = $('<div></div>');
    r.addClass('r');
    
    sub.append([l, r]);
    frs.ui.body().append(sub);
    
    build_body_sub_l();
}

function build_body_sub_l() {
    var oper = $('<div></div>');
    oper.addClass('oper');
    oper.append(new frs.ui.Button(new frs.ui.shape.Plus('1px', 'white', '2em', '2em')));
    
    var list = new frs.ui.List().to_dark();
    list.append_cell({major : '黑名单'});
    list.append_cell({major : '白名单'});
    list.append_cell({major : '黄名单'});
    
    $('.frs .body .dev .l').append([oper, list]);
}

})(jQuery)

