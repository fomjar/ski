(function($) {
fomjar.framework.phase.append('ini', function() {

frs.video = {};
frs.video.available = function() {
    // 检查插件是否已经安装过
    if (-1 == WebVideoCtrl.I_CheckPluginInstall()) {
        alert("您还未安装过插件，双击开发包目录里的WebComponents.exe安装！");
        return false;
    }
    return true;
};

frs.video.Player = function(devs, id) {
    if (!devs || !devs.length) return null;
    
    var player = {devs : devs};
    // Web 插件初始化(包含插件事件注册)
    WebVideoCtrl.I_InitPlugin('100%', '100%', {
        iWndowType : Math.min(4, Math.ceil(Math.sqrt(devs.length)))   // 最大 4 * 4
    });
    // 嵌入播放插件
    if (!id) {
        id = 'player_' + new Date().getTime().toString();
        var div = $('<div></div>');
        div.attr('id', id);
        div.css('width', '0');
        div.css('height', '0');
        $('body').append(div);
    }
    WebVideoCtrl.I_InsertOBJECTPlugin(id);
    // 销毁
    player.destory = function() {
        player.stop();
        player.logout();
        $('#' + id).remove();
    };
    // 登录设备
    player.login = function(cb_s, cb_f) {
        $.each(player.devs, function(i, dev) {
            var r = WebVideoCtrl.I_Login(dev.ip, 1, 80, dev.user, dev.pass, {
                async : false,
                success: function (xml) {if (cb_s) cb_s(dev, xml);},
                error: function () {if (cb_f) cb_f(dev);}
            });
            if (-1 == r) if (cb_s) cb_s(dev); // 重复登陆
        });
    };
    // 登出设备
    player.logout = function() {
        var r = 0;
        $.each(player.devs, function(i, dev) {r |= WebVideoCtrl.I_Logout(dev.ip);});
        return 0 == r;
    };
    // 获取设备基本信息
    player.info = function(i, cb_s, cb_f) {
        var dev = player.devs[i];
        WebVideoCtrl.I_GetDeviceInfo(dev.ip, {
            async : false,
            success: function (xml) {if (cb_s) cb_s(dev, xml);},
            error: function () {if (cb_f) cb_f(dev);}
        });
    };
    // 开始预览
    player.play = function(i) {
        if ('number' == typeof(i)) return 0 == WebVideoCtrl.I_StartRealPlay(player.devs[i].ip, {iWndIndex : i});
        else {
            var r = 0;
            $.each(player.devs, function(i, dev) {r |= WebVideoCtrl.I_StartRealPlay(dev.ip, {iWndIndex : i});});
            return 0 == r;
        }
    };
    // 停止播放
    player.stop = function(i) {
        if ('number' == typeof(i)) return 0 == WebVideoCtrl.I_Stop(i);
        else {
            var r = 0;
            $.each(player.devs, function(i, dev) {r |= WebVideoCtrl.I_Stop(i);});
            return 0 == r;
        }
    };
    // 暂停
    player.pause = function(i) {
        if ('number' == typeof(i)) return 0 == WebVideoCtrl.I_Pause(i);
        else {
            var r = 0;
            $.each(player.devs, function(i, dev) {r |= WebVideoCtrl.I_Pause(i);});
            return 0 == r;
        }
    };
    // 恢复播放
    player.resume = function(i) {
        if ('number' == typeof(i)) return 0 == WebVideoCtrl.I_Resume(i);
        else {
            var r = 0;
            $.each(player.devs, function(i, dev) {r |= WebVideoCtrl.I_Resume(i);});
            return 0 == r;
        }
    };
    // 全屏播放
    player.fullscreen = function(b) {WebVideoCtrl.I_FullScreen(b);};
    
    return player;
};

});
})(jQuery);
