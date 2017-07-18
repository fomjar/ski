//index.js

//获取应用实例
var app = getApp();
var net  = require('../../ski/net.js');
var util = require('../../ski/util.js');

Page({
    onLoad    : function () {},
    onReady   : function () {},
    onShow    : function () {},
    onHide    : function () {},
    onUnload  : function () {},
    onPullDownRefreash : function () {},
    data : {
        button_test : {
            wxt_btn_type  	: 'primary',
            wxt_btn_name    : '测试',
            wxt_btn_bindtap : 'tap_test'
        },
        response : [],
    },
    
    tap_test : function() {
        var page = this;
        net.send(net.ISIS.INST_ECOM_QUERY_GAME, {user : app.user, gid : '1E'}, function(code, desc) {
            var response = page.data.response;
            response[response.length] = code + ' - ' + util.ots(desc);

            page.setData({
                response : response
            });
        });
    },
})
