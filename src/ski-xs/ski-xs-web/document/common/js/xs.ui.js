
xs.ui = {};

xs.ui.PageSet = function() {
    var ps = $('<div></div>');
    ps.addClass('page-set');
    var pages = [];

    ps.append_page = function(page) {
        pages.push(page);
    };

    return ps;
}

xs.ui.Page = function(options) {
    if (!options.name) throw 'field \'name\' must offered';
    if (!options.view) throw 'field \'view\' must offered';

    this.name   = options.name;
    this.view   = options.view;
    this.oper   = options.oper ? options.oper : null;

    this.view.addClass('page');

    return this;
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
        head.children().remove();

        head.cover = new xs.ui.Cover(null, '登陆');
        head.m = $('<div></div>');
        head.m.addClass('m');
        head.r = $('<div></div>');
        head.r.addClass('r');

        head.append([head.cover, head.m, head.r]);
    };
    
    xs.ui._head = head;
    return xs.ui._head;
}

