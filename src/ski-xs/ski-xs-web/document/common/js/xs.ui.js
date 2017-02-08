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
            page.to_center();
            stack.append(page);
        } else {
            page.to_right();
            stack.append(page);
            var down = stack.pages[stack.pages.length - 1];

            fomjar.util.async(function() {
                page.to_center();
                down.to_left();
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
            page_down.to_center();
            page_up.to_right();
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
        switch (operation) {
        case 'push':
            if (page_old && page_old._head) {
                page_old._head.addClass('page-head-l');
            }
            if (page_new && page_new._head) {
                page_new._head.addClass('page-head-r');
                xs.ui.head().append(page_new._head);
                fomjar.util.async(function() {page_new._head.removeClass('page-head-r');});
            }
            break;
        case 'pop':
            if (page_old && page_old._head) {
                page_old._head.addClass('page-head-r');
                fomjar.util.async(function() {page_old._head.detach();}, xs.ui.DELAY);
            }
            if (page_new && page_new._head) {
                page_new._head.removeClass('page-head-l');
            }
            break;
        }
    };

    return stack;
}

xs.ui.Page = function() {
    var div = $('<div></div>');
    div.addClass('page');

    div.to_center = function() {
        div.removeClass('page-l page-r');
    };
    div.to_left = function() {
        div.removeClass('page-l page-r');
        div.addClass('page-l');
    };
    div.to_right = function() {
        div.removeClass('page-l page-r');
        div.addClass('page-r');
    };
    div.head = function(op_l, op_c, op_r) {
        if (!div._head) {
            var head = $('<div></div>');
            head.addClass('page-head');
            head.l = $('<div></div>');
            head.l.addClass('l');
            head.c = $('<div></div>');
            head.c.addClass('c center');
            head.r = $('<div></div>');
            head.r.addClass('r');
            div._head = head;
            head.append([head.l, head.c, head.r]);
        }

        if (op_l) {
            head.l.children().detach();
            head.l.append(op_l);
        }
        if (op_c) {
            head.c.children().detach();
            head.c.append(op_c);
        }
        if (op_r) {
            head.r.children().detach();
            head.r.append(op_r);
        }

        return div._head;
    };
    div.jump = function() {
        var jump = $('<div></div>');
        jump.addClass('page-jump page-jump-d');

        if (div._head) {
            var head = $('<div></div>');
            head.addClass('head');
            head.append(div._head);

            var body = $('<div></div>');
            body.addClass('body');
            body.append(div);

            jump.append([head, body]);
        } else {
            jump.append(div);
        }

        jump.drop = function() {
            jump.addClass('page-jump-d');
            fomjar.util.async(function() {jump.detach();}, xs.ui.DELAY);
        };

        $('.xs').append(jump);
        fomjar.util.async(function() {jump.removeClass('page-jump-d');});

        return jump;
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
        var div = $('<div></div>');
        div.css('padding',  '.2em .5em');
        var input = $('<input>');
        input.css('width',  '100%');
        if (attr) {
            $.each(attr, function(k, v) {
                input.attr(k, v);
            });
        }
        div.append(input);
        dialog.append(div);
        return input;
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
        , color     : 'gray'        // #rgb or #rrggbb or array of colors
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
        , shadow    : true          // Whether to render a shadow
        , hwaccel   : false         // Whether to use hardware acceleration
        , position  : 'absolute'    // Element positioning
    };
    var spinner = new Spinner(opts);

    var size = scale * 35;
    var div = $('<div></div>');
    div.css('display',   'inline-block');
    div.css('width',     size);
    div.css('height',    size);
    div.spinner = spinner;
    div.spinner.spin(div[0]);
    return div;
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
        head.cover = new xs.ui.Cover();
        head.cover.div.text('登陆');
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
xs.ui.hud.Major = function(content) {
    var div = $('<div></div>');
    div.addClass('hud-major center');

    div.style_loading = function(text) {
        var spin = new xs.ui.Spin(1.5);
        spin.css('margin',  '.5em');
        div.append(spin);
        if (text) {
            var div_txt = $('<div></div>');
            div_txt.css('margin',           '.5em 1em');
            div_txt.css('margin-bottom',    '0');
            div_txt.text(text);
            div.append(div_txt);
        }
    };
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
    div_a.css('width',  '70.72%');
    div_a.css('height', '70.72%');
    div_a.css('left',   '29.28%');
    div_a.css(        'transform',  'translate(-50%, -50%) rotate(45deg)');
    div_a.css('-webkit-transform',  'translate(-50%, -50%) rotate(45deg)');
    div_a.css('border-top',   line + ' solid ' + color);
    div_a.css('border-right', line + ' solid ' + color);

    div.append(div_a);

    if (width)  div.css('width',  width);
    if (height) div.css('height', height);
    return div;
};
xs.ui.shape.ArrowLeft = function(line, color, width, height) {
    var div = new xs.ui.shape.ArrowRight(line, color, width, height);
    div.find('>div').css('left',    '70.72%');
    div.find('>div').css(        'transform',  'translate(-50%, -50%) rotate(-135deg)');
    div.find('>div').css('-webkit-transform',  'translate(-50%, -50%) rotate(-135deg)');
    return div;
};
xs.ui.shape.ArrowUp = function(line, color, width, height) {
    var div = new xs.ui.shape.ArrowRight(line, color, width, height);
    div.find('>div').css('left',    '');
    div.find('>div').css('top',     '70.72%');
    div.find('>div').css(        'transform',  'translate(-50%, -50%) rotate(-45deg)');
    div.find('>div').css('-webkit-transform',  'translate(-50%, -50%) rotate(-45deg)');
    return div;
};
xs.ui.shape.ArrowDown = function(line, color, width, height) {
    var div = new xs.ui.shape.ArrowRight(line, color, width, height);
    div.find('>div').css('left',    '');
    div.find('>div').css('top',     '29.28%');
    div.find('>div').css(        'transform',  'translate(-50%, -50%) rotate(135deg)');
    div.find('>div').css('-webkit-transform',  'translate(-50%, -50%) rotate(135deg)');
    return div;
};
xs.ui.shape.Plus = function(line, color, width, height) {
    var div = $('<div></div>');
    div.addClass('shape');

    var div_h = $('<div></div>');
    div_h.css('width',  '100%');
    div_h.css('height', line);
    div_h.css('background', color);

    var div_v = $('<div></div>');
    div_v.css('width',  line);
    div_v.css('height', '100%');
    div_v.css('background', color);

    div.append([div_h, div_v]);

    if (width)  div.css('width',  width);
    if (height) div.css('height', height);
    return div;
};
xs.ui.shape.Option = function(point, color, width, height) {
    var div = $('<div></div>');
    div.addClass('shape');

    for (var i = 0; i < 3; i++) {
        var p = $('<div></div>');
        p.css('width',      point);
        p.css('height',     point);
        p.css('background', color);
        p.css(        'border-radius', point);
        p.css('-webkit-border-radius', point);
        div.append(p);
    }
    div.find('>div:nth-child(1)').css('left',   '25%');
    div.find('>div:nth-child(3)').css('left',   '75%');

    if (width)  div.css('width',  width);
    if (height) div.css('height', height);
    return div;
};
xs.ui.shape.X = function(line, color, width, height) {
    var plus = new xs.ui.shape.Plus(line, color, width, height);
    plus.css(        'transform',  'scale(1.39) translate(-36%, -36%) rotate(45deg)');
    plus.css('-webkit-transform',  'scale(1.39) translate(-36%, -36%) rotate(45deg)');
    return plus;
};
xs.ui.shape.Drag = function(line, color, width, height) {
    var div = $('<div></div>');
    div.addClass('shape');

    for (var i = 0; i < 3; i++) {
        var l = $('<div></div>');
        l.css('width',      '100%');
        l.css('height',     line);
        l.css('background', color);
        div.append(l);
    }

    div.find('>div:nth-child(1)').css('top',    '25%');
    div.find('>div:nth-child(3)').css('top',    '75%');

    if (width)  div.css('width',  width);
    if (height) div.css('height', height);
    return div;
};

xs.ui.preview = function(src) {
    var div = $('<div></div>');
    div.addClass('preview disappear');
    var img = $('<img>');
    img.attr('src',     src);
    div.append(img);

    div.disappear = function() {
        mask.disappear();
        div.addClass('disappear');
        fomjar.util.async(function() {div.detach();}, xs.ui.DELAY);
    };
    var mask = new xs.ui.Mask();
    mask.bind('click', div.disappear);
    div.bind('click', div.disappear);

    mask.appear();
    $('.xs').append(div);
    fomjar.util.async(function() {div.removeClass('disappear');});

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
        if (article.title) input.val(article.title);

        div.append([input]);

        ae.append(div);
    };

    ae.append_paragraph_new = function() {
        var div = new xs.ui.Button();
        div.addClass('apn');

        var plus = new xs.ui.shape.Plus('4px', 'lightgray');
        plus.addClass('center');

        var type_img = $('<div></div>');
        type_img.addClass('type type-img disappear');
        type_img.text('图');
        var input = $("<input type='file' accept='image/*' multiple=true >");
        type_img.append(input);
        input.bind('click', function() {
            clearTimeout(div.timer);
            div.to_normal();
        });
        input.bind('change', function(e) {
            div.to_normal();
            var delay = 0;
            $.each(e.target.files, function(i, f) {
                var src = window.URL.createObjectURL(f);
                var paragraph = {
                    element : [
                        {esn : 1, et : 1, ec : src},
                        {esn : 2, et : 0, ec : ''}
                    ]
                };
                fomjar.util.async(function() {
                    ae.append_paragraph(paragraph);
                }, delay);
                delay += xs.ui.DELAY / 2;
            });
        });

        var type_txt = $('<div></div>');
        type_txt.addClass('type type-txt disappear');
        type_txt.text('文');
        type_txt.bind('click', function() {
            clearTimeout(div.timer);
            div.to_normal();
            ae.append_paragraph();
        });

        var types = $([type_img[0], type_txt[0]]);
        types.hide();

        div.append([plus, type_img, type_txt]);
        div.is_normal = function() {
            return !plus.hasClass('disappear');
        };
        div.to_normal = function() {
            fomjar.util.async(function() {
                plus.removeClass('disappear');
                types.addClass('disappear');
            });
            fomjar.util.async(function() {types.hide();}, xs.ui.DELAY);
        };
        div.timer = null;
        div.to_choose = function() {
            plus.addClass('disappear');
            types.show();
            types.removeClass('disappear');
            div.timer = fomjar.util.async(function() {div.to_normal();}, 3000);
        };
        div.bind('click', function() {
            if (div.is_normal()) {
                div.to_choose();
            }
        });

        ae.append(div);
    };
    ae.append_paragraph = function(paragraph) {
        var div = $('<div></div>');
        div.addClass('ap fast disappear');

        var div_dele = new xs.ui.Button(new xs.ui.shape.X('1px', 'gray'), function() {
            div.addClass('disappear');
            fomjar.util.async(function() {div.detach();}, xs.ui.DELAY / 2);
        });
        div_dele.addClass('oper oper-1');
        div.append(div_dele);

        var div_move_up = new xs.ui.Button(new xs.ui.shape.ArrowUp('1px', 'gray'), function() {
            var i = div.index();
            if (1 == i) return;

            var before = $(ae.find('.ap')[i - 2]);
            div.addClass('disappear');
            before.addClass('disappear');
            fomjar.util.async(function() {
                div.detach();
                before.before(div);
                fomjar.util.async(function() {
                    div.removeClass('disappear');
                    before.removeClass('disappear');
                });
            }, xs.ui.DELAY / 2);
        });
        div_move_up.addClass('oper oper-2');
        div.append(div_move_up);

        // var div_move_down = new xs.ui.Button(new xs.ui.shape.ArrowDown('1px', 'gray'));
        // div_move_down.addClass('oper move-down');
        // div.append(div_move_down);

        div.addClass('ap-txt');
        if (paragraph && paragraph.element) {
            $.each(paragraph.element, function(i, e) {
                switch (e.et) {
                case 0: {
                    var txt = $('<textarea></textarea>');
                    txt.attr('placeholder', '输入文字内容');
                    txt.text(e.ec);
                    div.append(txt);
                    break;
                }
                case 1: {
                    div.removeClass('ap-txt');
                    div.addClass('ap-img');

                    var img = $('<img>');
                    img.addClass('center');
                    img.css('width',    '100%');
                    img.attr('src',     e.ec);
                    img.bind('click', function() {xs.ui.preview(e.ec);});

                    var div_img = $('<div></div>');
                    div_img.addClass('img');
                    div_img.append(img);
                    div.append(div_img);
                    break;
                }
                }
            });
        } else {
            var txt = $('<textarea></textarea>');
            txt.attr('placeholder', '输入段落内容');
            div.append(txt);
        }
        ae.find('.apn').before(div);
        fomjar.util.async(function() {div.removeClass('disappear');});
    };

    ae.generate_article = function() {
        var article = {};
        article.title = ae.find('.ah input').val();

        var paragraph = [];
        $.each(ae.find('.ap'), function(i, ap) {
            ap = $(ap);
            var element = [];
            if (0 < ap.find('img').length) {
                var e = {esn : 1, et : 1, ec : ap.find('img').attr('src')};
                element.push(e);
            }
            if (0 < ap.find('textarea').length) {
                var e = {esn : element.length + 1, et : 0, ec : ap.find('textarea').val()};
                element.push(e);
            }
            var p = {};
            p.psn = i;
            p.element = element;
            paragraph.push(p);
        });
        article.paragraph = paragraph;

        return article;
    };

    ae.append_head(ae.article);

    ae.append_paragraph_new();
    if (ae.article.paragraph && ae.article.paragraph.length > 0)
        $.each(ae.article.paragraph, function(i, p) {ae.append_paragraph(p);});

    return ae;
};

xs.ui.ArticleViewer = function(article) {
    var av = $('<div></div>');
    av.addClass('av');

    if (article.title) {
        var ah = $('<div></div>');
        ah.addClass('ah');
        ah.text(article.title);
        av.append(ah);
    }
    if (article.paragraph) {
        $.each(article.paragraph, function(i, p) {
            var ap = $('<div></div>');
            ap.addClass('ap ap-txt');

            $.each(p.element, function(i, e) {
                switch (e.et) {
                case 1: {
                    ap.removeClass('ap-txt');
                    ap.addClass('ap-img');

                    var img = $('<img>');
                    img.attr('src', e.ec);
                    ap.append(img);
                    break;
                }
                case 0: {
                    var txt = $('<div></div>');
                    txt.text(e.ec);
                    ap.append(txt);
                    break;
                }
                }
            });
            av.append(ap);
        });
    }
    return av;
};

FastClick.attach(document.body);

});
})(jQuery);
