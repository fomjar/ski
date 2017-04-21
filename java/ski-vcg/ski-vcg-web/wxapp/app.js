//app.js

App({
    onLaunch : function () {
        var app  = this;
        var intf = require('ski/interface.js');
        intf.login(function(user) {
            app.user = user;
        });
    },
    onShow : function () {},
    onHide : function () {},

    user : null,
})