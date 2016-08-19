var ski = {

    cookie : function(key, val)  {
        switch (arguments.length) {
        case 1:
            return $.cookie(key);
        case 2:
            var origin = $.cookie(key);
            $.cookie(key, val, { path: "/"});
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
        }
    }
};
