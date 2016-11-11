
(function($) {

fomjar.framework.phase.append('dom', animate);
fomjar.framework.phase.append('ren', init_event);

function animate() {
    // revert
    var bg_img  = $('.sn .bg >img');
    var bg_mask = $('.sn .bg >div');
    var sn_head = $('.sn .head');
    
    bg_img.css('opacity', '0');
    bg_img.css('width', '100%');
    bg_img.css('height', '100%');
    bg_img.css(        'filter', 'none');
    bg_img.css('-webkit-filter', 'none');
    
    bg_mask.css('opacity', '0');
    
    sn_head.css('top', '-3em');
    
    // animate
    bg_img.bind('load', function() {
        bg_img.css('opacity', '1');
        
        setTimeout(function() {
            bg_img.css('width',  '105%');
            bg_img.css('height', '105%');
            
            setTimeout(function() {
                bg_img.css(        'filter', 'blur(.3em)');
                bg_img.css('-webkit-filter', 'blur(.3em)');
                
                bg_mask.css('opacity', '.3');
                
                sn_head.css('top', '0');
                
                animate_done();
            }, 3000);
        }, 500);
    });
}

function init_event() {
    // scroll event
    sn.ui.scroll($('.sn .body'),
        function() {    // begin
            $('.sn .head').css('top', '-3em');
            if (sn.user) $('.sn .foot').css('bottom', '-4em');
        },
        function() {    // end
            $('.sn .head').css('top', '0');
            if (sn.user) $('.sn .foot').css('bottom', '0');
        },
        function() {    // top
            $('.sn .head').css('top', '0');
            if (sn.user) $('.sn .foot').css('bottom', '0');
        },
        function() {    // bottom
            load_message(sn.message.length, 10);
        },
        200);
    
    // pull event
    sn.ui.pull($('.sn .body'),
        function() {    // down
            load_message(0, 20);
        },
        function() {},  // up
        80);
}

function animate_done() {
    load_message(0, 20);
    if (sn.user) {
        $('.sn .foot').css('bottom', '0');
    } else {
        sn.stub.login.push(function() {
            $('.sn .foot').css('bottom', '0');
        });
    }
}


var ATTITUDE_NONE   = 0;
var ATTITUDE_UP     = 1;
var ATTITUDE_DOWN   = 2;

var loading = false;

function load_message(pos, len) {
    if (!sn.location) return;
    if (loading) return;
    
    sn.ui.toast('正在加载消息');
    loading = true;
    fomjar.net.send(ski.ISIS.INST_QUERY_MESSAGE, {
        lat : sn.location.point.lat,
        lng : sn.location.point.lng,
        pos : pos,
        len : len
    }, function(code, desc) {
        loading = false;
        if (0 != code) return;
        
        if (0 == pos) {
            $('.sn .body').html('');
            sn.message = [];
        }
        if (0 == desc.length) {
            sn.ui.toast('已没有消息');
            return;
        }
        
        var delay = 0;
        
        $.each(desc, function(i, msg) {
            sn.message.push(msg);
            
            var panel = create_message_panel(msg);
            var reply = create_message_reply(msg);
            msg.panel = panel;
            msg.reply = reply;
            
            var div_msg = $([panel[0], reply.find('.msg')[0]]);
            
            var show_detail = function() {
                var dialog = sn.ui.dialog();
                dialog.append(reply);
                dialog.appear();
            };
            panel.find('.mc').bind('click', show_detail);
            panel.find('.mf .button').bind('click', show_detail);

            msg.attitude = function() {
                if (!sn.user) return null;
                var a = null;
                $.each(msg.focuser, function(i, f) {
                    if (f.uid = sn.uid) {
                        a = f;
                        return false;
                    }
                });
                if (!a) {
                    a = {mid : msg.mid, uid : sn.uid, type : ATTITUDE_NONE};
                    msg.focuser.push(a);
                }
                return a;
            };
            msg.attitude_up = function() {
                if (!sn.user) {
                    sn.ui.dialog().children().detach();
                    sn.login();
                    return;
                }
                var a = msg.attitude();
                var up = div_msg.find('.ma >div:nth-child(1)');
                var num = div_msg.find('.ma >div:nth-child(2)');
                var down = div_msg.find('.ma >div:nth-child(3)');
                switch (a.type) {
                case ATTITUDE_NONE:
                    a.type = ATTITUDE_UP;
                    up.css('background-color', '#bbbbbb');
                    num.text(parseInt(num[0].innerText) + 1);
                    down.css('background-color', '');
                    break;
                case ATTITUDE_UP:
                    a.type = ATTITUDE_NONE;
                    up.css('background-color', '');
                    num.text(parseInt(num[0].innerText) - 1);
                    down.css('background-color', '');
                    break;
                case ATTITUDE_DOWN:
                    a.type = ATTITUDE_UP;
                    up.css('background-color', '#bbbbbb');
                    num.text(parseInt(num[0].innerText) + 2);
                    down.css('background-color', '');
                    break;
                }
                msg.attitude_update();
            };
            msg.attitude_down = function() {
                if (!sn.user) {
                    sn.ui.dialog().children().detach();
                    sn.login();
                    return;
                }
                var a = msg.attitude();
                var up = div_msg.find('.ma >div:nth-child(1)');
                var num = div_msg.find('.ma >div:nth-child(2)');
                var down = div_msg.find('.ma >div:nth-child(3)');
                switch (a.type) {
                case ATTITUDE_NONE:
                    a.type = ATTITUDE_DOWN;
                    up.css('background-color', '');
                    num.text(parseInt(num[0].innerText) - 1);
                    down.css('background-color', '#bbbbbb');
                    break;
                case ATTITUDE_UP:
                    a.type = ATTITUDE_DOWN;
                    up.css('background-color', '');
                    num.text(parseInt(num[0].innerText) - 2);
                    down.css('background-color', '#bbbbbb');
                    break;
                case ATTITUDE_DOWN:
                    a.type = ATTITUDE_NONE;
                    up.css('background-color', '');
                    num.text(parseInt(num[0].innerText) + 1);
                    down.css('background-color', '');
                    break;
                }
                msg.attitude_update();
            };
            msg.attitude_update = function() {
                if (!sn.user) {
                    sn.login();
                    return;
                }
                fomjar.net.send(ski.ISIS.INST_UPDATE_MESSAGE_FOCUS, {
                    mid     : msg.mid,
                    type    : msg.attitude().type
                }, function(code, desc) {
                    if (0 != code) sn.ui.toast('操作失败');
                    else sn.ui.toast('操作成功');
                })
            };
            
            fomjar.net.send(ski.ISIS.INST_QUERY_MESSAGE_FOCUS, {
                mid : msg.mid
            }, function(code, desc) {
                if (0 != code) return;
                
                msg.focuser = desc;
                
                panel.onfocuser(desc);
                reply.onfocuser(desc);
            });
            
            panel.css('opacity', '0');
            $('.sn .body').append(panel);
            setTimeout(function() {panel.css('opacity', '1');}, delay);
            delay += 100;
        });

    });
}

function create_message_panel(msg) {
    var div = $('<div></div>');
    div.addClass('msg');
    var mc = $('<div></div>');
    mc.addClass('mc');
    var ma = $('<div></div>');
    ma.addClass('ma');
    
    var mh = $('<div></div>');
    mh.addClass('mh');
    mh.append("<div><img src='" + msg.ucover + "'/><div>" + msg.uname + "</div></div>")
    
    var mb = $('<div></div>');
    mb.addClass('mb');
    if (0 < msg.mtext.length)  mb.append('<div>' + new fomjar.util.base64().decode(msg.mtext)  + '</div>');
    if (0 < msg.mimage.length) mb.append("<img src='" + msg.mimage + "' / >");
    mb.append("<div class='ass'><img src='res/msg-time.png'/><div>" + get_time_description(msg.second) + "</div><img src='res/msg-dist.png'/><div>" + get_distance_description(msg.distance) + "</div></div>")
    
    var mf = $('<div></div>');
    mf.addClass('mf');
    mf.append("<div class='button'>回复(" + msg.reply + ")</div>");
    
    mc.append([mh, mb, mf]);
    
    ma.append("<div></div><div>" + msg.focus + "</div><div></div>");
    ma.find('div:nth-child(1)').bind('click', function() {msg.attitude_up();});
    ma.find('div:nth-child(3)').bind('click', function() {msg.attitude_down();});
    
    var table = $('<table></table>');
    var tr = $('<tr></tr');
    var td_mc = $('<td></td>');
    td_mc.append(mc);
    var td_ma = $("<td></td>");
    td_ma.append(ma);
    tr.append([td_mc, td_ma]);
    table.append(tr);
    
    div.append(table);
    
    div.onfocuser = function(focuser) {
        if (!sn.user) return;
        
        var up = ma.find('div:nth-child(1)');
        var num = ma.find('div:nth-child(2)');
        var down = ma.find('div:nth-child(3)');
        switch (msg.attitude().type) {
        case ATTITUDE_NONE:
            up.css('background-color', '');
            down.css('background-color', '');
            break;
        case ATTITUDE_UP:
            up.css('background-color', '#bbbbbb');
            down.css('background-color', '');
            break;
        case ATTITUDE_DOWN:
            up.css('background-color', '');
            down.css('background-color', '#bbbbbb');
            break;
        }
    };
    
    return div;
}

function get_distance_description(distance) {
    var i = 0;
    
    i = distance / 1000 / 10000;
    if (1 <= i) return i.toFixed(0) + '万公里';
    
    i = distance / 1000 / 1000;
    if (1 <= i) return i.toFixed(0) + '千公里';
    
    i = distance / 1000 / 100;
    if (1 <= i) return i.toFixed(0) + '百公里';
    
    i = distance / 1000;
    if (1 <= i) return i.toFixed(1) + '公里';
    
    i = distance / 100;
    if (1 <= i) return i.toFixed(0) + '米';
    
    return '身边';
}

function get_time_description(second) {
    var i = 0;
    
    i = second / 60 / 60 / 24 / 365;
    if (1 <= i) return i.toFixed(0) + '年前';
    
    i = second / 60 / 60 / 24;
    if (1 <= i) return i.toFixed(0) + '天前';
    
    i = second / 60 / 60;
    if (1 <= i) return i.toFixed(0) + '小时前';
    
    i = second / 60;
    if (1 <= i) return i.toFixed(0) + '分钟前';
    
    return '刚刚';
}

function create_message_reply(msg) {
    var div = $('<div></div>');
    div.addClass('reply');
    
    var panel = create_message_panel(msg);
    panel.find('.mf').remove();
    
    var focus = $('<div></div>');
    focus.addClass('focus');
    focus.append("<div>支持</div>");
    var focus_up = $('<div></div>');
    focus.append(focus_up);
//     focus.append("<div>反对</div>");
//     var focus_down = $('<div></div>');
//     focus.append(focus_down);
    
    var action = $('<div></div>');
    action.addClass('action');
    action.append("<div class='button'>返回</div>");
    action.append("<div class='button'>回复</div>");
    action.find('.button:nth-child(1)').bind('click', function() {sn.ui.dialog().disappear();});
    
    div.append([panel, focus, action]);
    
    div.onfocuser = function(focuser) {
        panel.onfocuser(focuser);
        
        $.each(focuser, function(i, f) {
            switch (f.type) {
            case ATTITUDE_UP:
                focus_up.append("<img src='" + f.ucover + "' />");
                break;
//             case ATTITUDE_DOWN:
//                 focus_down.append("<img src='" + f.ucover + "' />");
//                 break;
            }
        });
        if (0 == focus_up.html().length)    focus_up.text('无');
//         if (0 == focus_down.html().length)  focus_down.text('无');
    };
    
    return div;
}

})(jQuery)

