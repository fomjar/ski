
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

function build_frame() {
    var frame = $('<div></div>');
    frame.addClass('xs');

    var head = $('<div></div>');
    head.addClass('head');
    var body = $('<div></div>');
    body.addClass('body');


    frame.append([head, body]);
    $('body').append(frame);

    fomjar.util.async(function() {frame.addClass('xs-appear');});
}

function login_auto() {
    xs.user.login_auto();
}

})(jQuery)
