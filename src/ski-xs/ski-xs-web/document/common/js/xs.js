var xs = {};

(function($) {

fomjar.framework.phase.append('ini', function() {
    xs.config = function(key, val) {
        if (val) {
            return fomjar.util.cookie(key, val);
        } else {
            val = fomjar.util.cookie(key);
            if (val && 0 < val.length) return val;
            else return undefined;
        }
    }
    xs.token    = xs.config('token');
    xs.uid      = parseInt(xs.config('uid'));
});

fomjar.framework.phase.append('dom', build_frame);
fomjar.framework.phase.append('ren', xsmain);

function build_frame() {
    var frame = $('<div></div>');
    frame.addClass('xs');

    var head = $('<div></div>');
    head.addClass('head disappear');
    var body = $('<div></div>');
    body.addClass('body disappear');

    frame.append([head, body]);
    $('body').append(frame);

    xs.ui.head().reset();
    xs.ui.body().reset();

    fomjar.util.async(function() {
        head.removeClass('disappear');
        body.removeClass('disappear');
    });
}

function xsmain() {
    xs.ui.body().append(create_page_stack());

    xs.user.login_auto();
}

function create_page_stack() {
    var stack = new xs.ui.PageStack();
    stack.page_set_switch_cb(stack.PAGE_SWITCH_CB_DEFAULT);

    stack.dir_in = function(dir) {
        if (!dir) dir = xs.user.dir_cur;
        if (!xs.user.map_dir[dir]) {
            new xs.ui.hud.Minor('目录"' + dir + '"不存在').appear(1500);
            return;
        };
        xs.user.dir_cur = dir;
        stack.page_push(create_page_dir(stack, xs.user.dir_cur));
    };
    stack.dir_out = function() {
        if (xs.user.dir_cur == '/') return;

        xs.user.dir_cur = xs.user.dir_cur.substring(0, xs.user.dir_cur.length - 1);
        xs.user.dir_cur = xs.user.dir_cur.substring(0, xs.user.dir_cur.lastIndexOf('/') + 1);
        stack.page_pop();
    };
    stack.art_new = function(art) {
        stack.page_push(create_page_art(stack));
    };

    stack.dir_in();
    return stack;
}

function create_page_dir(stack, dir) {
    var op_l = dir == '/' ? xs.ui.head().cover : new xs.ui.Button('返回', function() {stack.dir_out();});
    var dir_name = xs.user.dir_name(dir);
    var page = new xs.ui.Page();
    page.head(
        op_l,
        new xs.ui.Button(dir_name),
        [
            create_button_new(stack)
        ]
    );
    var list = new xs.ui.List();
    page.append(list);

    page.dir_refresh = function() {
        list.addClass('disappear');
        fomjar.util.async(function() {
            list.children().detach();

            $.each(xs.user.dir_sub(dir), function(i, d) {
                list.append_cell({
                    icon        : 'res/folder.png',
                    major       : xs.user.dir_name(d),
                    accessory   : true,
                    action      : function() {stack.dir_in(d);}
                });
            });
            list.removeClass('disappear')
        }, xs.ui.DELAY);
    };
    page.dir_refresh();
    return page;
}

function create_page_art(stack) {
    var ae = new xs.ui.ArticleEditor();
    var page = new xs.ui.Page();
    page.head(
        new xs.ui.Button('返回', function() {stack.page_pop();}),
        new xs.ui.Button('新文章'),
        [
            new xs.ui.Button('预览', function() {
                var page = new xs.ui.Page();
                var jump = null;
                page.head(null, null, new xs.ui.Button('关闭', function() {jump.drop();}));
                jump = page.jump();
            }),
            new xs.ui.Button('完成', function() {stack.page_pop();})
        ]
    );
    page.append(ae);
    return page;
}

function create_button_new(stack) {
    var shape = new xs.ui.shape.Plus('1px', '#ccccff');
    shape.addClass('center');
    var button = new xs.ui.Button(shape, function() {
        var list = new xs.ui.List();
        var mask = new xs.ui.Mask();
        var dialog = new xs.ui.Dialog();
        dialog.style_popupmenu({
            title   : '新建',
            content : list
        });
        mask.bind('click', function() {
            mask.disappear();
            dialog.disappear();
        });

        list.append_cell({
            major   : '目录',
            accessory   : true,
            align   : 'center',
            action  : function() {
                var mask1 = new xs.ui.Mask();
                var dialog1 = new xs.ui.Dialog();
                dialog1.append_text_h1c('新建目录');
                dialog1.append_space('1em');
                dialog1.append_input({
                    'type'          : 'text',
                    'placeholder'   : '输入目录名称'
                });
                dialog1.append_button(new xs.ui.Button('创建', function() {
                    var subdir = dialog1.find('input').val();
                    if (!subdir || '' == subdir || '/' == subdir) {
                        new xs.ui.hud.Minor('目录不合法').appear(1500);
                        dialog1.shake();
                        return;
                    }
                    if (subdir.indexOf('/') >= 0 || subdir.indexOf('\"') >= 0 || subdir.indexOf('\'') >= 0) {
                        new xs.ui.hud.Minor('目录不合法，不能包含英文符号：/、\"、\'').appear(1500);
                        dialog1.shake();
                        return;
                    }
                    if (xs.user.map_dir[xs.user.dir_cur + subdir + '/']) {
                        new xs.ui.hud.Minor('目录已存在').appear(1500);
                        dialog1.shake();
                        return;
                    }

                    xs.user.dir_new(subdir);
                    stack.page_cur().dir_refresh();

                    mask1.disappear();
                    dialog1.disappear();
                })).to_high();

                mask1.bind('click', function() {
                    mask1.disappear();
                    dialog1.disappear();
                });

                mask.disappear();
                dialog.disappear();
                mask1.appear();
                dialog1.appear();

                dialog1.find('input').focus();
            }
        });
        list.append_cell({
            major   : '文章',
            align   : 'center',
            action  : function() {
                mask.disappear();
                dialog.disappear();

                stack.art_new();
            }
        });

        mask.appear();
        dialog.appear();
    });

    return button;
}

})(jQuery)

