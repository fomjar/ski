var ski = {

    cookie : function(key, val)  {
        switch (arguments.length) {
        case 1:
            return $.cookie(key);
        case 2:
            var origin = $.cookie(key);
            $.cookie(key, val, {path: "/"});
            return origin;
        }
    },

    user : function() {return this.cookie('user');},

    ui : {
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

        createCover : function(width, height, url) {
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
        
        textByHeight : function(text, height) {
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
        }
    }
};
