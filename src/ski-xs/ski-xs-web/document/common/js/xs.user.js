xs.user = {};

xs.user.login_auto = function() {
    xs.user.logout_cb();
}

xs.user.login_manual = function() {
}


xs.user.login_cb = function(user) {
    
}

xs.user.logout_cb = function() {
    xs.ui.head().cover.div.text('登陆');
    xs.ui.head().cover.bind('click', xs.user.login_manual);
}


