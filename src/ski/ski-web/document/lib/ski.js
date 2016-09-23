var ski = {}

// network base
ski.url = {
    api : '/ski-web',
    doc : '/wechat'
};
ski.urlparams = function() {
    var params = {};
    var arrays = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
    for(var i = 0; i < arrays.length; i++) {
        var kv = arrays[i].split('=');
        params[kv[0]] = decodeURIComponent(kv[1]);
    }
    return params;
};
ski.sendto = function(url, data, callback) {
    $.post(url, $.toJSON(data), callback);
};
ski.send = function(inst, data, callback) {
    if (null == data) data = {};
    if ('number' == typeof(inst)) data.inst = inst;
    if ('string' == typeof(inst)) data.inst = parseInt(inst, 16);

    ski.sendto(ski.url.api, data, callback);
};

// permanent
ski.cookie = function(key, val, domain, path, expires)  {
    switch (arguments.length) {
    case 0:
        return $.cookie();
    case 1:
        return $.cookie(key);
    case 2: {
        var origin = $.cookie(key);
        $.cookie(key, val, {path: "/"});
        return origin;
    }
    case 5: {
        var origin = $.cookie(key);
        $.cookie(key, val, {domain : domain, path : path, expires : expires});
        return origin;
    }
    }
};
ski.user = function() {return ski.cookie('user');};

// ui
ski.ui = {
    set_title : function(title) {
        //利用iframe的onload事件刷新页面
        document.title = title;
        var iframe = document.createElement('iframe');
        iframe.style.visibility = 'hidden';
        iframe.style.width = '1px';
        iframe.style.height = '1px';
        iframe.onload = function () {
            setTimeout(function () {
                document.body.removeChild(iframe);
            }, 0);
        };
        document.body.appendChild(iframe);
    },

    show_toast : function(text) {
        if ($('#toast').length == 0) {
            $('body').append("<div class='weui_loading_toast' id='toast' style='display: none;'>"
                               + "<div class='weui_mask_transparent'></div>"
                               + "<div class='weui_toast'>"
                                   + "<div class='weui_loading'>"
                                      + " <div class='weui_loading_leaf weui_loading_leaf_0'></div>"
                                      + " <div class='weui_loading_leaf weui_loading_leaf_1'></div>"
                                      + " <div class='weui_loading_leaf weui_loading_leaf_2'></div>"
                                      + " <div class='weui_loading_leaf weui_loading_leaf_3'></div>"
                                      + " <div class='weui_loading_leaf weui_loading_leaf_4'></div>"
                                      + " <div class='weui_loading_leaf weui_loading_leaf_5'></div>"
                                      + " <div class='weui_loading_leaf weui_loading_leaf_6'></div>"
                                      + " <div class='weui_loading_leaf weui_loading_leaf_7'></div>"
                                      + " <div class='weui_loading_leaf weui_loading_leaf_8'></div>"
                                      + " <div class='weui_loading_leaf weui_loading_leaf_9'></div>"
                                      + " <div class='weui_loading_leaf weui_loading_leaf_10'></div>"
                                      + " <div class='weui_loading_leaf weui_loading_leaf_11'></div>"
                                   + "</div>"
                                   + "<p class='weui_toast_content' id='toast_text'></p>"
                               + "</div>"
                           + "</div>");
        }
        $('#toast_text').html(text);
        $('#toast').show();
    },

    hide_toast : function() {
        if ($('#toast').length > 0) $('#toast').hide();
    },
    
    show_dialog_alert : function(options) {
        if ($('#dialog_alert').length == 0) {
            $('body').append("<div class='weui_dialog_alert' id='dialog_alert' style='display:none'>"
                               + "<div class='weui_mask'></div>"
                               + "<div class='weui_dialog'>"
                                   + "<div class='weui_dialog_hd'><strong class='weui_dialog_title' id='dialog_alert_head'></strong></div>"
                                   + "<div class='weui_dialog_bd' id='dialog_alert_body'></div>"
                                   + "<div class='weui_dialog_ft'>"
                                       + "<a class='weui_btn_dialog primary' id='dialog_alert_yes'>确定</a>"
                                   + "</div>"
                               + "</div>"
                           + "</div>");
        }
        if (undefined != options.head) $('#dialog_alert_head').html(options.head);
        else $('#dialog_alert_head').html('');
        if (undefined != options.body) $('#dialog_alert_body').html(options.body);
        else $('#dialog_alert_body').html('');
        if (undefined != options.yes)  $('#dialog_alert_yes').attr('href', "javascript: $('#dialog_alert').hide(); var yes=" + options.yes.toString() + "; yes();");
        else $('#dialog_alert_yes').attr('href', "javascript: $('#dialog_alert').hide();");

        $('#dialog_alert').show();
    },

    hide_dialog_alert : function() {
        if ($('#dialog_alert').length > 0) $('#dialog_alert').hide();
    },
    
    show_dialog_confirm : function(options) {
        if ($('#dialog_confirm').length == 0) {
            $('body').append("<div class='weui_dialog_confirm' id='dialog_confirm' style='display:none'>"
                               + "<div class='weui_mask'></div>"
                               + "<div class='weui_dialog'>"
                                   + "<div class='weui_dialog_hd'><strong class='weui_dialog_title' id='dialog_confirm_head'></strong></div>"
                                   + "<div class='weui_dialog_bd' id='dialog_confirm_body'></div>"
                                   + "<div class='weui_dialog_ft'>"
                                       + "<a class='weui_btn_dialog default' id='dialog_confirm_no'>取消</a>"
                                       + "<a class='weui_btn_dialog primary' id='dialog_confirm_yes'>确定</a>"
                                   + "</div>"
                               + "</div>"
                           + "</div>");
        }
        if (undefined != options.head) $('#dialog_confirm_head').html(options.head);
        else $('#dialog_confirm_head').html('');
        if (undefined != options.body) $('#dialog_confirm_body').html(options.body);
        else $('#dialog_confirm_body').html('');
        if (undefined != options.yes)  $('#dialog_confirm_yes').attr('href', "javascript: $('#dialog_confirm').hide(); var yes=" + options.yes.toString() + "; yes();");
        else $('#dialog_confirm_yes').attr('href', "javascript: $('#dialog_confirm').hide();");
        if (undefined != options.no)   $('#dialog_confirm_no').attr('href', "javascript: $('#dialog_confirm').hide(); var no=" + options.no.toString() + "; no();");
        else $('#dialog_confirm_no').attr('href', "javascript: $('#dialog_confirm').hide();");

        $('#dialog_confirm').show();
    },

    hide_dialog_confirm : function() {
        if ($('#dialog_confirm').length > 0) $('#dialog_confirm').hide();
    },

    cover : function(width, height, url) {
        var cover = $("<div></div>");
        cover.css('width',  width + 'px');
        cover.css('height', height + 'px');
        cover.append("<img width='" + width + "px' height='" + height + "px' src='" + url + "'></img>");
        /*
        cover.append("<div style='position: absolute; z-index: 8; top: 0px; left: 0px; right: 0px; bottom: 0px;"
                + "background: -webkit-linear-gradient(290deg, rgba(255, 255, 255, 0.6), rgba(255, 255, 255, 0));"
                + "background:        -linear-gradient(160deg, rgba(255, 255, 255, 0.6), rgba(255, 255, 255, 0));"
                + "'></div>");
        cover.append("<div style='position: absolute; z-index: 9; top: 0px; left: 0px; right: 0px; bottom: 0px; border-top: 1px solid lightgray; border-left: 1px solid lightgray; border-bottom: 1px solid black; border-right: 1px solid black'></div>");
        */
        return cover;
    },

    cover_beautiful : function(width, height, url) {
        var cover = $("<div></div>");
        cover.css('width',  width + 'px');
        cover.css('height', height + 'px');
        cover.append("<img width='" + width + "px' height='" + height + "px' src='" + url + "'></img>");
        cover.append("<div style='position: absolute; z-index: 8; top: 0px; left: 0px; right: 0px; bottom: 0px;"
                + "background: -webkit-linear-gradient(290deg, rgba(255, 255, 255, 0.6), rgba(255, 255, 255, 0));"
                + "background:        -linear-gradient(160deg, rgba(255, 255, 255, 0.6), rgba(255, 255, 255, 0));"
                + "'></div>");
        cover.append("<div style='position: absolute; z-index: 9; top: 0px; left: 0px; right: 0px; bottom: 0px; border-top: 1px solid lightgray; border-left: 1px solid lightgray; border-bottom: 1px solid black; border-right: 1px solid black'></div>");
        return cover;
    },
    
    text_expandable : function(text, height) {
        var id=new Date().getTime();
        var id_txt = 'txt_' + id;   // real text
        var id_bak = 'bak_' + id;   // backup text
        var id_btn = 'btn_' + id;   // expand button
        var l_open  = '▼展开';
        var l_close = '▲收起';

        var div = $("<div></div>");
        div.append("<div id='"+id_txt+"' style='width:100%; height:"+height+"px; overflow:hidden; text-overflow:ellipsis;'>"+text+"</div>");

        var fn = "if ($('#"+id_txt+"').height() > "+height+") {" // close
                   + "$('#"+id_btn+"').text('"+l_open+"');"
                   + "$('#"+id_txt+"').animate({height : '"+height+"px'});"
               + "} else {" // open
                   + "$('#"+id_bak+"').show();"
                   + "var h=$('#"+id_bak+"').outerHeight(true);"
                   + "$('#"+id_bak+"').hide();"
                   + "$('#"+id_btn+"').text('"+l_close+"');"
                   + "$('#"+id_txt+"').animate({height : h+'px'});"
               + "}";
        div.append("<div style='width:100%; text-align:right'><a id='"+id_btn+"' href='#' onclick=\""+fn+"\">"+l_open+"</a></div>");
        div.append("<div id='"+id_bak+"' style='width:100%; display:none; opacity: 0.0'>"+text+"</div>"); // append a copy to calculate real height
        return div;
    },

    message_left : function(message) {
        var width_total   = $(document.body).width();
        var width_edge    = 80;
        var width_space   = width_total / 5;
        var width_message = width_total - width_space - width_edge;
        var div = $('<div></div>');
        div.css('width', width_total+'px');
        div.css('margin-top',    '16px');
        div.css('margin-bottom', '16px');
        div.append("<table style='border-collapse: collapse;'><tr>"
                     + "<td width='"+width_edge+"px' style='vertical-align: top;'>"
                         + "<div style='position: relative; float: right; right: -1px; top: 12px; width: 0px; height: 0px; z-index: 200; border-top: 5px solid transparent; border-right: 6px solid white; border-bottom: 5px solid transparent;'></div>"
                         + "<div style='position: relative; left: 0px; top: 0px;'>"
                            + "<div style='width: 30px; height: 30px; margin-left: auto; margin-right: auto; text-align: center; color: white; background: gray; border: 1px solid black; font-size: 10px; line-height: 10px; word-wrap: break-word;'>"+message.member+"</div>"
                         + "</div>"
                     + "</td>"
                     + "<td width='"+width_message+"px'><div class='weui_btn weui_btn_mini weui_btn_primary' style='max-width: "+width_message+"px; float: left; word-wrap: break-word; text-align: left; color: black; background: white;'>"+message.message+"</div></td>"
                     + "<td width='"+width_space+"px'></td>"
                 + "</tr></table>");
        return div;
    },
    message_right : function(message) {
        var width_total   = $(document.body).width();
        var width_space   = width_total / 5;
        var width_edge    = 80;
        var width_message = width_total - width_space - width_edge;
        var div = $('<div></div>');
        div.css('width', width_total+'px');
        div.css('margin-top',    '16px');
        div.css('margin-bottom', '16px');
        div.append("<table style='border-collapse: collapse;'><tr>"
                     + "<td width='"+width_space+"px'></td>"
                     + "<td width='"+width_message+"px'><div class='weui_btn weui_btn_mini weui_btn_primary' style='max-width: "+width_message+"px; float: right; word-wrap: break-word; text-align: left;'>"+message.message+"</div></td>"
                     + "<td width='"+width_edge+"px' style='vertical-align: top;'>"
                         + "<div style='position: relative; float: left; left: -1px; top: 12px; width: 0px; height: 0px; z-index: 200; border-top: 5px solid transparent; border-left: 6px solid rgb(0,190,1); border-bottom: 5px solid transparent;'></div>"
                         + "<div style='position: relative; right: 0px; top: 0px;'>"
                            + "<div style='width: 30px; height: 30px; margin-left: auto; margin-right: auto; text-align: center; color: white; background: gray; border: 1px solid black; font-size: 10px; line-height: 10px; word-wrap: break-word;'>"+message.member+"</div>"
                         + "</div>"
                     + "</td>"
                 + "</tr></table>");
        return div;
    }
};

ski.wechat = {
    pay_prepare : function(callback) {
        ski.sendto(ski.url.api + '/pay/recharge/prepare', {inst : '2104'}, callback);
    },
    pay_apply : function(money, callback) {
        ski.sendto(ski.url.api + '/pay/recharge/apply', {inst : '2104', money : money}, callback);
    },
    pay_refund : function(callback) {
        ski.sendto(ski.url.api + '/pay/refund', {inst : '2104'}, callback);
    }
};

ski.goto = {
    game : function(gid) {
        if ('number' == typeof(gid)) gid = gid.toString(16);
        window.location = ski.url.doc + '/query_game_by_gid.html?gid=' + gid;
    },
    message : function(options) {
        var url = ski.url.doc + '/message.html';
        if (undefined != options.msg_type)      url += '?msg_type=' + options.msg_type;
        if (undefined != options.msg_title)     url += '&msg_title=' + options.msg_title;
        if (undefined != options.msg_content)   url += '&msg_content=' + options.msg_content;
        if (undefined != options.msg_url)       url += '&msg_url=' + options.msg_url;
        window.location = url;
    },
    pay_recharge : function() {
        window.location = ski.url.doc + '/apply_platform_account_money_recharge.html';
    },
    pay_refund : function() {
        window.location = ski.url.doc + '/apply_platform_account_money_refund.html';
    },
    rent_begin : function(gid) {
        if ('number' == typeof(gid)) gid = gid.toString(16);
        window.location = ski.url.doc + '/apply_rent_begin.html?gid=' + gid;
    },
    rent_end : function(oid, csn) {
        if ('number' == typeof(oid)) oid = oid.toString(16);
        if ('number' == typeof(csn)) csn = csn.toString(16);
        window.location = ski.url.doc + '/apply_rent_end.html?oid=' + oid + '&csn=' + csn;
    },
    update_platform_account_map : function() {
        window.location = ski.url.doc + '/update_platform_account_map.html';
    }
};

ski.common = {
    apply_rent_begin : function(gid, type, callback) {
        ski.send('2106', {gid : gid, type : type}, callback);
    },
    apply_rent_end : function(oid, csn, callback) {
        ski.send('2107', {oid : oid, csn : csn}, callback);
    },
    query_game : function(options, callback) {
        ski.send('2001', options, callback);
    },
    query_order : function(oid, csn, callback) {
        if (1 == arguments.length) {
            callback = oid;
            ski.send('2006', null, callback);
        } else ski.send('2006', {oid : oid, csn : csn}, callback);
    },
    query_platform_account : function(options, callback) {
        if (1 == arguments.length) {
            callback = options;
            options = null;
        }
        ski.send('2009', options, callback);
    },
    query_platform_account_map : function(callback) {
        ski.send('200a', null, callback);
    },
    update_platform_account_map : function(options, callback) {
        ski.send('240a', options, callback);
    }
};

// chatroom
ski.chatroom = {
    get : function(options, callback) {
        if (undefined != options.gid) {
            ski.send('2011', {gid : options.gid}, callback);
        }
    },
    join : function(crid, callback) {
        ski.send('2412', {crid : crid}, callback);
    },
    member : function(crid, member, callback) {
        var data = null;
        switch (arguments.length) {
        case 2:
            data = {crid : crid};
            callback = member;
            break;
        case 3:
            data = {crid : crid, member : member};
            break;
        default:
            return;
        }
        ski.send('2012', data, callback);
    },
    message : function(crid, options, callback) {
        var data = null;
        switch (arguments.length) {
        case 2:
            data = {crid : crid};
            callback = options;
            break;
        case 3:
            data = options;
            data.crid = crid;
            break;
        default:
            return;
        }
        ski.send('2013', data, callback);
    }
};

