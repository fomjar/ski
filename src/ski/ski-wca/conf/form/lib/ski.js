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
