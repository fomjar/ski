
(function($) {

sn.msg = {};
sn.msg.data = [];

var ATTITUDE_NONE   = 0;
var ATTITUDE_UP     = 1;
var ATTITUDE_DOWN   = 2;

sn.msg.wrap = function(data) {
    var msg = {};
    
    msg.data = data;
    if (!data.ucover) data.ucover = 'res/user.png';
    msg.data.attitude = function() {
        if (!sn.user) return null;
        if (!msg.data.focuser) msg.data.focuser = [];
        var a = null;
        $.each(msg.data.focuser, function(i, f) {
            if (f.uid == sn.uid) {
                a = f;
                return false;
            }
        });
        if (!a) {
            a = {mid : msg.data.mid, uid : sn.uid, type : ATTITUDE_NONE};
            msg.data.focuser.push(a);
        }
        return a;
    };
    msg.data.attitude_update = function() {
        fomjar.net.send(ski.ISIS.INST_UPDATE_MESSAGE_FOCUS, {
            mid     : msg.data.mid,
            type    : msg.data.attitude().type
        }, function(code, desc) {
            if (0 != code) {
                sn.ui.toast('操作失败');
            }
            sn.ui.toast('操作成功');
            load_message_focus(msg);
        })
    };

    var panel = create_message_panel(msg);
    var detail = create_message_detail(msg);
    msg.ui = {};
    msg.ui.panel = panel;
    msg.ui.detail = detail;
    
    var show_detail = function(e) {
        if (e.target.tagName == 'IMG') return;
        
        var dialog = sn.ui.dialog();
        dialog.append(detail);
        dialog.appear();
    };
    panel.find('.mc').bind('click', show_detail);
    panel.find('.mf .button').bind('click', show_detail);

    var div_msg = $([panel[0], detail.find('.msg-panel')[0]]);
    msg.ui.attitude_up = function() {
        if (!sn.user) {
            sn.ui.dialog().children().detach();
            sn.ui.login();
            return;
        }
        var a = msg.data.attitude();
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
        msg.data.attitude_update();
    };
    msg.ui.attitude_down = function() {
        if (!sn.user) {
            sn.ui.dialog().children().detach();
            sn.ui.login();
            return;
        }
        var a = msg.data.attitude();
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
        msg.data.attitude_update();
    };
    
    load_message_focus(msg);
    
    load_message_reply(msg);
    
    return msg;
};

sn.msg.new = function() {
    var dialog = sn.ui.dialog();
    dialog.addClass('dialog-msg-new');
    dialog.append(create_new_message_panel(dialog));
    dialog.appear();
}

function create_message_panel(msg) {
    var div = $('<div></div>');
    div.addClass('msg-panel');
    var mc = $('<div></div>');
    mc.addClass('mc');
    var ma = $('<div></div>');
    ma.addClass('ma');
    
    var mh = $('<div></div>');
    mh.addClass('mh');
    mh.append(sn.ui.cover(msg.data.ucover));
    mh.append('<div>' + msg.data.uname + '</div>');
    
    var mb = $('<div></div>');
    mb.addClass('mb');
    if (0 < msg.data.mtext.length)  mb.append('<div>' + new fomjar.util.base64().decode(msg.data.mtext)  + '</div>');
    if (0 < msg.data.mimage.length) {
        mb.append("<img src='" + msg.data.mimage + "' / >");
        sn.ui.browse(mb.find('img'));
    }
    mb.append("<div class='ass'><img src='res/msg-time.png'/><div>" + get_time_description(msg.data.second) + "</div><img src='res/msg-dist.png'/><div>" + get_distance_description(msg.data.distance) + "</div></div>")
    
    var mf = $('<div></div>');
    mf.addClass('mf');
    mf.append("<div class='button'>回复(" + msg.data.reply + ")</div>");
    
    mc.append([mh, mb, mf]);
    
    ma.append("<div></div><div>" + msg.data.focus + "</div><div></div>");
    ma.find('div:nth-child(1)').bind('click', function() {msg.ui.attitude_up();});
    ma.find('div:nth-child(3)').bind('click', function() {msg.ui.attitude_down();});
    
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
        switch (msg.data.attitude().type) {
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

function create_message_detail(msg) {
    var dialog = sn.ui.dialog();

    var div = $('<div></div>');
    div.addClass('msg-detail');
    
    var content = $('<div></div>');
    content.addClass('ct');
    
    var panel = create_message_panel(msg);
    panel.find('.mf').remove();
    
    var focus = $('<div></div>');
    focus.addClass('focus');
    focus.append("<div>支持</div>");
    var focus_up = $('<div></div>');
    focus.append(focus_up);
    focus.append("<div>回复</div>");
//     focus.append("<div>反对</div>");
//     var focus_down = $('<div></div>');
//     focus.append(focus_down);

    var replys = $('<div></div>');
    replys.addClass('list');
    
    var send = $('<div></div>');
    send.addClass('send');
    send.append("<textarea></textarea>");
    send.append(sn.ui.choose_image(1024 * 1024 * 2, function() {}, function() {dialog.shake();}));
    send.append("<div><div class='button'>取消</div><div class='button button-default'>发送</div></div>");
    var div_can = send.find('.button:nth-child(1)');
    var div_sen = send.find('.button:nth-child(2)');
    div_can.bind('click', function() {
        send.css('opacity', '0');
        setTimeout(function() {send.hide();}, 500);
    });
    div_sen.doing = false;
    div_sen.bind('click', function() {
        var text = send.find('textarea').val();
        if (0 == text.length) {
            dialog.shake();
            return;
        }
        if (/^ +$/.test(text)) {
            dialog.shake();
            return;
        }
        
        if (div_sen.doing) return;
        
        div_sen.doing = true;
        div_sen.addClass('button-disable');
        
        text = new fomjar.util.base64().encode(text);
        image = send.find('.choose-image img').attr('src');
        fomjar.net.send(ski.ISIS.INST_UPDATE_MESSAGE_REPLY, {
            mid     : msg.data.mid,
            coosys  : 1,
            lat     : sn.location.point.lat,
            lng     : sn.location.point.lng,
            type    : 0,
            text    : text,
            image   : image
        }, function(code, desc) {
            div_sen.doing = false;
            div_sen.removeClass('button-disable');
            if (0 == code) {
                div_can.trigger('click');
                load_message_reply(msg);
            } else {
                dialog.shake();
                sn.ui.toast(desc);
            }
        });
    });
    send.hide();
    
    var action = $('<div></div>');
    action.addClass('action');
    action.append("<div class='button'>返回</div>");
    action.append("<div class='button'>回复</div>");
    action.find('.button:nth-child(1)').bind('click', function() {dialog.disappear();});
    action.find('.button:nth-child(2)').bind('click', function() {
        if (!sn.user) {
            dialog.children().detach();
            sn.ui.login();
            return;
        }
        
        send.css('opacity', '0');
        send.show();
        send.css('opacity', '1');
    });
    
    content.append([panel, focus, replys]);
    div.append([content, send, action]);
    
    div.onfocuser = function(focuser) {
        panel.onfocuser(focuser);
        
        focus_up.children().remove();
        $.each(focuser, function(i, f) {
            if (!f.ucover) f.ucover = 'res/user.png';
            
            switch (f.type) {
            case ATTITUDE_UP:
                focus_up.append("<img src='" + f.ucover + "' />");
                break;
//             case ATTITUDE_DOWN:
//                 focus_down.append("<img src='" + f.ucover + "' />");
//                 break;
            }
        });
        if (0 == focus_up.html().length)    focus_up.append('<div>无</div>');
//         if (0 == focus_down.html().length)  focus_down.append('<div>无</div>');
    };
    div.onreplyer = function(replyer) {
        replys.children().remove();
        $.each(replyer, function(i, r) {
            if (!r.ucover) r.ucover = 'res/user.png';
        
            var d = $('<div></div>');
            d.addClass('reply');
            d.append("<img src='" + r.ucover + "' />");
            d.append('<div>' + r.uname + '</div>');
            d.append('<div>' + r.time.substring(5, 16) + '</div>');
            if (0 < r.mtext.length) d.append('<div>' + new fomjar.util.base64().decode(r.mtext) + '</div>');
            if (0 < r.mimage.length) {
                var img = $('<img />');
                img.attr('src', r.mimage);
                d.append(img);
                sn.ui.browse(img);
            }
            replys.append(d);
        });
    }
    
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

function load_message_focus(msg) {
    fomjar.net.send(ski.ISIS.INST_QUERY_MESSAGE_FOCUS, {
        mid : msg.data.mid
    }, function(code, desc) {
        if (0 != code) return;
        
        msg.data.focuser = desc;
        
        msg.ui.panel.onfocuser(desc);
        msg.ui.detail.onfocuser(desc);
    });
}

function load_message_reply(msg) {
    fomjar.net.send(ski.ISIS.INST_QUERY_MESSAGE_REPLY, {
        mid : msg.data.mid
    }, function(code, desc) {
        if (0 != code) return;
        
        msg.data.replyer = desc;
        
        msg.ui.detail.onreplyer(desc);
    });
}

function create_new_message_panel(dialog) {
    var div = $('<div></div>');
    div.addClass('msg-new');
    
    div.append("<div><img src='res/msg-dist.png'/><div>" + sn.location.address + "</div></div>");
    
    var div_content = $('<div></div>');
    div_content.append("<textarea placeholder='想法 / 问询 / 活动 / 段子'></textarea>");
    div_content.append(sn.ui.choose_image(1024 * 1024 * 2, function(){}, function(){dialog.shake();}));
    
    div.append(div_content);
    div.append("<div><div class='button'>取消</div><div class='button button-default'>发送</div></div>");
    
    var div_tex = div.find('>*:nth-child(2) textarea');
    var div_ima = div.find('>*:nth-child(2) img');
    var div_can = div.find('>*:nth-child(3) .button:nth-child(1)');
    var div_sen = div.find('>*:nth-child(3) .button:nth-child(2)');
    
    div_can.bind('click', function() {dialog.disappear();});
    div_sen.doing = false;
    div_sen.bind('click', function() {
        var text    = div_tex.val();
        var image   = div_ima.attr('src');
        if (0 == text.length) {
            dialog.shake();
            return;
        }
        if (/^ +$/.test(text)) {
            dialog.shake();
            return;
        }
        
        if (div_sen.doing) return;
        
        div_sen.doing = true;
        div_sen.addClass('button-disable');
        
        text = new fomjar.util.base64().encode(text);
        fomjar.net.send(ski.ISIS.INST_UPDATE_MESSAGE, {
            coosys  : 1,
            lat     : sn.location.point.lat,
            lng     : sn.location.point.lng,
            type    : 0,
            text    : text,
            image   : image
        }, function(code, desc) {
            div_sen.doing = false;
            div_sen.removeClass('button-disable');
            if (0 == code) {
                dialog.disappear();
            } else {
                dialog.shake();
                sn.ui.toast(desc);
            }
        });
    });
    
    return div;
}

})(jQuery)
