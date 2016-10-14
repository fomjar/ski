
fomjar.framework.phase.append('dom', build_msg);
fomjar.framework.phase.append('dom', build_cp);
fomjar.framework.phase.append('dom', build_cp_tlk);
fomjar.framework.phase.append('dom', build_cp_fns);

fomjar.framework.phase.append('ren', wx_config);
fomjar.framework.phase.append('ren', setup);

function build_msg() {
    var msg = $('<div></div>');
    msg.addClass('cr-msg');

    $('.wechat .frame .body').append(msg);
}

function build_cp() {
    var cp = $('<div></div>')
    cp.addClass('cr-cp');
    $('.wechat .frame .body').append(cp);
}

function build_cp_tlk() {
    var cp_talk = $('<div></div>');

    var tgl_tlk = $('<div></div>');
    tgl_tlk.addClass('toggle');
    tgl_tlk.append("<img src='res/cr-tgl-voc.png' style='width : 50%;' />");
    tgl_tlk.append("<img src='res/cr-tgl-kbd.png' style='width : 70%; display : none;' />");
    tgl_tlk.bind('click', toggle_talk);

    var tlk_kbd = $('<input>');
    tlk_kbd.addClass('talk');
    tlk_kbd.attr('type', 'text');
    tlk_kbd.bind('keydown', function() {if(13 == event.keyCode) talk_keyboard();});
    var tlk_voc = $('<div></div>');
    tlk_voc.addClass('talk');
    tlk_voc.text('按住 说话');
    tlk_voc.bind('touchstart', talk_voice_start);
    tlk_voc.bind('mousedown', talk_voice_start);
    tlk_voc.bind('touchend', talk_voice_end);
    tlk_voc.bind('mouseup', talk_voice_end);

    var tgl_fns = $('<div></div>');
    tgl_fns.addClass('toggle');
    tgl_fns.append("<img src='res/cr-tgl-fns.png'/>");
    tgl_fns.bind('click', toggle_functions);

    cp_talk.append(tgl_tlk);
    cp_talk.append(tlk_kbd);
    cp_talk.append(tlk_voc);
    cp_talk.append(tgl_fns);

    $('.cr-cp').append(cp_talk);
}

function build_cp_fns() {
    var cp_fns = $('<div></div>');

    var fn_pho = $('<div></div>');
    fn_pho.append("<div><img src='res/cr-fn-photo.png' /></div>");
    fn_pho.append('<div>照片</div>');
    fn_pho.bind('click', function() {
        wx.chooseImage({
            count       : 3, // 默认9
            sizeType    : ['compressed'], // 可以指定是原图还是压缩图，默认二者都有
            sourceType  : ['album'], // 可以指定来源是相册还是相机，默认二者都有
            success     : function (res) {
                var localIds = res.localIds; // 返回选定照片的本地ID列表，localId可以作为img标签的src属性显示图片
                for (var i in localIds) {
                    var localId = localIds[i];
                    wx.uploadImage({
                        localId: localId, // 需要上传的图片的本地ID，由chooseImage接口获得
                        isShowProgressTips: 1, // 默认为1，显示进度提示
                        success: function (res) {
                            var serverId = res.serverId; // 返回图片的服务器端ID
                            fomjar.net.send(fomjar.net.ISIS.INST_ECOM_UPDATE_CHATROOM_MESSAGE, {
                                crid : g_cr.cr.crid,
                                type : 1,
                                message : serverId
                            }, function (code, desc) {});
                        }
                    });
                }
            }
        });

    });

    var fn_cmr = $('<div></div>');
    fn_cmr.append("<div><img src='res/cr-fn-camera.png'/></div>");
    fn_cmr.append('<div>相机</div>');
    fn_cmr.bind('click', function() {
        wx.chooseImage({
            count       : 3, // 默认9
            sizeType    : ['compressed'], // 可以指定是原图还是压缩图，默认二者都有
            sourceType  : ['camera'], // 可以指定来源是相册还是相机，默认二者都有
            success     : function (res) {
                var localIds = res.localIds; // 返回选定照片的本地ID列表，localId可以作为img标签的src属性显示图片
                for (var i in localIds) {
                    var localId = localIds[i];
                    wx.uploadImage({
                        localId: localId, // 需要上传的图片的本地ID，由chooseImage接口获得
                        isShowProgressTips: 1, // 默认为1，显示进度提示
                        success: function (res) {
                            var serverId = res.serverId; // 返回图片的服务器端ID
                            fomjar.net.send(fomjar.net.ISIS.INST_ECOM_UPDATE_CHATROOM_MESSAGE, {
                                crid : g_cr.cr.crid,
                                type : 1,
                                message : serverId
                            }, function (code, desc) {});
                        }
                    });
                }
            }
        });
    });

    cp_fns.append([fn_pho, fn_cmr]);

    $('.cr-cp').append(cp_fns);
}

function toggle_talk() {
    var t1 = $('.talk:nth-child(2)'); // k
    var t2 = $('.talk:nth-child(3)'); // v
    if (t1.is(':visible')) { 
        $($('.toggle img')[0]).hide();
        $($('.toggle img')[1]).show();
        t1.css('opacity', '0');
        t2.css('opacity', '0');
        t2.show();
        setTimeout(function() {t1.hide();t2.css('opacity', '1')}, 200);
    } else {
        $($('.toggle img')[0]).show();
        $($('.toggle img')[1]).hide();
        t1.css('opacity', '0');
        t2.css('opacity', '0');
        t1.show();
        setTimeout(function() {t2.hide();t1.css('opacity', '1')}, 200);
    }
}

function toggle_functions() {
    var cp = $('.cr-cp');
    var msg = $('.cr-msg');
    var tlk = $('.cr-cp >div:nth-child(1)');
    var fns = $('.cr-cp >div:nth-child(2)');
    if (cp.height() > 100) { // down
        cp.css('height', '3em');
        msg.css('bottom', '3em');
    } else { // up
        cp.css('height', '13em');
        msg.css('bottom', '13em');
        setTimeout(scroll_message_bottom, 200);
    }
}

function talk_keyboard() {
    var txt = $('.cr-cp input').val();
    if (0 == txt.length) return;

    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_UPDATE_CHATROOM_MESSAGE, {
        crid : g_cr.cr.crid,
        type : 0,
        message : fomjar.util.base64.encode(txt)
    }, function (code, desc) {
        if (0 != code) wechat.show_toast('发送失败', 15000);
        else $('.cr-cp input').val('');
    });
}

function talk_voice_start() {
    var talk = $('.talk:nth-child(3)');
    talk.text('松开 结束');

    wx.startRecord();
}

function talk_voice_end() {
    var talk = $('.talk:nth-child(3)');
    talk.text('按住 说话');

    wx.stopRecord({
        success: function (res) {
            var localId = res.localId;
            wx.uploadVoice({
                localId: localId, // 需要上传的音频的本地ID，由stopRecord接口获得
                isShowProgressTips: 1, // 默认为1，显示进度提示
                success: function (res) {
                    var serverId = res.serverId; // 返回音频的服务器端ID
                    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_UPDATE_CHATROOM_MESSAGE, {
                        crid : g_cr.cr.crid,
                        type : 2,
                        message : serverId
                    }, function (code, desc) {});
                }
            });
        }
    });
}
wx.onVoiceRecordEnd({
    // 录音时间超过一分钟没有停止的时候会执行 complete 回调
    complete: function (res) {
        var localId = res.localId;
        wx.uploadVoice({
            localId: localId, // 需要上传的音频的本地ID，由stopRecord接口获得
            isShowProgressTips: 1, // 默认为1，显示进度提示
            success: function (res) {
                var serverId = res.serverId; // 返回音频的服务器端ID
                fomjar.net.send(fomjar.net.ISIS.INST_ECOM_UPDATE_CHATROOM_MESSAGE, {
                    crid : g_cr.cr.crid,
                    type : 2,
                    message : serverId
                }, function (code, desc) {});
            }
        });
    }
});

function wx_config() {
    var appId;
    var timestamp;
    var nonceStr;
    var signature;
    // prepare
    fomjar.net.sendto(fomjar.net.api()+'/pay/recharge/prepare', {inst : fomjar.net.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY}, function(code, desc) {
        appId       = desc.appid;
        timestamp   = desc.timestamp;
        nonceStr    = desc.noncestr;
        signature   = desc.signature;
        wx.config({
            'debug'     : false,        // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
            'appId'     : appId,        // 必填，公众号的唯一标识
            'timestamp' : timestamp,    // 必填，生成签名的时间戳
            'nonceStr'  : nonceStr,     // 必填，生成签名的随机串
            'signature' : signature,    // 必填，签名，见附录1
            'jsApiList' : [
                    'chooseImage',
                    'uploadImage',
                    'startRecord',
                    'stopRecord',
                    'onVoiceRecordEnd',
                    'uploadVoice'
            ]            // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
        });
    });
}

var g_cr = {};

function setup() {
    wechat.show_toast('正在获取个人信息...');
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT, function(code, desc) {
        wechat.hide_toast();
        if (0 != code) {wechat.show_toast('获取失败', 10000); return;}
        g_cr.me = desc;

    var gid = fomjar.util.args().gid;
    wechat.show_toast('正在获取聊天室...');
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_CHATROOM, {gid : gid}, function(code, desc) {
        wechat.hide_toast();
        if (0 != code) {wechat.show_toast('获取失败', 10000); return;}
        g_cr.cr = desc[0]; // 先取第一个

    wechat.show_toast('正在加入聊天室...');
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_UPDATE_CHATROOM_MEMBER, {crid : g_cr.cr.crid}, function(code, desc) {
        wechat.hide_toast();
        if (0 != code) {wechat.show_toast('加入失败', 10000); return;}

    wechat.show_toast('正在加载成员...');
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_CHATROOM_MEMBER, {crid : g_cr.cr.crid}, function(code, desc) {
        wechat.hide_toast();
        if (0 != code) {wechat.show_toast('加载失败', 10000); return;}
        g_cr.mb = desc;

    wechat.show_toast('正在加载消息...');
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_CHATROOM_MESSAGE, {crid : g_cr.cr.crid, count : 100}, function(code, desc) {
        wechat.hide_toast();
        if (0 != code) {wechat.show_toast('加载失败', 10000); return;}
        g_cr.msg = desc;

        setup_chatroom();
        setup_message();
        receive_message();
    });

    });

    });

    });

    });
}

function setup_chatroom() {
    fomjar.util.title(g_cr.cr.name+'('+g_cr.mb.length+')');
}

function setup_message() {
    $.each(g_cr.msg, function(i, m) {
        $('.cr-msg').append(create_msg(m));
    })
    scroll_message_bottom();
}

function receive_message() {
    if ('undefined' == typeof g_cr.msg) g_cr.msg = [];
    g_cr.last = new Date().format('yyyy-MM-dd HH:mm:ss');
    setInterval(function() {
        fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_CHATROOM_MESSAGE, {crid : g_cr.cr.crid, time : g_cr.last}, function(code, desc) {
            if (0 == code) {
                var msgs = desc;
                $.each(msgs, function(i, m) {
                    $('.cr-msg').append(create_msg(m));
                    scroll_message_bottom();
                    g_cr.msg.push(m);
                    g_cr.last = m.time;
                });
            }
        });
    }, 1500);
}

function scroll_message_bottom() {
    $('.cr-msg').scrollTop(-1>>>2);
}

function create_msg(m) {
    var msg = $('<div></div>');
    msg.addClass('msg');

    var table = $('<table></table>');
    var tr = $('<tr></tr>');

    var msg_cnt = null;
    switch (m.type) {
    case 0:     // txt
        msg_cnt = create_msg_txt(m);
        break;
    case 1:     // img
        msg_cnt = create_msg_img(m);
        break;
    case 2:     // voc
        msg_cnt = create_msg_voc(m);
        break;
    }
    msg_cnt.addClass('cnt');

    if (is_member_sys(m.member)) {
        table.addClass('sys');

        tr.append('<td></td>');

        var td = $('<td></td>');
        msg_cnt.text(msg_cnt.text().substring(0, msg_cnt.text().length-3));
        td.append(msg_cnt);
        tr.append(td);

        tr.append('<td></td>');
    } else {
        table.addClass('usr');
        if (is_member_self(m.member)) {
            tr.addClass('rig');

            tr.append('<td></td>');

            var td = $('<td></td>');
            td.append(msg_cnt);
            tr.append(td);

            var td = $('<td></td>');
            td.addClass('uc');
            td.append("<img src='"+m.mi.url_cover+"' />");
            tr.append(td);
        } else {
            tr.addClass('lef');

            var td = $('<td></td>');
            td.addClass('uc');
            td.append("<img src='"+m.mi.url_cover+"' />");
            tr.append(td);

            var td = $('<td></td>');
            td.append(msg_cnt);
            tr.append(td);

            tr.append('<td></td>');
        }
    }

    table.append(tr);
    msg.append(table);

    return msg;
}

function create_msg_txt(m) {
    var cnt = $('<div></div>');
    cnt.addClass('txt');
    cnt.text(fomjar.util.base64.decode(m.message));
    return cnt;
}

function create_msg_img(m) {
    var cnt = $('<div></div>');
    cnt.addClass('img');
    cnt.append("<img src='"+fomjar.net.api()+"?inst="+fomjar.net.ISIS.INST_ECOM_QUERY_CHATROOM_MESSAGE.toString(16)+"&crid="+g_cr.cr.crid.toString(16)+"&mid="+m.mid.toString(16)+"&user="+fomjar.util.user()+"' />");
    cnt.find('img').bind('load', scroll_message_bottom);
    return cnt;
}

function create_msg_voc(m) {
    var cnt = $('<div></div>');
    cnt.addClass('voc');
    var audio = $('<audio></audio>');
    audio.attr('src', fomjar.net.api()+"?inst="+fomjar.net.ISIS.INST_ECOM_QUERY_CHATROOM_MESSAGE.toString(16)+"&crid="+g_cr.cr.crid.toString(16)+"&mid="+m.mid.toString(16)+"&user="+fomjar.util.user());
    var txt = $('<div></div>');
    txt.text('声音');
    cnt.append(txt);
    cnt.append(audio);
    cnt.bind('click', function(){
        audio[0].play();
        txt.text('...');
    });
    audio.bind('ended', function() {
        txt.text('声音');
    })
    return cnt;
}

function is_member_self(m) {return m == g_cr.me.paid;}

function is_member_sys(m) {return m == -1;}


