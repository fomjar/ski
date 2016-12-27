
var xs = {};
xs.config = function(key, val) {
    if (val) {
        return fomjar.util.cookie(key, val);
    } else {
        val = fomjar.util.cookie(key);
        if (val && 0 < val.length) return val;
        else return undefined;
    }
}
xs.token    = xs.config('token');
xs.uid      = parseInt(xs.config('uid'));

(function($) {

fomjar.framework.phase.append('dom', build_frame);
fomjar.framework.phase.append('ren', login_auto);
fomjar.framework.phase.append('ren', test);

function build_frame() {
    var frame = $('<div></div>');
    frame.addClass('xs disappear');

    var head = $('<div></div>');
    head.addClass('head');
    var body = $('<div></div>');
    body.addClass('body');


    frame.append([head, body]);
    $('body').append(frame);

    fomjar.util.async(function() {frame.removeClass('disappear');});
}

function login_auto() {
    xs.user.login_auto();
}

function test() {
    var ps = new xs.ui.PageSet();
    ps.page_set_switch_cb(ps.PAGE_SWITCH_CB_DEFAULT);

    xs.ui.body().append(ps);

    ps.page_append(new xs.ui.Page({
        name    : 'test',
        view    : $("<div></div>"),
        oper    : new xs.ui.HeadButton($('<img src=\'res/share.png\'/>'))
    }));
}

})(jQuery)

