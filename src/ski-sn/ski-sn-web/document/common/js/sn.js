
var sn = {};
sn.token    = (function() {var token = fomjar.util.cookie('token'); return token && 0 < token.length ? token : undefined;})();
sn.uid      = (function() {var uid = fomjar.util.cookie('uid'); return uid && 0 < uid.length ? parseInt(uid) : undefined;})();
// sn.user      = {};
// sn.location  = {};
sn.stub             = {};
sn.stub.login       = [];
sn.stub.locate      = [];

(function($) {

fomjar.framework.phase.append('dom', build_frame);
fomjar.framework.phase.append('dom', build_head);
fomjar.framework.phase.append('ren', init_event);
fomjar.framework.phase.append('ren', login_automatic);

function build_frame() {
    var sn = $('<div></div>');
    sn.addClass('sn');
    var bg = $('<div></div>');
    bg.addClass('bg');
    bg.append("<img src='/res/bg.jpg'/>");
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
    var cover = $('<div></div>');
    cover.addClass('cover');
    cover.append("<img src='res/user.png' />");
    cover.append('<div>登录 / 注册</div>');
    
    $('.sn .head').append(cover);
    cover.bind('click', sn.ui.login);
}

function build_user_state() {

    $('.sn .head').append("<div class='state'><img src='/res/state-locate.png' /><div style='display:none'></div></div>");
//     $('.sn .head').append("<div class='state'><img src='/res/state-nearby.png' /><div style='display:none'></div></div>");
//     $('.sn .head').append("<div class='state'><img src='/res/state-notify.png' /><div style='display:none'></div></div>");
    
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
    
function login_manually(phone, pass, success, failure) {
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
        
        $('.sn .head .cover >*:nth-child(1)').attr('src', sn.user.cover);
        $('.sn .head .cover >*:nth-child(2)').text(sn.user.name);
        $('.sn .head .cover').unbind('click');
        $('.sn .head .cover').bind('click', sn.ui.detail);

        build_user_state();
        
        if (success) success();
        
        $.each(sn.stub.login, function(i, f) {f(sn.user);});
    });
}

function login_automatic() {
    if (!sn.token) return;

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
        
        $('.sn .head .cover >*:nth-child(1)').attr('src', sn.user.cover);
        $('.sn .head .cover >*:nth-child(2)').text(sn.user.name);
        $('.sn .head .cover').unbind('click');
        $('.sn .head .cover').bind('click', sn.ui.detail);
        
        build_user_state();
        $.each(sn.stub.login, function(i, f) {f(sn.user);});
    });
}


})(jQuery);
