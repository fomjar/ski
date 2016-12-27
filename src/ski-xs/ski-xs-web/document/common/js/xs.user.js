xs.user = {};

xs.user.login_auto = function() {
    xs.user.logout_cb();
}

xs.user.login_manual = function() {
    var mask = new xs.ui.Mask();
    mask.appear();

    var dialog = new xs.ui.Dialog();
    dialog.size('50%', '50%');
    dialog.appear();

    dialog.bind('click', function() {
        mask.disappear();
        dialog.disappear();
    });
}


xs.user.login_cb = function(user) {
    
}

xs.user.logout_cb = function() {
    xs.ui.head().reset();
    xs.ui.head().cover.div.text('登陆');
    xs.ui.head().cover.bind('click', xs.user.login_manual);
}


