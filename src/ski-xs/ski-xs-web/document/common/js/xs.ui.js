
xs.ui = {};

xs.ui.PageSet = function() {
    var ps = $('<div></div>');
    ps.addClass('page-set');
    var pages = [];
    var cpage = null;

    var index_of_name = function(name) {
        var i = 0;
        $.each(pages, function(key, val) {
            if (key == name) {
                return false;
            }
            i++;
        });
        return i;
    };

    ps.page_append = function(page) {
        pages[page.name] = page;

        if (!cpage) ps.page_switch(page.name);
    };

    ps.page_switch = function(name) {
        if (cpage) {
            var i_o = index_of_name(cpage.name);
            var i_n = index_of_name(name);
            if (i_o <= i_n) {
                pages[name].or();
                ps.append(pages[name].view);

                cpage.ol(true);
                pages[name].in();
            } else {
                pages[name].ol();
                ps.append(pages[name].view);

                cpage.or(true);
                pages[name].in();
            }
        } else {
            pages[name].or();
            ps.append(pages[name].view);

            pages[name].in();
        }

        if (ps.page_switch_cb) ps.page_switch_cb(cpage, pages[name]);
        cpage = pages[name];
    };

    ps.page_switch_cb = null;
    ps.page_set_switch_cb = function(cb) {
        ps.page_switch_cb = cb;
    };
    ps.PAGE_SWITCH_CB_DEFAULT = function(page_old, page_new) {
        xs.ui.head().l.addClass('disappear');
        xs.ui.head().m.addClass('disappear');
        xs.ui.head().r.addClass('disappear');

        fomjar.util.async(function() {
            xs.ui.head().l.children().detach();
            xs.ui.head().m.children().detach();
            xs.ui.head().r.children().detach();

            if (page_new.op_l) {
                xs.ui.head().l.append(page_new.op_l);
                xs.ui.head().l.removeClass('disappear');
            }
            if (page_new.op_m) {
                xs.ui.head().m.append(page_new.op_m);
                xs.ui.head().m.removeClass('disappear');
            }
            if (page_new.op_r) {
                xs.ui.head().r.append(page_new.op_r);
                xs.ui.head().r.removeClass('disappear');
            }
        }, 250);
    };
    ps.page_set_switch_cb(ps.PAGE_SWITCH_CB_DEFAULT);

    return ps;
}

xs.ui.Page = function(options) {
    if (!options.name) throw 'field \'name\' must be offered';
    if (!options.view) throw 'field \'view\' must be offered';

    this.name   = options.name;
    this.view   = options.view;
    this.op_l   = options.op_l ? options.op_l : null;
    this.op_m   = options.op_m ? options.op_m : null;
    this.op_r   = options.op_r ? options.op_r : null;

    this.view.addClass('page');

    this.in = function() {
        var view = this.view;
        fomjar.util.async(function() {view.removeClass('page-l page-r');}, 0);
    };
    this.ol = function(detach) {
        var view = this.view;
        fomjar.util.async(function() {
            view.removeClass('page-l page-r');
            view.addClass('page-l');
            if (detach) fomjar.util.async(function() {view.detach();}, 500);
        }, 0);
    };
    this.or = function(detach) {
        var view = this.view;
        fomjar.util.async(function() {
            view.removeClass('page-l page-r');
            view.addClass('page-r');
            if (detach) fomjar.util.async(function() {view.detach();}, 500);
        }, 0);
    };

    return this;
}

xs.ui.Mask = function() {
    var mask = $('<div></div>');
    mask.addClass('mask disappear');

    mask.appear = function() {
        $('.xs').append(mask);
        fomjar.util.async(function() {mask.removeClass('disappear');});
    };
    mask.disappear = function() {
        mask.addClass('disappear');
        fomjar.util.async(function() {mask.detach();}, 500);
    };

    mask.bind('click', function(e) {e.preventDefault();});

    return mask;
}

xs.ui.Dialog = function() {
    var dialog = $('<div></div>');
    dialog.addClass('dialog center dialog-disappear');

    dialog.appear = function() {
        $('.xs').append(dialog);
        fomjar.util.async(function() {dialog.removeClass('dialog-disappear');});
    };
    dialog.disappear = function() {
        dialog.addClass('dialog-disappear');
        fomjar.util.async(function() {dialog.detach();}, 500);
    };
    dialog.size = function(width, height) {
        if (width)  dialog.css('width',     width);
        if (height) dialog.css('height',    height);
    };

    return dialog;
}

xs.ui.Cover = function(src, txt) {
    var cover = $('<div></div>');
    cover.addClass('cover');
    cover.img = $('<img />');
    cover.div = $('<div></div>');
    cover.append([cover.img, cover.div]);

    if (src) cover.img.attr('src', src);
    if (txt) cover.div.text(txt);

    return cover;
}

xs.ui.Spin = function(scale) {
    if (!scale) scale = 1.0;
    var opts = {
        lines       : 12            // The number of lines to draw
        , length    : 6             // The length of each line
        , width     : 3             // The line thickness
        , radius    : 8             // The radius of the inner circle
        , scale     : scale         // Scales overall size of the spinner
        , corners   : 1             // Corner roundness (0..1)
        , color     : '#999'        // #rgb or #rrggbb or array of colors
        , opacity   : 0.25          // Opacity of the lines
        , rotate    : 0             // The rotation offset
        , direction : 1             // 1: clockwise, -1: counterclockwise
        , speed     : 0.8           // Rounds per second
        , trail     : 40            // Afterglow percentage
        , fps       : 20            // Frames per second when using setTimeout() as a fallback for CSS
        , zIndex    : 2e9           // The z-index (defaults to 2000000000)
        , className : 'spinner'     // The CSS class to assign to the spinner
        , top       : '50%'         // Top position relative to parent
        , left      : '50%'         // Left position relative to parent
        , shadow    : false         // Whether to render a shadow
        , hwaccel   : false         // Whether to use hardware acceleration
        , position  : 'absolute'    // Element positioning
    };
    var spinner = new Spinner(opts);

    var size = scale * 35;
    var sc = $('<div></div>');
    sc.addClass('spinnerc disappear');
    sc.css('width', size);
    sc.css('height', size);
    sc.spinner = spinner;
    sc.appear = function() {
        sc.removeClass('disappear');
    };
    sc.disappear = function() {
        sc.addClass('disappear');
        fomjar.util.async(function() {
            spinner.spin();
            sc.remove();
        }, 500);
    };
    sc.center = function() {
        sc.addClass('center');
    };

    spinner.spin(sc[0]);
    return sc;
}

xs.ui.HeadButton = function(child, click) {
    var hb = $('<div></div>');
    hb.addClass('button');

    if (child) hb.append(child);
    if (click) hb.bind('click', click);

    return hb;
}

xs.ui.head = function() {
    if (xs.ui._head) return xs.ui._head;

    var head = $('.xs .head');

    head.reset = function() {
        head.children().detach();

        head.l = $('<div></div>');
        head.l.addClass('l');
        head.cover = new xs.ui.Cover();
        head.l.append(head.cover);

        head.m = $('<div></div>');
        head.m.addClass('m');

        head.r = $('<div></div>');
        head.r.addClass('r');

        head.append([head.l, head.m, head.r]);
    };
    
    xs.ui._head = head;
    return xs.ui._head;
}

xs.ui.body = function() {
    if (xs.ui._body) return xs.ui._body;

    var body = $('.xs .body');

    body.reset = function() {
        body.children().detach();
    };

    xs.ui._body = body;
    return xs.ui._body;
}

// 以下业务相关

xs.ui.CellArticle = function() {
    var cell = $('<div></div>');
}

FastClick.attach(document.body);

