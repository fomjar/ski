
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
fomjar.framework.phase.append('ren', xsmain);

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

function xsmain() {
    xs.ui.head().reset();
    xs.ui.body().reset();
    xs.ui.body().append(create_page_set());

    xs.user.login_auto();
}

function create_page_set() {
    var set = new xs.ui.PageSet();
    set.page_append(create_page_folder());
    return set;
}

function create_page_folder() {
    var page = new xs.ui.Page({
        name    : 'test',
        view    : $("<div></div>"),
        op_l    : xs.ui.head().cover,
        op_r    : [
            new xs.ui.HeadButton($('<img src=\'res/new.png\'/>')),
            new xs.ui.HeadButton($('<img src=\'res/share.png\'/>')),
        ]
    });
    return page;
}

})(jQuery)

