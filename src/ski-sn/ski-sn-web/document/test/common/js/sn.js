
var sn = {};
sn.config = function(key, val) {
    if (val) {
        return fomjar.util.cookie(key, val);
    } else {
        val = fomjar.util.cookie(key);
        if (val && 0 < val.length) return val;
        else return undefined;
    }
}
sn.token    = sn.config('token');
sn.uid      = parseInt(sn.config('uid'));
// sn.user      = {};
// sn.location  = {};
sn.stub             = {};
sn.stub.login       = [];
sn.stub.logout      = [];
sn.stub.locate      = [];

sn.login_manually = function(phone, pass, success, failure) {
    fomjar.net.send(ski.ISIS.INST_APPLY_AUTHORIZE, {
        phone       : phone,
        pass        : pass,
        terminal    : 1
    }, function(code, desc) {
        if (0 != code) {
            if (failure) failure(code, desc);
            return;
        }
        
        fomjar.util.cookie('token', desc.token, 365);
        fomjar.util.cookie('uid',   desc.uid,   365);
        sn.token = desc.token;
        sn.uid   = desc.uid;
        sn.user  = desc;
        if (!sn.user.cover) sn.user.cover = 'res/user.png';
        
        $('.sn .head .cover img').attr('src', sn.user.cover);
        $('.sn .head >*:nth-child(2)').text(sn.user.name);
        $('.sn .head .cover').unbind('click');
        $('.sn .head .cover').bind('click', sn.ui.detail);

        if (success) success();
        
        $.each(sn.stub.login, function(i, f) {f(sn.user);});
    });
};

sn.login_automatic = function() {
    if (!sn.token) return;

    setTimeout(function() {
        fomjar.net.send(ski.ISIS.INST_APPLY_AUTHORIZE, {
            token       : sn.token,
            uid         : sn.uid,
            terminal    : 1
        }, function(code, desc) {
            if (0 != code) return;
            
            fomjar.util.cookie('token', desc.token, 365);
            fomjar.util.cookie('uid',   desc.uid,   365);
            sn.token = desc.token;
            sn.uid   = desc.uid;
            sn.user  = desc;
            if (!sn.user.cover) sn.user.cover = 'res/user.png';
            
            $('.sn .head .cover img').attr('src', sn.user.cover);
            $('.sn .head >*:nth-child(2)').text(sn.user.name);
            $('.sn .head .cover').unbind('click');
            $('.sn .head .cover').bind('click', sn.ui.detail);
            
            $.each(sn.stub.login, function(i, f) {f(sn.user);});
        });
    }, 500);
};


(function($) {

fomjar.framework.phase.append('dom', build_frame);
fomjar.framework.phase.append('dom', build_head);
fomjar.framework.phase.append('ren', init_event);
fomjar.framework.phase.append('ren', sn.login_automatic);
fomjar.framework.phase.append('ren', watch_location);

function build_frame() {
    var sn = $('<div></div>');
    sn.addClass('sn');
    var bg = $('<div></div>');
    bg.addClass('bg');
    bg.append("<img src='res/bg.jpg'/>");
    bg.append('<div></div>');
    var head = $('<div></div>');
    head.addClass('head');
    var body = $('<div></div>');
    body.addClass('body');
    var foot = $('<div></div>');
    foot.addClass('foot');
    var mask = $('<div></div>');
    mask.addClass('mask');
    mask.hide();
    var dialog = $('<div></div>');
    dialog.addClass('dialog');
    dialog.hide();
    var browse = $('<div></div>');
    browse.addClass('browse');
    browse.append('<img />');
    browse.hide();
    var toast = $('<div></div>');
    toast.addClass('toast');
    toast.hide();
    
    sn.append([bg, head, body, foot, mask, dialog, browse, toast]);
    $('body').append(sn);
}

function build_head() {
    build_user_cover();
    build_user_state();
    
    $('.sn .head').bind('click', function(e) {
        if (!$(e.target).hasClass('head')) return;
        
        var i = setInterval(function() {
            var s = Math.pow($('.sn .body').scrollTop(), 1 / 1.5);
            if (s < 1) s = 1;
            $('.sn .body').scrollTop($('.sn .body').scrollTop() - s);
            if ($('.sn .body').scrollTop() <= 0) clearInterval(i);
        }, 10);
    });
}

function build_user_cover() {
    $('.sn .head').append("<div class='cover'><img src='res/user.png' /></div>");
    $('.sn .head').append('<div>登录 / 注册</div>');
    $('.sn .head').find('>*:nth-child(1)').bind('click', sn.ui.login);
    $('.sn .head').find('>*:nth-child(2)').bind('click', function() {
        $('.sn .head').find('>*:nth-child(1)').trigger('click');
    });
}

function build_user_state() {

    $('.sn .head').append("<div class='state'><img src='res/state-locate.png' /><div style='display:none'></div></div>");
//     $('.sn .head').append("<div class='state'><img src='res/state-nearby.png' /><div style='display:none'></div></div>");
//     $('.sn .head').append("<div class='state'><img src='res/state-notify.png' /><div style='display:none'></div></div>");
    
    var states = $('.sn .head .state');
    $.each(states, function(i, state) {
        state = sn.ui.state($(state));
        state.bind('click', function() {
            if (!state.isopen()) state.open();
            else state.close();
        });
    });
}

function init_event() {
    // fast click
    FastClick.attach(document.body);
}

function watch_location() {
    var run = function(r){
        if (this.getStatus() == BMAP_STATUS_SUCCESS){
            var p = r.point;
            if (sn.user) {
                fomjar.net.send(ski.ISIS.INST_UPDATE_USER_STATE, {
                    state       : 1,
                    terminal    : 1,
                    location    : p.lat + ':' + p.lng
                }, function(code, desc) {});
            }
            new BMap.Geocoder().getLocation(p, function(rs) {
                var addr = rs.addressComponents.street + rs.addressComponents.streetNumber;
                if (0 < rs.surroundingPois.length) {
                    addr = rs.surroundingPois[0].title;
                }
                /*
                Object =
                    address: "中影国际影城南京雨花台南站店"
                    addressComponents: Object
                        city: "南京市"
                        district: "雨花台区"
                        province: "江苏省"
                        street: "明城大道"
                        streetNumber: ""
                    “Object”原型
                    business: "宁南"
                    point: H
                        lat: 31.98444
                        lng: 118.803924
                    “H”原型
                    surroundingPois: Array (4)
                        0 Object
                            Ui: "休闲娱乐"
                            address: "南京市雨花台区玉兰路99号(明发商业广场1幢B6区4层17室)"
                            city: "南京市"
                            eu: Array (1)
                                0 "休闲娱乐"
                            “Array”原型
                            phoneNumber: null
                            point: H
                                lat: 31.984822
                                lng: 118.804075
                            “H”原型
                            postcode: null
                            title: "中影国际影城南京雨花台南站店"
                            type: 0
                            uid: "9abe5786376f2dd98bfc06ef"
                        “Object”原型
                        1 {title: "永辉超市(雨花店)", uid: "7a443c7ec83b4ccd2fe67bb8", point: H, city: "南京市", Ui: "购物", …}
                        2 {title: "南京易居智能科技有限公司", uid: "3a46dbe174d7478203ec0106", point: H, city: "南京市", Ui: "公司企业", …}
                        3 {title: "德居欣舒适家居体验中心", uid: "f270eba5dda323d682288ae4", point: H, city: "南京市", Ui: "购物", …}
                    “Array”原型
                    “Object”原型
                */
                if (!sn.location || addr != sn.location.address) {
                    var state_locate = sn.ui.state(1);
                    state_locate.find('>div').text(addr);
                    state_locate.find('>div').css('width', addr.length + 'em');
                    state_locate.flash();
                }
                sn.location = rs;
                sn.location.address = addr;
                
                $.each(sn.stub.locate, function(i, f) {f(sn.location);});
            });
        } else {}
    };
    new BMap.Geolocation().getCurrentPosition(run);
    setTimeout(function() {if (sn.location) sn.ui.state(1).flash();}, 1000 * 5);
    setInterval(function() {new BMap.Geolocation().getCurrentPosition(run);}, 1000 * 10);
}

})(jQuery);
