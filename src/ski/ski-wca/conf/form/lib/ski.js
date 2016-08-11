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

    user : function() {return this.cookie('user');}
};
