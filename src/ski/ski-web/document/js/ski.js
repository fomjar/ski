var ski = {}

// util
ski.util = {
    base64 : {
        // 转码表  
        table : [  
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',  
                'I', 'J', 'K', 'L', 'M', 'N', 'O' ,'P',  
                'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',  
                'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',  
                'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',  
                'o', 'p', 'q', 'r', 's', 't', 'u', 'v',  
                'w', 'x', 'y', 'z', '0', '1', '2', '3',  
                '4', '5', '6', '7', '8', '9', '+', '/' 
        ],  
        UTF16ToUTF8 : function(str) {  
            var res = [], len = str.length;  
            for (var i = 0; i < len; i++) {  
                var code = str.charCodeAt(i);  
                if (code > 0x0000 && code <= 0x007F) {  
                    // 单字节，这里并不考虑0x0000，因为它是空字节  
                    // U+00000000 – U+0000007F  0xxxxxxx  
                    res.push(str.charAt(i));  
                } else if (code >= 0x0080 && code <= 0x07FF) {  
                    // 双字节  
                    // U+00000080 – U+000007FF  110xxxxx 10xxxxxx  
                    // 110xxxxx  
                    var byte1 = 0xC0 | ((code >> 6) & 0x1F);  
                    // 10xxxxxx  
                    var byte2 = 0x80 | (code & 0x3F);  
                    res.push(  
                        String.fromCharCode(byte1),   
                        String.fromCharCode(byte2)  
                    );  
                } else if (code >= 0x0800 && code <= 0xFFFF) {  
                    // 三字节  
                    // U+00000800 – U+0000FFFF  1110xxxx 10xxxxxx 10xxxxxx  
                    // 1110xxxx  
                    var byte1 = 0xE0 | ((code >> 12) & 0x0F);  
                    // 10xxxxxx  
                    var byte2 = 0x80 | ((code >> 6) & 0x3F);  
                    // 10xxxxxx  
                    var byte3 = 0x80 | (code & 0x3F);  
                    res.push(  
                        String.fromCharCode(byte1),   
                        String.fromCharCode(byte2),   
                        String.fromCharCode(byte3)  
                    );  
                } else if (code >= 0x00010000 && code <= 0x001FFFFF) {  
                    // 四字节  
                    // U+00010000 – U+001FFFFF  11110xxx 10xxxxxx 10xxxxxx 10xxxxxx  
                } else if (code >= 0x00200000 && code <= 0x03FFFFFF) {  
                    // 五字节  
                    // U+00200000 – U+03FFFFFF  111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx  
                } else /** if (code >= 0x04000000 && code <= 0x7FFFFFFF)*/ {  
                    // 六字节  
                    // U+04000000 – U+7FFFFFFF  1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx  
                }  
            }  
     
            return res.join('');  
        },  
        UTF8ToUTF16 : function(str) {  
            var res = [], len = str.length;  
            var i = 0;  
            for (var i = 0; i < len; i++) {  
                var code = str.charCodeAt(i);  
                // 对第一个字节进行判断  
                if (((code >> 7) & 0xFF) == 0x0) {  
                    // 单字节  
                    // 0xxxxxxx  
                    res.push(str.charAt(i));  
                } else if (((code >> 5) & 0xFF) == 0x6) {  
                    // 双字节  
                    // 110xxxxx 10xxxxxx  
                    var code2 = str.charCodeAt(++i);  
                    var byte1 = (code & 0x1F) << 6;  
                    var byte2 = code2 & 0x3F;  
                    var utf16 = byte1 | byte2;  
                    res.push(String.fromCharCode(utf16));  
                } else if (((code >> 4) & 0xFF) == 0xE) {  
                    // 三字节  
                    // 1110xxxx 10xxxxxx 10xxxxxx  
                    var code2 = str.charCodeAt(++i);  
                    var code3 = str.charCodeAt(++i);  
                    var byte1 = (code << 4) | ((code2 >> 2) & 0x0F);  
                    var byte2 = ((code2 & 0x03) << 6) | (code3 & 0x3F);  
                    utf16 = ((byte1 & 0x00FF) << 8) | byte2  
                    res.push(String.fromCharCode(utf16));  
                } else if (((code >> 3) & 0xFF) == 0x1E) {  
                    // 四字节  
                    // 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx  
                } else if (((code >> 2) & 0xFF) == 0x3E) {  
                    // 五字节  
                    // 111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx  
                } else /** if (((code >> 1) & 0xFF) == 0x7E)*/ {  
                    // 六字节  
                    // 1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx  
                }  
            }  
     
            return res.join('');  
        },  
        encode : function(str) {  
            if (!str) {  
                return '';  
            }  
            var utf8    = this.UTF16ToUTF8(str); // 转成UTF8  
            var i = 0; // 遍历索引  
            var len = utf8.length;  
            var res = [];  
            while (i < len) {  
                var c1 = utf8.charCodeAt(i++) & 0xFF;  
                res.push(this.table[c1 >> 2]);  
                // 需要补2个=  
                if (i == len) {  
                    res.push(this.table[(c1 & 0x3) << 4]);  
                    res.push('==');  
                    break;  
                }  
                var c2 = utf8.charCodeAt(i++);  
                // 需要补1个=  
                if (i == len) {  
                    res.push(this.table[((c1 & 0x3) << 4) | ((c2 >> 4) & 0x0F)]);  
                    res.push(this.table[(c2 & 0x0F) << 2]);  
                    res.push('=');  
                    break;  
                }  
                var c3 = utf8.charCodeAt(i++);  
                res.push(this.table[((c1 & 0x3) << 4) | ((c2 >> 4) & 0x0F)]);  
                res.push(this.table[((c2 & 0x0F) << 2) | ((c3 & 0xC0) >> 6)]);  
                res.push(this.table[c3 & 0x3F]);  
            }  
     
            return res.join('');  
        },  
        decode : function(str) {  
            if (!str) {  
                return '';  
            }  
     
            var len = str.length;  
            var i   = 0;  
            var res = [];  
     
            while (i < len) {  
                code1 = this.table.indexOf(str.charAt(i++));  
                code2 = this.table.indexOf(str.charAt(i++));  
                code3 = this.table.indexOf(str.charAt(i++));  
                code4 = this.table.indexOf(str.charAt(i++));  
     
                c1 = (code1 << 2) | (code2 >> 4);  
                c2 = ((code2 & 0xF) << 4) | (code3 >> 2);  
                c3 = ((code3 & 0x3) << 6) | code4;  
     
                res.push(String.fromCharCode(c1));  
     
                if (code3 != 64) {  
                    res.push(String.fromCharCode(c2));  
                }  
                if (code4 != 64) {  
                    res.push(String.fromCharCode(c3));  
                }  
     
            }  
     
            return this.UTF8ToUTF16(res.join(''));  
        }  
    }
};

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
		var $body = $('body');
		document.title = title;
		// hack在微信等webview中无法修改document.title的情况
		var $iframe = $('<iframe src="/favicon.ico"></iframe>').on('load', function() {
			setTimeout(function() {
				$iframe.off('load').remove();
			}, 0)
		}).appendTo($body);
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
        div.append("<div style='width:100%; text-align:right'><a id='"+id_btn+"' href='javascript: void(0);' onclick=\""+fn+"\">"+l_open+"</a></div>");
        div.append("<div id='"+id_bak+"' style='width:100%; display:none; opacity: 0.0'>"+text+"</div>"); // append a copy to calculate real height
        return div;
    },

    message_left : function(message) {
        switch (message.type) {
        case 0: {// text
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
		    				     + "<div style='width: "+size_cover+"px; height: "+size_cover+"px; margin-left: auto; margin-right: auto'><img width='"+size_cover+"' height='"+size_cover+"' src='"+ski.url.api+"?inst=2109&string="+message.member_info.name+"' /></div>"
		    				 + "</div>"
		    				 + "<div style='text-align: center; font-size: 60%; color: gray; word-wrap: break-word; '>"+message.member_info.name+"</div>"
                         + "</td>"
                         + "<td width='"+width_message+"px' style='vertical-align: top;'><div class='weui_btn weui_btn_mini weui_btn_primary' style='max-width: "+width_message+"px; float: left; padding: "+padding_content+"px; word-wrap: break-word; text-align: left; font-size: "+size_font+"px; line-height: "+(size_font*1.5)+"px; color: black; background: white;'>"+message.message+"</div></td>"
                         + "<td width='"+width_space+"px'></td>"
                     + "</tr></table>");
            return div;
        }
        case 1: {
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
		    				     + "<div style='width: "+size_cover+"px; height: "+size_cover+"px; margin-left: auto; margin-right: auto'><img width='"+size_cover+"' height='"+size_cover+"' src='"+ski.url.api+"?inst=2109&string="+message.member_info.name+"' /></div>"
		    				 + "</div>"
		    				 + "<div style='text-align: center; font-size: 60%; color: gray; word-wrap: break-word; '>"+message.member_info.name+"</div>"
                         + "</td>"
                         + "<td style='max-width:"+width_message+"px' style='vertical-align: top;'><div class='weui_btn weui_btn_mini weui_btn_primary' style='max-width: "+width_message+"px; float: left; padding: "+padding_content+"px; word-wrap: break-word; text-align: left; background: white;'>"
                             + "<img width='"+(width_message-padding_content*2)+"px' src='"+ski.url.api+"?inst=2013&crid="+message.crid.toString(16)+"&mid="+message.mid.toString(16)+"&type="+message.type.toString(16)+"' />"
                         + "</div></td>"
                         + "<td width='"+width_space+"px'></td>"
                     + "</tr></table>");
            return div;
        }
        }
    },
    message_right : function(message) {
        switch (message.type) {
        case 0: { // text
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
                         + "<td width='"+width_message+"px' style='vertical-align: top;'><div class='weui_btn weui_btn_mini weui_btn_primary' style='max-width: "+width_message+"px; float: right; word-wrap: break-word; text-align: left; padding: "+padding_content+"px; font-size: "+size_font+"px; line-height: "+(size_font*1.5)+"px; color: black; background: #a0e75a'>"+message.message+"</div></td>"
                         + "<td width='"+width_edge+"px' style='vertical-align: top;'>"
                             + "<div>"
                                 + "<div style='position: relative; float: left; left: 0px; top: "+(size_cover/2)+"px; width: 0px; height: 0px; z-index: 2; border-top: 5px solid transparent; border-left: 6px solid #80b948; border-bottom: 5px solid transparent;'></div>"
                                 + "<div style='position: relative; float: left; left: -7px; top: "+(size_cover/2)+"px; width: 0px; height: 0px; z-index: 2; border-top: 5px solid transparent; border-left: 6px solid #a0e75a; border-bottom: 5px solid transparent;'></div>"
		    				     + "<div style='width: "+size_cover+"px; height: "+size_cover+"px; margin-left: auto; margin-right: auto'><img width='"+size_cover+"' height='"+size_cover+"' src='"+ski.url.api+"?inst=2109&string="+message.member_info.name+"' /></div>"
		    				 + "</div>"
		    				 + "<div style='text-align: center; font-size: 60%; color: gray; word-wrap: break-word; '>"+message.member_info.name+"</div>"
                         + "</td>"
                     + "</tr></table>");
            return div;
        }
        case 1: { // image
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
                         + "<td width='"+width_message+"px' style='vertical-align: top;'><div class='weui_btn weui_btn_mini weui_btn_primary' style='max-width: "+width_message+"px; float: right; word-wrap: break-word; text-align: left; padding: "+padding_content+"px; background: #a0e75a'>"
                             + "<img style='max-width:"+(width_message-padding_content*2)+"px' src='"+ski.url.api+"?inst=2013&crid="+message.crid.toString(16)+"&mid="+message.mid.toString(16)+"&type="+message.type.toString(16)+"' />"
                         + "</div></td>"
                         + "<td width='"+width_edge+"px' style='vertical-align: top;'>"
                             + "<div>"
                                 + "<div style='position: relative; float: left; left: 0px; top: "+(size_cover/2)+"px; width: 0px; height: 0px; z-index: 2; border-top: 5px solid transparent; border-left: 6px solid #80b948; border-bottom: 5px solid transparent;'></div>"
                                 + "<div style='position: relative; float: left; left: -7px; top: "+(size_cover/2)+"px; width: 0px; height: 0px; z-index: 2; border-top: 5px solid transparent; border-left: 6px solid #a0e75a; border-bottom: 5px solid transparent;'></div>"
		    				     + "<div style='width: "+size_cover+"px; height: "+size_cover+"px; margin-left: auto; margin-right: auto'><img width='"+size_cover+"' height='"+size_cover+"' src='"+ski.url.api+"?inst=2109&string="+message.member_info.name+"' /></div>"
		    				 + "</div>"
		    				 + "<div style='text-align: center; font-size: 60%; color: gray; word-wrap: break-word; '>"+message.member_info.name+"</div>"
                         + "</td>"
                     + "</tr></table>");
            return div;
        }
        }
    },
    message_system : function(message) {
        var today = new Date().format('yyyy-MM-dd');
        if (message.message.startsWith(today)) message.message = message.message.substring(11).substring(0, 5);

        var margin_message = 8;
        var div = $('<div></div>');
        div.css('width', '100%');
        div.css('padding-top',    margin_message + 'px');
        div.css('padding-bottom', margin_message + 'px');
        div.append("<table style='border-collapse: collapse; margin-left: auto; margin-right: auto; '><tr><td><div style='padding: 6px; -webkit-border-radius : 6px; -moz-border-radius : 6px; font-size: 11px; line-height: 8px; color: white; background: #cccccc;'>"+message.message+"</div></td></tr></table>");
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
    },
    chatroom : function(gid) {
        if ('number' == typeof(gid)) gid = gid.toString(16);
        window.location = ski.url.doc + '/query_chatroom.html?gid=' + gid;
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
            ski.send('2013', data, callback);
        },
        send : function (message, callback) {
            ski.send('2414', message, callback);
        },
        receiver : -1,
        receive_start : function(crid, interval, callback_condition, callback_message) {
            ski.chatroom.message.receiver = window.setInterval(function() {
                ski.chatroom.message.get(crid, callback_condition(), callback_message);
            }, interval);
        },
        receive_stop : function() {
            window.clearInterval(ski.chatroom.message.receiver);
        }
    }
};

// prototype
Date.prototype.format = function (fmt) { //author: meizz 
    var o = {
        "M+" : this.getMonth() + 1,  //月份 
        "d+" : this.getDate(),       //日 
        "H+" : this.getHours(),      //小时 
        "m+" : this.getMinutes(),    //分 
        "s+" : this.getSeconds(),    //秒 
        "q+" : Math.floor((this.getMonth() + 3) / 3), //季度 
        "S"  : this.getMilliseconds() //毫秒 
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o) {
        if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    }
    return fmt;
};
