(function($) {
fomjar.framework.phase.append('ini', function() {

xs.ui = {};
xs.ui.DELAY = 500;

xs.ui.PageStack = function() {
    var stack = $('<div></div>');
    stack.addClass('page-stack');
    stack.pages = [];

    stack.page_push = function(page) {
        if (stack.pages.length == 0) {
            page.to_mid();
            stack.append(page);
        } else {
            page.to_up();
            stack.append(page);
            var down = stack.pages[stack.pages.length - 1];

            fomjar.util.async(function() {
                page.to_mid();
                down.to_down();
            });
        }

        stack.pages.push(page);
        if (stack.page_switch_cb) stack.page_switch_cb('push', stack.pages.length >= 2 ? stack.pages[stack.pages.length - 2] : null, page);
    };
    stack.page_pop = function() {
        if (stack.pages.length <= 1) return;

        var page_down = stack.pages[stack.pages.length - 2];
        var page_up = stack.pages[stack.pages.length - 1];
        fomjar.util.async(function() {
            page_down.to_mid();
            page_up.to_up();
        });
        fomjar.util.async(function() {
            page_up.detach();
        }, xs.ui.DELAY);

        stack.pages.pop();
        if (stack.page_switch_cb) stack.page_switch_cb('pop', page_up, page_down);

        return page_up;
    };
    stack.page_cur = function() {
        if (stack.pages.length == 0) return null;
        return stack.pages[stack.pages.length - 1];
    };

    stack.page_switch_cb = null;
    stack.page_set_switch_cb = function(cb) {
        stack.page_switch_cb = cb;
    };
    stack.PAGE_SWITCH_CB_DEFAULT = function(operation, page_old, page_new) {
        xs.ui.head().l.addClass('head-disappear');
        xs.ui.head().m.addClass('head-disappear');
        xs.ui.head().r.addClass('head-disappear');

        fomjar.util.async(function() {
            xs.ui.head().l.children().detach();
            xs.ui.head().m.children().detach();
            xs.ui.head().r.children().detach();

            if (page_new.op_l) {
                xs.ui.head().l.append(page_new.op_l);
                xs.ui.head().l.removeClass('head-disappear');
            }
            if (page_new.op_m) {
                xs.ui.head().m.append(page_new.op_m);
                xs.ui.head().m.removeClass('head-disappear');
            }
            if (page_new.op_r) {
                xs.ui.head().r.append(page_new.op_r);
                xs.ui.head().r.removeClass('head-disappear');
            }
        }, xs.ui.DELAY / 2);
    };

    return stack;
}

xs.ui.Page = function(options) {
    var div = $('<div></div>');
    div.addClass('page');

    if (options.name) div.name = options.name;
    if (options.op_l) div.op_l = options.op_l;
    if (options.op_m) div.op_m = options.op_m;
    if (options.op_r) div.op_r = options.op_r;

    div.to_mid = function() {
        div.removeClass('page-down page-up');
    };
    div.to_down = function() {
        div.removeClass('page-down page-up');
        div.addClass('page-down');
    };
    div.to_up = function() {
        div.removeClass('page-down page-up');
        div.addClass('page-up');
    };

    return div;
}

xs.ui.Mask = function() {
    var mask = $('<div></div>');
    mask.addClass('mask');

    mask.appear = function() {
        mask.addClass('disappear');
        $('.xs').append(mask);
        fomjar.util.async(function() {mask.removeClass('disappear');});
    };
    mask.disappear = function() {
        mask.addClass('disappear');
        fomjar.util.async(function() {mask.detach();}, xs.ui.DELAY);
    };

    mask.bind('click', function(e) {e.preventDefault();});

    return mask;
}

xs.ui.Dialog = function() {
    var dialog = $('<div></div>');
    dialog.addClass('dialog center');

    dialog.appear = function() {
        dialog.addClass('dialog-disappear');
        $('.xs').append(dialog);
        fomjar.util.async(function() {dialog.removeClass('dialog-disappear');});
    };
    dialog.disappear = function() {
        dialog.addClass('dialog-disappear');
        fomjar.util.async(function() {dialog.detach();}, xs.ui.DELAY);
    };
    dialog.shake = function() {
        dialog.addClass('dialog-shake');
        fomjar.util.async(function() {dialog.removeClass('dialog-shake');}, xs.ui.DELAY);
    };
    dialog.append_space = function(height) {
        if (!height) height = '1em';
        var space = $('<div></div>');
        space.css('height', height);
        dialog.append(space);
        return space;
    };
    dialog.append_text_h1 = function(text) {
        var div = $('<div></div>');
        div.css('padding',      '.5em');
        div.css('font-weight',  '700');
        div.append(text);
        dialog.append(div);
        return div;
    };
    dialog.append_text_h1c = function(text) {
        var div = $('<div></div>');
        div.css('padding',      '.5em');
        div.css('font-weight',  '700');
        div.css('text-align',   'center');
        div.append(text);
        dialog.append(div);
        return div;
    };
    dialog.append_text_p1 = function(text) {
        var div = $('<div></div>');
        div.css('padding',  '.5em');
        div.append(text);
        dialog.append(div);
        return div;
    };
    dialog.append_text_p1c = function(text) {
        var div = $('<div></div>');
        div.css('padding',      '.5em');
        div.css('text-align',   'center');
        div.append(text);
        dialog.append(div);
        return div;
    };
    dialog.append_input = function(attr) {
        var div = $('<input>');
        div.css('width',    '100%');
        if (attr) {
            $.each(attr, function(k, v) {
                div.attr(k, v);
            });
        }
        dialog.append(div);
        return div;
    };
    dialog.append_button = function(button) {
        var div = $('<div></div>');
        div.append(button);

        div.css('padding', '.5em');
        button.css('width',         '100%');
        button.css('text-align',    'center');

        dialog.append(div);
        return button;
    }
    dialog.style_popupmenu = function(options) {
        dialog.children().detach();

        var has_head = false;
        if (options.icon) {
            dialog.append_text_p1c(options.icon)
            has_head = true;
        }
        if (options.title) {
            dialog.append_text_h1c(options.title);
            has_head = true;
        }
        if (options.subtitle) {
            dialog.append_text_p1c(options.subtitle);
            has_head = true;
        }

        if (has_head) {
            dialog.append_space('1em');
        }

        if (options.content) {
            dialog.append(options.content);
        }

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

xs.ui.List = function() {
    var list = $('<div></div>');
    list.addClass('list');


    list.append_cell = function(options) {
        var cell = $('<div></div>');

        if (options.icon) {
            var img = $('<img>');
            img.addClass('icon');
            img.attr('src', options.icon);

            var width = null;
            if (options.major && options.minor) width = '2.6em';
            else if (options.major) width = '1.3em';
            else width = '1em';
            img.css('width', width);

            cell.append(img);
        }
        if (options.major)  {
            var div = $('<div></div>');
            div.addClass('major');
            div.text(options.major);
            cell.append(div);
        }
        if (options.minor)  {
            var div = $('<div></div>');
            div.addClass('minor');
            div.text(options.minor);
            cell.append(div);
        }
        if (options.accessory)  {
            var div = new xs.ui.shape.ArrowRight('1px', 'lightgray');
            div.addClass('accessory');
            cell.append(div);
        }
        if (options.align)  cell.css('text-align',  options.align);
        if (options.action) cell.bind('click',      options.action);

        list.append(cell);
        return cell;
    };

    return list;
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
        spinner.spin(sc[0]);
    };
    sc.disappear = function() {
        sc.addClass('disappear');
        fomjar.util.async(function() {
            spinner.spin();
            sc.detach();
        }, xs.ui.DELAY);
    };
    sc.center = function() {
        sc.addClass('center');
    };

    return sc;
}

xs.ui.Button = function(content, action) {
    var button = $('<div></div>');
    button.addClass('button');
    button.to_normal = function() {
        button.removeClass('button-high');
        button.removeClass('button-dark');
    };
    button.to_high = function() {
        button.removeClass('button-dark');
        button.addClass('button-high');
    };
    button.to_dark = function() {
        button.removeClass('button-high');
        button.addClass('button-dark');
    };
    if (content) button.append(content);
    if (action) button.bind('click', action);
    return button;
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

xs.ui.hud = {};
xs.ui.hud.Major = function() {
};
xs.ui.hud.Minor = function(content) {
    var div = $('<div></div>');
    div.addClass('hud-minor');

    div.appear = function(timeout) {
        if (!timeout) timeout = 2e9;

        div.addClass('disappear');
        $('.xs').append(div);
        fomjar.util.async(function() {div.removeClass('disappear');});
        fomjar.util.async(function() {
            div.disappear();
        }, timeout)
    };
    div.disappear = function() {
        div.addClass('disappear');
        fomjar.util.async(function() {div.detach();}, xs.ui.DELAY);
    };

    if (content) div.append(content);

    return div;
};

xs.ui.shape = {};
xs.ui.shape.ArrowRight = function(line, color, width, height) {
    var div = $('<div></div>');
    div.addClass('shape');

    var div_a = $('<div></div>');
    div_a.addClass('arrow-r');
    div_a.css('border-top',   line + ' solid ' + color);
    div_a.css('border-right', line + ' solid ' + color);

    div.append(div_a);

    if (width)  div.css('width',  width);
    if (height) div.css('height', height);
    return div;
};
xs.ui.shape.Plus = function(line, color, width, height) {
    var div = $('<div></div>');
    div.addClass('shape');

    var div_h = $('<div></div>');
    div_h.addClass('plus-h');
    div_h.css('height', line);
    div_h.css('background', color);

    var div_v = $('<div></div>');
    div_v.addClass('plus-v');
    div_v.css('width', line);
    div_v.css('background', color);

    div.append([div_h, div_v]);

    if (width)  div.css('width',  width);
    if (height) div.css('height', height);
    return div;
};

// 以下业务相关
/**
 * article : {
 *     aid,
 *     uid,
 *     ucover,
 *     uname,
 *     ugender,
 *     create,
 *     modify,
 *     location,
 *     weather,
 *     title,
 *     status,
 *     paragraph : [
 *         {
 *             pid,
 *             psn,
 *             element: [
 *                 esn,
 *                 et,
 *                 ec
 *             ]
 *         }
 *     ]
 * }
 */
xs.ui.ArticlePrepare = function(article) {
    article.paragraph = article.paragraph.sort(function(p1, p2) {return p1.psn - p2.psn;});
    $.each(article.paragraph, function(i, p) {
        p.element = p.element.sort(function(e1, e2) {return e1.esn - e2.esn;});
    });
};

xs.ui.ArticleEditor = function(article) {
    var ae = $('<div></div>');
    ae.addClass('ae');
    if (!article) article = {};

    ae.article = article;

    ae.append_head = function(article) {
        var div = $('<div></div>');
        div.addClass('ah');

        var input = $("<input placeholder='写下文章标题'>");

        div.append([input]);

        ae.append(div);
    };

    ae.append_paragraph_new = function() {
        var div = $('<div></div>');
        div.addClass('apn');

        var plus = new xs.ui.shape.Plus('4px', 'gray');
        plus.addClass('center');

        div.append(plus);

        ae.append(div);
    };
    ae.append_paragraph = function(paragraph) {
    };

    ae.append_head(ae.article);

    if (!ae.article.paragraph || ae.article.paragraph.length == 0) ae.append_paragraph_new();
    else $.each(ae.article.paragraph, function(i, p) {ae.append_paragraph(p);});

    return ae;
}

FastClick.attach(document.body);

});
})(jQuery);
