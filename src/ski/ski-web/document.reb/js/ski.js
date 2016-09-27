define('ski', ['fomjar', 'weui'],
function(fomjar) {
return {
    url : {
        doc : '/wechat'
    },
    
    user : function() {
        return fomjar.cookie('user');
    },

    ui : {
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
            div.append("<div style='width:100%; text-align:right'><a id='"+id_btn+"' href='javascript: void(0);' onclick=\""+fn+"\">"+l_open+"</a></div>");
            div.append("<div id='"+id_bak+"' style='width:100%; display:none; opacity: 0.0'>"+text+"</div>"); // append a copy to calculate real height
            return div;
        },
    
        message_left : function(message) {
            var width_total   = $(document.body).width();
            var width_edge    = 80;
            var width_space   = width_total / 5;
            var width_message = width_total - width_space - width_edge;
            var size_cover    = 36;
            var padding_message = 8;
    		var padding_content = 10;
            var size_font     = 16;
    
            var div = $('<div></div>');
            div.css('width', width_total+'px');
            div.css('padding-top',    padding_message + 'px');
            div.css('padding-bottom', padding_message + 'px');
            div.append("<table style='border-collapse: collapse;'><tr>"
                         + "<td width='"+width_edge+"px' style='vertical-align: top;'>"
                             + "<div>"
                                 + "<div style='position: relative; float: right; right: 0px; top: "+(size_cover/2)+"px; width: 0px; height: 0px; z-index: 2; border-top: 5px solid transparent; border-right: 6px solid #cccccc; border-bottom: 5px solid transparent;'></div>"
                                 + "<div style='position: relative; float: right; right: -7px; top: "+(size_cover/2)+"px; width: 0px; height: 0px; z-index: 2; border-top: 5px solid transparent; border-right: 6px solid white; border-bottom: 5px solid transparent;'></div>"
    						     + "<div style='width: "+size_cover+"px; height: "+size_cover+"px; margin-left: auto; margin-right: auto'><img width='"+size_cover+"' height='"+size_cover+"' src='"+fomjar.net.api('?inst=2109&string='+message.member_info.name)+"' /></div>"
    						 + "</div>"
    						 + "<div style='text-align: center; font-size: 60%; color: gray; word-wrap: break-word; '>"+message.member_info.name+"</div>"
                         + "</td>"
                         + "<td width='"+width_message+"px' style='vertical-align: top;'><div class='weui_btn weui_btn_mini weui_btn_primary' style='max-width: "+width_message+"px; float: left; padding-top: "+padding_content+"px; padding-bottom: "+padding_content+"px; padding-left: 1em; padding-right: 1em; word-wrap: break-word; text-align: left; font-size: "+size_font+"px; line-height: "+(size_font*1.5)+"px; color: black; background: white;'>"+message.message+"</div></td>"
                         + "<td width='"+width_space+"px'></td>"
                     + "</tr></table>");
            return div;
        },
        message_right : function(message) {
            var width_total   = $(document.body).width();
            var width_space   = width_total / 5;
            var width_edge    = 80;
            var width_message = width_total - width_space - width_edge;
            var top_triangle  = 13;
            var size_cover    = 36;
            var padding_message = 8;
    		var padding_content = 10;
            var size_font     = 16;
    
            var div = $('<div></div>');
            div.css('width', width_total+'px');
            div.css('padding-top',    padding_message + 'px');
            div.css('padding-bottom', padding_message + 'px');
            div.append("<table style='border-collapse: collapse;'><tr>"
                         + "<td width='"+width_space+"px'></td>"
                         + "<td width='"+width_message+"px' style='vertical-align: top;'><div class='weui_btn weui_btn_mini weui_btn_primary' style='max-width: "+width_message+"px; float: right; word-wrap: break-word; text-align: left; padding-top: "+padding_content+"px; padding-bottom: "+padding_content+"px; padding-left: 1em; padding-right: 1em; font-size: "+size_font+"px; line-height: "+(size_font*1.5)+"px; color: black; background: #a0e75a'>"+message.message+"</div></td>"
                         + "<td width='"+width_edge+"px' style='vertical-align: top;'>"
                             + "<div>"
                                 + "<div style='position: relative; float: left; left: 0px; top: "+(size_cover/2)+"px; width: 0px; height: 0px; z-index: 2; border-top: 5px solid transparent; border-left: 6px solid #80b948; border-bottom: 5px solid transparent;'></div>"
                                 + "<div style='position: relative; float: left; left: -7px; top: "+(size_cover/2)+"px; width: 0px; height: 0px; z-index: 2; border-top: 5px solid transparent; border-left: 6px solid #a0e75a; border-bottom: 5px solid transparent;'></div>"
    						     + "<div style='width: "+size_cover+"px; height: "+size_cover+"px; margin-left: auto; margin-right: auto'><img width='"+size_cover+"' height='"+size_cover+"' src='"+fomjar.net.api('?inst=2109&string='+message.member_info.name)+"' /></div>"
    						 + "</div>"
    						 + "<div style='text-align: center; font-size: 60%; color: gray; word-wrap: break-word; '>"+message.member_info.name+"</div>"
                         + "</td>"
                     + "</tr></table>");
            return div;
        },

        cell_game : function(game) {
            return $("<a class='weui_media_box weui_media_appmsg' href=\"javascript: ski.goto.game(" + game.gid + ");\">"
                       + "<div class='weui_media_hd'><img class='weui_media_appmsg_thumb' src='" + game.url_icon + "'></div>"
                       + "<div class='weui_media_bd'>"
                          + "<h4 class='weui_media_title'>" + game.name_zh_cn + "</h4>"
                          + "<p class='weui_media_desc'>" + game.introduction + "</p>"
                       + "</div>"          
                       + "<div style='position: absolute; width: 70px; top: 20px; right: 20px'><table width='100%'><tr>"
                           + (game.rent_avail_a ? "<td style='white-space: nowrap;'><div style='text-align : center; font-size : 10px; color : white; background : #00C802; -webkit-border-radius : 6px; -moz-border-radius : 6px;'>认证</div></td>"
                                                : "<td style='white-space: nowrap;'><div style='text-align : center; font-size : 10px; color : white; background : #B2B2B2; -webkit-border-radius : 6px; -moz-border-radius : 6px;'>认证</div></td>")
                           + (game.rent_avail_b ? "<td style='white-space: nowrap;'><div style='text-align : center; font-size : 10px; color : white; background : #00C802; -webkit-border-radius : 6px; -moz-border-radius : 6px;'>不认证</div></td>"
                                                : "<td style='white-space: nowrap;'><div style='text-align : center; font-size : 10px; color : white; background : #B2B2B2; -webkit-border-radius : 6px; -moz-border-radius : 6px;'>不认证</div></td>")
                       + "</tr></table></div>"
                   + "</a>");           
        }
    },

    wechat : {
        pay_prepare : function(callback) {
            fomjar.net.sendto(fomjar.net.api('/pay/recharge/prepare'), {inst : '2104'},                callback);
        },
        pay_apply : function(money, callback) {
            fomjar.net.sendto(fomjar.net.api('/pay/recharge/apply'),   {inst : '2104', money : money}, callback);
        },
        pay_refund : function(callback) {
            fomjar.net.sendto(fomjar.net.api('/pay/refund'),           {inst : '2104'},                callback);
        }
    },

    goto : {
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
        },
        chatroom : function(gid) {
            if ('number' == typeof(gid)) gid = gid.toString(16);
            window.location = ski.url.doc + '/query_chatroom.html?gid=' + gid;
        }
    },

    common : {
        apply_rent_begin : function(gid, type, callback) {
            fomjar.net.send('2106', {gid : gid, type : type}, callback);
        },
        apply_rent_end : function(oid, csn, callback) {
            fomjar.net.send('2107', {oid : oid, csn : csn}, callback);
        },
        query_game : function(options, callback) {
            fomjar.net.send('2001', options, callback);
        },
        query_order : function(oid, csn, callback) {
            if (1 == arguments.length) {
                callback = oid;
                fomjar.net.send('2006', null, callback);
            } else fomjar.net.send('2006', {oid : oid, csn : csn}, callback);
        },
        query_platform_account : function(options, callback) {
            if (1 == arguments.length) {
                callback = options;
                options = null;
            }
            fomjar.net.send('2009', options, callback);
        },
        query_platform_account_map : function(callback) {
            fomjar.net.send('200a', null, callback);
        },
        update_platform_account_map : function(options, callback) {
            fomjar.net.send('240a', options, callback);
        },
        chatroom : {
            get : function(options, callback) {
                if (undefined != options.gid) {
                    fomjar.net.send('2011', {gid : options.gid}, callback);
                }
            },
            join : function(crid, callback) {
                fomjar.net.send('2412', {crid : crid}, callback);
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
                fomjar.net.send('2012', data, callback);
            },
            message : {
                get : function(crid, options, callback) {
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
                    fomjar.net.send('2013', data, callback);
                },
                send : function (message, callback) {
                    fomjar.net.send('2414', message, callback);
                },
                receiver : -1,
                receive_start : function(crid, interval, callback_condition, callback_message) {
                    ski.common.chatroom.message.receiver = window.setInterval(function() {
                        ski.common.chatroom.message.get(crid, callback_condition(), callback_message);
                    }, interval);
                    return ski.common.chatroom.message.receiver;
                },
                receive_stop : function(receiver) {
                    if (null == receiver) window.clearInterval(ski.common.chatroom.message.receiver);
                    else window.clearInterval(receiver);
                }
            }
        }
    }
};
});
