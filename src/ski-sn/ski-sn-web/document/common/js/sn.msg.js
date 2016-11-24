
(function($) {

sn.msg = {};
sn.msg.data = [];

sn.msg.load = function(pos, len, cb) {
    if (!sn.location) return;
    if (!sn.uid) {
        sn.uid = new Date().getTime();
        fomjar.util.cookie('uid', sn.uid);
    }
    fomjar.net.send(ski.ISIS.INST_QUERY_MESSAGE, {
        lat : sn.location.point.lat,
        lng : sn.location.point.lng,
        pos : pos,
        len : len
    }, function(code, desc) {
        if (0 != code) sn.ui.toast(desc);
        cb(desc);
    });
};

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
        });
    };

    var panel = create_message_panel(msg);
    var detail = create_message_detail(sn.ui.dialog(), msg);
    msg.ui = {};
    msg.ui.panel = panel;
    msg.ui.detail = detail;
    
    var show_detail = function(e) {
        if (e.target.tagName == 'IMG') return;
        
        var dialog = sn.ui.dialog();
        dialog.content.append(detail);
        detail.page_get('detail').onappear();
        dialog.appear();
    };
    panel.find('.mc').bind('click', show_detail);
    panel.find('.mf .button').bind('click', show_detail);

    var div_msg = $([panel[0], detail.find('.msg-panel')[0]]);
    msg.ui.attitude_up = function() {
        if (!sn.user) {
            sn.ui.dialog().disappear();
            setTimeout(function() {sn.ui.login();}, 210)
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
            sn.ui.dialog().disappear();
            setTimeout(function() {sn.ui.login();}, 210)
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
    if (!sn.config('first_send')) {
        show_first_send(dialog);
    } else {
        dialog.addClass('dialog-msg-new');
        dialog.content.append(create_new_message_panel(dialog));
        dialog.appear();
    }
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

function create_message_detail(dialog, msg) {
    var page = sn.ui.page();
    
    var div_detail = $('<div></div>');
    div_detail.addClass('msg-detail');
    
    var panel = create_message_panel(msg);
    panel.find('.mf').remove();
    
    var focus = $('<div></div>');
    focus.addClass('focus');
    focus.append(dialog.t2('支持'));
    var focus_up = $('<div></div>');
    focus.append(focus_up);
    focus.append(dialog.t2('回复'));

    var replys = $('<div></div>');
    replys.addClass('list');
    
    div_detail.append([panel, focus, replys]);
    div_detail.ondisappear = function() {dialog.action.clear();}; 
    div_detail.onappear = function() {
        dialog.action.clear();
        dialog.action.add('返回').bind('click', function() {dialog.disappear();});
        dialog.action.add('回复').bind('click', function() {
            if (!sn.user) {
                sn.ui.dialog().disappear();
                setTimeout(function() {sn.ui.login();}, 210)
                return;
            }
            page.page_set('reply');
        });
    };
    
    var div_reply = $('<div></div>');
    div_reply.addClass('msg-reply');
    div_reply.append("<textarea></textarea>");
    div_reply.append(sn.ui.choose_image());
    div_reply.ondisappear = function() {dialog.action.clear();};
    div_reply.onappear = function() {
        dialog.action.clear();
        dialog.action.add('取消').bind('click', function() {page.page_set('detail');});
        var doing = false;
        dialog.action.add('发送').bind('click', function() {
            var text = div_reply.find('textarea').val();
            if (0 == text.length) {
                dialog.shake();
                sn.ui.toast('必须要输入文字');
                return;
            }
            if (/^ +$/.test(text)) {
                dialog.shake();
                sn.ui.toast('必须要输入文字');
                return;
            }
            
            if (doing) return;
            doing = true;
            sn.ui.toast('正在发送', 10000);
            text = new fomjar.util.base64().encode(text);
            var image = div_reply.find('.choose-image img').attr('src');
            fomjar.net.send(ski.ISIS.INST_UPDATE_MESSAGE_REPLY, {
                mid     : msg.data.mid,
                coosys  : 1,
                lat     : sn.location.point.lat,
                lng     : sn.location.point.lng,
                type    : 0,
                text    : text,
                image   : image
            }, function(code, desc) {
                doing = false;
                if (0 == code) {
                    load_message_reply(msg);
                    page.page_set('detail');
                    sn.ui.toast('发送成功');
                } else {
                    dialog.shake();
                    sn.ui.toast(desc);
                }
            });
        });
    };
    
    page.page_append('detail',  div_detail);
    page.page_append('reply',   div_reply);
    
    page.onfocuser = function(focuser) {
        panel.onfocuser(focuser);
        
        focus_up.children().remove();
        $.each(focuser, function(i, f) {
            if (!f.ucover) f.ucover = 'res/user.png';
            
            switch (f.type) {
            case ATTITUDE_UP:
                focus_up.append(sn.ui.cover(f.ucover));
                break;
            }
        });
        if (0 == focus_up.html().length)    focus_up.append('<div>无</div>');
    };
    page.onreplyer = function(replyer) {
        replys.children().remove();
        $.each(replyer, function(i, r) {
            if (!r.ucover) r.ucover = 'res/user.png';
        
            var d = $('<div></div>');
            d.addClass('reply');
            d.append(sn.ui.cover(r.ucover));
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
    
    return page;
}

function get_distance_description(distance) {
    var i = 0;
    
    i = (distance / 1000 / 10000).toFixed(0);
    if (1 <= i) return i + '万公里';
    
    i = (distance / 1000 / 1000).toFixed(0);
    if (1 <= i) return i + '千公里';
    
    i = (distance / 1000 / 100).toFixed(0);
    if (1 <= i) return i + '百公里';
    
    i = (distance / 1000).toFixed(1);
    if (1 <= i) return i + '公里';
    
    i = (distance / 100).toFixed(0);
    if (1 <= i) return i + '00米';
    
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

function show_first_send(dialog) {
    var div = $('<div></div>');
    div.addClass('first-send');
    div.append(dialog.h2('声呐礼仪'));
    div.append(dialog.p1("<p><b>收获<img src='res/msg-up.png' />:</b>发周围小伙伴都喜欢的消息会获得<img src='res/msg-up.png' />。<img src='res/msg-up.png' />的越多，就会有更多人看到！</p>"));
    div.append(dialog.p1("<p><b>不要被<img src='res/msg-down.png' />:</b>如果消息其他人不喜欢／觉得不合适会获得<img src='res/msg-down.png' />。<img src='res/msg-down.png' />收到一定数量，您的消息会被删除哦</p>"));
    div.append(dialog.p1("<p><b>请不要</b>针对其他小伙伴，进行人身攻击，隐私曝光</p>"));
    div.append(dialog.p1("<p><b>请不要</b>发广告，或者重复发消息刷屏</p>"));
    
    dialog.content.append(div);
    dialog.action.add('拒绝').bind('click', function() {dialog.disappear();});
    dialog.action.add_default('接受').bind('click', function() {
        sn.config('first_send', 'true');
        dialog.disappear();
        setTimeout(function() {
            dialog.addClass('dialog-msg-new');
            dialog.content.append(create_new_message_panel(dialog));
            dialog.appear();
        }, 210);
    });
    
    dialog.appear();
}

function create_new_message_panel(dialog) {
    var div = $('<div></div>');
    
    div.append(dialog.t1("<img src='res/msg-dist.png'/><div>" + sn.location.address + "</div>"));
    div.append(dialog.p1("<textarea placeholder='想法 / 问询 / 活动 / 段子'></textarea>"));
    div.append(dialog.p1(sn.ui.choose_image()));
    
    var div_tex = div.find('>*:nth-child(2) textarea');
    var div_ima = div.find('>*:nth-child(2) img');
    
    dialog.action.add('取消').bind('click', function() {dialog.disappear();});
    var doing = false;
    dialog.action.add_default('发送').bind('click', function() {
        var text    = div_tex.val();
        var image   = div_ima.attr('src');
        if (0 == text.length) {
            sn.ui.toast('必须要输入文字');
            dialog.shake();
            return;
        }
        if (/^ +$/.test(text)) {
            sn.ui.toast('必须要输入文字');
            dialog.shake();
            return;
        }
        
        if (doing) return;
        doing = true;
        sn.ui.toast('正在发送');
        text = new fomjar.util.base64().encode(text);
        fomjar.net.send(ski.ISIS.INST_UPDATE_MESSAGE, {
            coosys  : 1,
            lat     : sn.location.point.lat,
            lng     : sn.location.point.lng,
            type    : 0,
            text    : text,
            image   : image
        }, function(code, desc) {
            doing = false;
            if (0 == code) {
                dialog.disappear();
                sn.msg.reload();
            } else {
                dialog.shake();
                sn.ui.toast(desc);
            }
        });
    });
    
    return div;
}

})(jQuery)
