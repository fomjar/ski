(function($) {
fomjar.framework.phase.append('ini', function() {

xs.user = {};
xs.user.map_dir = {};
xs.user.map_art = {};
xs.user.dir_cur = '';

xs.user.dir_new = function(subdir, info) {
    if (!subdir) return;
    if (!info)  info = {};
    if (subdir != '/') {
        while (subdir.startsWith('/')) subdir = subdir.substring(1, subdir.length - 1);
        if (!subdir.endsWith('/'))     subdir = subdir + '/';
    }

    var dir = xs.user.dir_cur + subdir;
    xs.user.map_dir[dir] = info;
    xs.user.map_art[dir] = [];
};
xs.user.dir_name = function(dir) {
    if (!dir || '' == dir || '/' == dir) return dir;

    while (dir.endsWith('/')) dir = dir.substring(0, dir.length - 1);

    if (dir.indexOf('/') < 0) return dir;
    else return dir.substring(dir.lastIndexOf('/') + 1);
};
xs.user.dir_new('/', {});
xs.user.dir_cur = '/';

xs.user.dir_sub = function(dir) {
    var subdirs = [];
    $.each(xs.user.map_dir, function(k, v) {
        if (k.startsWith(dir)) {
            var path = k.substring(dir.length);
            if (path.indexOf('/') >= 0) {
                path = path.substring(0, path.length - 1);
                if (path.indexOf('/') < 0) {
                    subdirs.push(k);
                }
            }
        }
    });
    return subdirs;
};

xs.user.login_auto = function() {
    xs.user.logout_cb();
}

xs.user.login_manual = function() {
}


xs.user.login_cb = function(user) {
}

xs.user.logout_cb = function() {
}

});
})(jQuery);
