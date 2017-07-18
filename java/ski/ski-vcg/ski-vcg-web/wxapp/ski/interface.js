
var net = require('net.js');

function login(cb) {
    wx.login({
        success: function(res) {
            // net.send(net.ISIS.INST_ECOM_APPLY_AUTHORIZE, {code : res.code}, function(code, desc) {
            //     cb(desc.user);
            // });
            cb(216);
        }
    });
}

function user(cb) {
    wx.getUserInfo({
        success: function(res) {
            cb(res.userInfo);
        },
    });
}

module.exports = {
    login   : login,
    user    : user,
};
