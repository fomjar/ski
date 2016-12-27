xs.user = {};

xs.user.login_auto = function() {
    xs.user.logout_cb();
}

xs.user.login_cb = function(user) {
    
}

xs.user.logout_cb = function() {
    xs.ui.head().reset();
}


