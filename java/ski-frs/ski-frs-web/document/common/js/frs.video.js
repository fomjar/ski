(function($) {
fomjar.framework.phase.append('ini', function() {

frs.video = {};
frs.video.check = function() {
    // 检查插件是否已经安装过
    if (-1 == WebVideoCtrl.I_CheckPluginInstall()) {
        alert("您还未安装过插件，双击开发包目录里的WebComponents.exe安装！");
        return false;
    }
    return true;
};

frs.video.Player = function(dev, container_id) {
    var div = $('#' + container_id);
    if (!div.length) return null;
    
    div.dev = dev;
    // 初始化插件参数及插入插件
    div.init = function() {
        WebVideoCtrl.I_InitPlugin('100%', '100%');
        WebVideoCtrl.I_InsertOBJECTPlugin(container_id);
    };
    // 登录
    div.login = function(cb_s, cb_f) {
        var ret = WebVideoCtrl.I_Login(dev.ip, 1, dev.port, dev.user, dev.pass, {
            success: function (xml) {if (cb_s) cb_s();},
            error: function () {if (cb_f) cb_f();}
        });
        if (-1 == ret) if (cb_f) cb_f();
    };
    // 退出
    div.logout = function() {
        return 0 == WebVideoCtrl.I_Logout(dev.ip);
    }
    // 播放
    div.play = function() {
        return 0 == WebVideoCtrl.I_StartRealPlay(dev.ip);
    };
    // 停止
    div.stop = function() {
        return 0 == WebVideoCtrl.I_Stop();
    };
    // 暂停
    div.pause = function() {
        return 0 == WebVideoCtrl.I_Pause();
    };
    // 全屏
    div.fullscreen = function() {
        WebVideoCtrl.I_FullScreen(true);
    };
    
    return div;
};

});
})(jQuery);
