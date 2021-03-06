var frs = {};

(function($) {

fomjar.framework.phase.append('ini', init);
fomjar.framework.phase.append('dom', build_frame);

function init() {
    if (FastClick) {
        FastClick.attach(document.body);
    }
    
    if ($.jstree) {
        $.jstree.defaults.core.themes.icons = false;
        $.jstree.defaults.core.themes.ellipsis = false;
    }
    
    // config
    frs.config = function(key, val) {
        if (val) {
            return fomjar.util.cookie(key, val);
        } else {
            val = fomjar.util.cookie(key);
            if (val && 0 < val.length) return val;
            else return undefined;
        }
    }
    frs.token   = frs.config('token');
    frs.user    = frs.config('user');
}

function build_frame() {
    var frame = $('<div></div>');
    frame.addClass('frs');

    var head = $('<div></div>');
    head.addClass('head');
    var body = $('<div></div>');
    body.addClass('body');

    frame.append([head, body]);
    $('body').append(frame);
};

})(jQuery)

