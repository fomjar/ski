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
        head.cover.img.attr('src', 'data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAASABIAAD/4QBMRXhpZgAATU0AKgAAAAgAAgESAAMAAAABAAEAAIdpAAQAAAABAAAAJgAAAAAAAqACAAQAAAABAAAB86ADAAQAAAABAAAB9gAAAAD/4QkhaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLwA8P3hwYWNrZXQgYmVnaW49Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/PiA8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4OnhtcHRrPSJYTVAgQ29yZSA1LjQuMCI+IDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+IDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiLz4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICA8P3hwYWNrZXQgZW5kPSJ3Ij8+AP/tADhQaG90b3Nob3AgMy4wADhCSU0EBAAAAAAAADhCSU0EJQAAAAAAENQdjNmPALIE6YAJmOz4Qn7/4hskSUNDX1BST0ZJTEUAAQEAABsUYXBwbAIQAABtbnRyUkdCIFhZWiAH4QABAAMACQA1AAVhY3NwQVBQTAAAAABBUFBMAAAAAAAAAAAAAAAAAAAAAAAA9tYAAQAAAADTLWFwcGwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABFkZXNjAAABUAAAAGJkc2NtAAABtAAABBhjcHJ0AAAFzAAAACN3dHB0AAAF8AAAABRyWFlaAAAGBAAAABRnWFlaAAAGGAAAABRiWFlaAAAGLAAAABRyVFJDAAAGQAAACAxhYXJnAAAOTAAAACB2Y2d0AAAObAAABhJuZGluAAAUgAAABj5jaGFkAAAawAAAACxtbW9kAAAa7AAAAChiVFJDAAAGQAAACAxnVFJDAAAGQAAACAxhYWJnAAAOTAAAACBhYWdnAAAOTAAAACBkZXNjAAAAAAAAAAhEaXNwbGF5AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAbWx1YwAAAAAAAAAiAAAADGhySFIAAAAUAAABqGtvS1IAAAAMAAABvG5iTk8AAAASAAAByGlkAAAAAAASAAAB2mh1SFUAAAAUAAAB7GNzQ1oAAAAWAAACAGRhREsAAAAcAAACFnVrVUEAAAAcAAACMmFyAAAAAAAUAAACTml0SVQAAAAUAAACYnJvUk8AAAASAAACdm5sTkwAAAAWAAACiGhlSUwAAAAWAAACnmVzRVMAAAASAAACdmZpRkkAAAAQAAACtHpoVFcAAAAMAAACxHZpVk4AAAAOAAAC0HNrU0sAAAAWAAAC3npoQ04AAAAMAAACxHJ1UlUAAAAkAAAC9GZyRlIAAAAWAAADGG1zAAAAAAASAAADLmNhRVMAAAAYAAADQHRoVEgAAAAMAAADWGVzWEwAAAASAAACdmRlREUAAAAQAAADZGVuVVMAAAASAAADdHB0QlIAAAAYAAADhnBsUEwAAAASAAADnmVsR1IAAAAiAAADsHN2U0UAAAAQAAAD0nRyVFIAAAAUAAAD4mphSlAAAAAMAAAD9nB0UFQAAAAWAAAEAgBMAEMARAAgAHUAIABiAG8AagBpzuy37AAgAEwAQwBEAEYAYQByAGcAZQAtAEwAQwBEAEwAQwBEACAAVwBhAHIAbgBhAFMAegDtAG4AZQBzACAATABDAEQAQgBhAHIAZQB2AG4A/QAgAEwAQwBEAEwAQwBEAC0AZgBhAHIAdgBlAHMAawDmAHIAbQQaBD4EOwRMBD4EQAQ+BDIEOAQ5ACAATABDAEQgDwBMAEMARAAgBkUGRAZIBkYGKQBMAEMARAAgAGMAbwBsAG8AcgBpAEwAQwBEACAAYwBvAGwAbwByAEsAbABlAHUAcgBlAG4ALQBMAEMARCAPAEwAQwBEACAF5gXRBeIF1QXgBdkAVgDkAHIAaQAtAEwAQwBEX2mCcgAgAEwAQwBEAEwAQwBEACAATQDgAHUARgBhAHIAZQBiAG4A6QAgAEwAQwBEBCYEMgQ1BEIEPQQ+BDkAIAQWBBoALQQ0BDgEQQQ/BDsENQQ5AEwAQwBEACAAYwBvAHUAbABlAHUAcgBXAGEAcgBuAGEAIABMAEMARABMAEMARAAgAGUAbgAgAGMAbwBsAG8AcgBMAEMARAAgDioONQBGAGEAcgBiAC0ATABDAEQAQwBvAGwAbwByACAATABDAEQATABDAEQAIABDAG8AbABvAHIAaQBkAG8ASwBvAGwAbwByACAATABDAEQDiAOzA8cDwQPJA7wDtwAgA78DuAPMA70DtwAgAEwAQwBEAEYA5AByAGcALQBMAEMARABSAGUAbgBrAGwAaQAgAEwAQwBEMKsw6TD8AEwAQwBEAEwAQwBEACAAYQAgAEMAbwByAGUAc3RleHQAAAAAQ29weXJpZ2h0IEFwcGxlIEluYy4sIDIwMTcAAFhZWiAAAAAAAADzUgABAAAAARbPWFlaIAAAAAAAAGXoAAA8EAAACdBYWVogAAAAAAAAapMAAKrFAAAXilhZWiAAAAAAAAAmWwAAGSwAALHSY3VydgAAAAAAAAQAAAAABQAKAA8AFAAZAB4AIwAoAC0AMgA2ADsAQABFAEoATwBUAFkAXgBjAGgAbQByAHcAfACBAIYAiwCQAJUAmgCfAKMAqACtALIAtwC8AMEAxgDLANAA1QDbAOAA5QDrAPAA9gD7AQEBBwENARMBGQEfASUBKwEyATgBPgFFAUwBUgFZAWABZwFuAXUBfAGDAYsBkgGaAaEBqQGxAbkBwQHJAdEB2QHhAekB8gH6AgMCDAIUAh0CJgIvAjgCQQJLAlQCXQJnAnECegKEAo4CmAKiAqwCtgLBAssC1QLgAusC9QMAAwsDFgMhAy0DOANDA08DWgNmA3IDfgOKA5YDogOuA7oDxwPTA+AD7AP5BAYEEwQgBC0EOwRIBFUEYwRxBH4EjASaBKgEtgTEBNME4QTwBP4FDQUcBSsFOgVJBVgFZwV3BYYFlgWmBbUFxQXVBeUF9gYGBhYGJwY3BkgGWQZqBnsGjAadBq8GwAbRBuMG9QcHBxkH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');
        head.cover.div.text('杜逢佳');
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
    if (article.tags) {
        var at = $('<div></div>');
        at.addClass('at');
        $.each(article.tags, function(i, t) {
            var tag = $('<div></div>');
            tag.text(t);
            at.append(tag);
        });
        av.append(at);
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
