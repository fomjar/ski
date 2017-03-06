
(function($) {

fomjar.framework.phase.append('dom', build_frame);
fomjar.framework.phase.append('ren', update);

var menu = fomjar.util.args.menu;
if (!menu) menu = 'dev';

function build_frame() {
    var menu_dev = frs.ui.head().add_item('设备', function() {window.location = window.location.pathname + '?menu=dev';});
    var menu_pic = frs.ui.head().add_item('图片', function() {window.location = window.location.pathname + '?menu=pic';});
    var menu_sub = frs.ui.head().add_item('主体', function() {window.location = window.location.pathname + '?menu=sub';});
    switch (menu) {
    case 'dev': menu_dev.addClass('active'); break;
    case 'pic': menu_pic.addClass('active'); break;
    case 'sub': menu_sub.addClass('active'); break;
    }
}

function update() {
    
}

})(jQuery)

