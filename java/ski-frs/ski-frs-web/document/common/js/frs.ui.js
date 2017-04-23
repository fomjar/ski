(function($) {
fomjar.framework.phase.append('ini', function() {

frs.ui = {};
frs.ui.DELAY = 500;

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
        fomjar.util.async(function() {
            var inputs = dialog.find('input');
            if (inputs.length > 0) {
                $(inputs[0]).focus();
            }
        }, frs.ui.DELAY);
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
        div.css('font-weight',  '700');
        div.append(text);
        dialog.append(div);
        return div;
    };
    dialog.append_text_h1c = function(text) {
        var div = $('<div></div>');
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
        div.css('text-align',   'center');
        div.append(text);
        dialog.append(div);
        return div;
    };
    dialog.append_input = function(attr) {
        var div = $('<div></div>');
        var input = $('<input>');
        input.css('width',      '100%');
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
        button.css('width', '100%');

        dialog.append(div);
        return button;
    };
    dialog.append_buttons = function(buttons) {
        var table = $('<table><tr></tr></table>');
        table.css('width', '100%');
        $.each(buttons, function(i, button) {
            button.css('width', '100%');
            
            var td = $('<td></td>');
            td.css('padding', '.5em');
            td.append(button);
            table.find('tr').append(td);
        });
        
        dialog.append(table);
        return buttons;
    };
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
            var div = new frs.ui.shape.ArrowRight('1px', 'rgba(0, 0, 0, .2)');
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
frs.ui.ListCellTable = function(columns) {
    var cell = $('<div></div>');
    cell.addClass('list-cell-table');
    
    var width = 100 / columns.length;
    $.each(columns, function(i, c) {
        var dc = $('<div></div>');
        dc.css('width', width + '%');
        dc.append(c);
        cell.append(dc);
    });
    
    return cell;
};

frs.ui.Tab = function() {
    var div = $('<div></div>');
    div.addClass('tab');
    div.append(div.tab = $('<div></div>'));
    div.append(div.con = $('<div></div>'));
    
    div.tabs = {};
    div.cur_con = function() {
        var children = div.con.children();
        if (0 == children.length) return null;
        return $(children[0]);
    };
    div.tab_index = function(name) {
        var i = -1;
        var j = 0;
        $.each(div.tabs, function(k, v) {
            if (k == name) {
                i = j;
                return false;
            }
            j++;
        });
        return i;
    };
    div.get_tab = function(name) {
        var i = div.tab_index(name);
        if (-1 == i) return null;
        
        return div.tab.find('>div:nth-child(' + (i + 1) + ')');
    }
    div.add_tab = function(name, content, isDefault) {
        div.tabs[name] = content;
        content.addClass('fast');
        
        var tab = $('<div></div>');
        tab.text(name);
        div.tab.append(tab);
        tab.bind('click', function() {div.to_tab(name);});
        
        if (isDefault) div.to_tab(name);
        return tab;
    };
    div.to_tab = function(name) {
        var cur = div.tabs[name];
        if (!cur) return;
        
        var old = div.cur_con();
        if (old) {
            old.addClass('disappear');
            cur.addClass('disappear');
            div.tab.children().removeClass('active');
            div.get_tab(name).addClass('active');
            fomjar.util.async(function() {
                if (old.on_disappear) old.on_disappear();
                old.detach();
                div.con.append(cur);
                fomjar.util.async(function() {cur.removeClass('disappear');});
                if (cur.on_appear) cur.on_appear();
                
            }, frs.ui.DELAY / 2);
        } else {
            cur.addClass('disappear');
            div.get_tab(name).addClass('active');
            fomjar.util.async(function() {
                div.con.append(cur);
                cur.removeClass('disappear');
                if (cur.on_appear) cur.on_appear();
            });
        }
    }
    return div;
};

frs.ui.Pager = function(cur, len, cb_turn) {
    var div = $('<div></div>');
    div.addClass('pager');
    div.div_pre = new frs.ui.Button('上一页');
    div.div_suf = new frs.ui.Button('下一页');
    div.div_cur = $('<div></div>');
    div.append([div.div_pre, div.div_cur, div.div_suf]);
    
    div.len = len;
    div.cur = cur;
    div.div_cur.text(cur);
    div.turn = function(i) {
        if (i > div.len || i <= 0) return;
        
        div.cur = i;
        div.div_cur.text(div.cur);
        if (cb_turn) cb_turn(div.cur);
    };
    
    div.div_pre.bind('click', function() {div.turn(div.cur - 1);});
    div.div_suf.bind('click', function() {div.turn(div.cur + 1);});
    return div;
};

frs.ui.Progress = function() {
    var div = $('<div></div>');
    div.addClass('progress');
    var div_perc = $('<div></div>');
    var div_text = $('<div></div>');
    div.append([div_perc, div_text]);
    
    div.val = function(percent, text) {
        if (percent) div_perc.css('width', percent + '%');
        if (text)    div_text.text(text);
    };
    return div;
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
    div.find('>div:nth-child(1)').css('left',   '10%');
    div.find('>div:nth-child(3)').css('left',   '90%');

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

    div.find('>div:nth-child(1)').css('top',    '10%');
    div.find('>div:nth-child(3)').css('top',    '90%');

    if (width)  div.css('width',  width);
    if (height) div.css('height', height);
    return div;
};


frs.ui.head = function() {
    if (frs.ui._head) return frs.ui._head;

    var head = $('.frs .head');
    head.append_item = function(item, action) {
        var div = $('<div></div>');
        div.append(item);
        if (action) div.bind('click', action);
        head.append(div);
        return div;
    };
    head.append_space = function(width) {
        var space = $('<div></div>');
        space.css('padding', '0');
        space.css('width',   width);
        head.append(space);
        return space;
    };
    head.style_default = function() {
        head.children().detach();
        
        var home = $('<div>APP</div>');
        home.addClass('home');
        home.bind('click', function() {window.location = 'app.html'});
        
        head.append(home);
        head.append_space('2em');
    };
    
    frs.ui._head = head;
    return frs.ui._head;
}

frs.ui.body = function() {
    if (frs.ui._body) return frs.ui._body;

    var body = $('.frs .body');

    frs.ui._body = body;
    return frs.ui._body;
};

frs.ui.layout = {};

frs.ui.layout.lr = function(c) {
    c.l = $('<div></div>');
    c.l.addClass('l');
    c.r = $('<div></div>');
    c.r.addClass('r');
    c.append([c.l, c.r]);
    return c;
};

frs.ui.layout.lrb = function(c) {
    c.t = $('<div></div>');
    c.t.addClass('t');
    c.l = $('<div></div>');
    c.l.addClass('l');
    c.r = $('<div></div>');
    c.r.addClass('r');
    c.b = $('<div></div>');
    c.b.addClass('b');
    c.append([c.t.append([c.l, c.r]), c.b]);
    return c;
};

frs.ui.BlockPicture = function(options) {
    options = options || {};
    var div = $('<div></div>');
    div.addClass("block-picture");
    
    if (options.cover) {
        var img = $('<img>');
        img.attr('src', options.cover);
        div.append(img);
        div.bind('click', function() {
            var mask = new frs.ui.Mask();
            mask.css('background', 'black');
            var dialog = new frs.ui.Dialog();
            dialog.css('width', '80%');
            dialog.css('height', '80%');
            dialog.css('padding', '0');
            var img1 = $('<img>');
            img1.css('width', '100%');
            img1.css('height', '100%');
            if (-1 < options.cover.indexOf('/F00')){
                img1.attr('src', options.cover.replace('/F00', '/'));
            } else if (-1 < options.cover.indexOf('/F01')){
                img1.attr('src', options.cover.replace('/F01', '/'));
            } else if (-1 < options.cover.indexOf('/F02')){
                img1.attr('src', options.cover.replace('/F02', '/'));
            } else if (-1 < options.cover.indexOf('/F20')){
                img1.attr('src', options.cover.replace('/F20', '/20'));
            } else {
                img1.attr('src', options.cover);
            }
            dialog.append(img1);
            
            var disappear = function() {
                mask.disappear();
                dialog.disappear();
            };
            mask.bind('click', disappear);
            dialog.bind('click', disappear);
            
            mask.appear();
            dialog.appear();
        });
    }
    if (options.name) {
        var name = $('<div></div>');
        name.html(options.name);
        div.append(name);
    }
    
    return div;
};

});
})(jQuery);
