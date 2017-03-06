(function($) {
fomjar.framework.phase.append('ini', function() {

frs.ui = {};
frs.ui.DELAY = 500;

frs.ui.Mask = function() {
    var mask = $('<div></div>');
    mask.addClass('mask');

    mask.appear = function() {
        mask.addClass('disappear');
        $('.frs').append(mask);
        fomjar.util.async(function() {mask.removeClass('disappear');});
    };
    mask.disappear = function() {
        mask.addClass('disappear');
        fomjar.util.async(function() {mask.detach();}, frs.ui.DELAY);
    };

    mask.bind('click', function(e) {e.preventDefault();});

    return mask;
}

frs.ui.Dialog = function() {
    var dialog = $('<div></div>');
    dialog.addClass('dialog center');

    dialog.appear = function() {
        dialog.addClass('dialog-disappear');
        $('.frs').append(dialog);
        fomjar.util.async(function() {dialog.removeClass('dialog-disappear');});
    };
    dialog.disappear = function() {
        dialog.addClass('dialog-disappear');
        fomjar.util.async(function() {dialog.detach();}, frs.ui.DELAY);
    };
    dialog.shake = function() {
        dialog.addClass('dialog-shake');
        fomjar.util.async(function() {dialog.removeClass('dialog-shake');}, frs.ui.DELAY);
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
        div.css('padding',  '.3em .5em');
        var input = $('<input>');
        input.css('width',      '100%');
        input.css('text-align', 'center');
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

frs.ui.Cover = function(src, txt) {
    var cover = $('<div></div>');
    cover.addClass('cover');
    cover.img = $('<img />');
    cover.div = $('<div></div>');
    cover.append([cover.img, cover.div]);

    if (src) cover.img.attr('src', src);
    if (txt) cover.div.text(txt);

    return cover;
}

frs.ui.List = function() {
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
            var div = new frs.ui.shape.ArrowRight('1px', 'lightgray');
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

frs.ui.Button = function(content, action) {
    var button = $('<div></div>');
    button.addClass('button');
    button.to_default = function() {
        button.removeClass('button-major');
        button.removeClass('button-minor');
        button.removeClass('button-disable');
        return this;
    };
    button.to_major = function() {
        this.to_default();
        button.addClass('button-major');
        return this;
    };
    button.to_minor = function() {
        this.to_default();
        button.addClass('button-minor');
        return this;
    };
    button.to_disable = function() {
        this.to_default();
        button.addClass('button-disable');
        return this;
    };
    if (content) button.append(content);
    if (action) button.bind('click', action);
    return button;
}

frs.ui.head = function() {
    if (frs.ui._head) return frs.ui._head;

    var head = $('.frs .head');
    head.add_item = function(item, action) {
        var div = $('<div></div>');
        div.append(item);
        if (action) div.bind('click', action);
        head.append(div);
        return div;
    };
    
    frs.ui._head = head;
    return frs.ui._head;
}

frs.ui.body = function() {
    if (frs.ui._body) return frs.ui._body;

    var body = $('.frs .body');

    frs.ui._body = body;
    return frs.ui._body;
}

frs.ui.hud = {};
frs.ui.hud.Major = function(content) {
    var div = $('<div></div>');
    div.addClass('hud-major center');

    div.appear = function(timeout) {
        if (!timeout) timeout = 2e9;

        div.addClass('disappear');
        $('.frs').append(div);
        fomjar.util.async(function() {div.removeClass('disappear');});
        fomjar.util.async(function() {
            div.disappear();
        }, timeout)
    };
    div.disappear = function() {
        div.addClass('disappear');
        fomjar.util.async(function() {div.detach();}, frs.ui.DELAY);
    };

    if (content) div.append(content);

    return div;
};
frs.ui.hud.Minor = function(content) {
    var div = $('<div></div>');
    div.addClass('hud-minor');

    div.appear = function(timeout) {
        if (!timeout) timeout = 2e9;

        div.addClass('disappear');
        $('.frs').append(div);
        fomjar.util.async(function() {div.removeClass('disappear');});
        fomjar.util.async(function() {
            div.disappear();
        }, timeout)
    };
    div.disappear = function() {
        div.addClass('disappear');
        fomjar.util.async(function() {div.detach();}, frs.ui.DELAY);
    };

    if (content) div.append(content);

    return div;
};

frs.ui.shape = {};
frs.ui.shape.ArrowRight = function(line, color, width, height) {
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
frs.ui.shape.ArrowLeft = function(line, color, width, height) {
    var div = new frs.ui.shape.ArrowRight(line, color, width, height);
    div.find('>div').css('left',    '70.72%');
    div.find('>div').css(        'transform',  'translate(-50%, -50%) rotate(-135deg)');
    div.find('>div').css('-webkit-transform',  'translate(-50%, -50%) rotate(-135deg)');
    return div;
};
frs.ui.shape.ArrowUp = function(line, color, width, height) {
    var div = new frs.ui.shape.ArrowRight(line, color, width, height);
    div.find('>div').css('left',    '');
    div.find('>div').css('top',     '70.72%');
    div.find('>div').css(        'transform',  'translate(-50%, -50%) rotate(-45deg)');
    div.find('>div').css('-webkit-transform',  'translate(-50%, -50%) rotate(-45deg)');
    return div;
};
frs.ui.shape.ArrowDown = function(line, color, width, height) {
    var div = new frs.ui.shape.ArrowRight(line, color, width, height);
    div.find('>div').css('left',    '');
    div.find('>div').css('top',     '29.28%');
    div.find('>div').css(        'transform',  'translate(-50%, -50%) rotate(135deg)');
    div.find('>div').css('-webkit-transform',  'translate(-50%, -50%) rotate(135deg)');
    return div;
};
frs.ui.shape.Plus = function(line, color, width, height) {
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
frs.ui.shape.Option = function(point, color, width, height) {
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
frs.ui.shape.X = function(line, color, width, height) {
    var plus = new frs.ui.shape.Plus(line, color, width, height);
    plus.css(        'transform',  'scale(1.39) translate(-36%, -36%) rotate(45deg)');
    plus.css('-webkit-transform',  'scale(1.39) translate(-36%, -36%) rotate(45deg)');
    return plus;
};
frs.ui.shape.Drag = function(line, color, width, height) {
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

frs.ui.preview = function(src) {
    var div = $('<div></div>');
    div.addClass('preview disappear');
    var img = $('<img>');
    img.attr('src',     src);
    div.append(img);

    div.disappear = function() {
        mask.disappear();
        div.addClass('disappear');
        fomjar.util.async(function() {div.detach();}, frs.ui.DELAY);
    };
    var mask = new frs.ui.Mask();
    mask.bind('click', div.disappear);
    div.bind('click', div.disappear);

    mask.appear();
    $('.frs').append(div);
    fomjar.util.async(function() {div.removeClass('disappear');});

    return div;
};

FastClick.attach(document.body);

});
})(jQuery);
