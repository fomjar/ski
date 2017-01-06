
var xs = {};
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

(function($) {

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

    fomjar.util.async(function() {
        head.removeClass('disappear');
        body.removeClass('disappear');
    });
}

function xsmain() {
    xs.ui.head().reset();
    xs.ui.body().reset();
    xs.ui.body().append(create_page_stack());

    xs.user.login_auto();
}

function create_page_stack() {
    var stack = new xs.ui.PageStack();
    stack.page_set_switch_cb(stack.PAGE_SWITCH_CB_DEFAULT);
    stack.page_push(create_page_folder(stack));
    return stack;
}

function create_page_folder(stack) {
    var page = new xs.ui.Page({
        name    : 'folder',
        op_l    : xs.ui.head().cover,
        op_r    : [
            create_button_new(stack)
        ]
    });
    return page;
}

function create_page_article_new(stack) {
    var page = new xs.ui.Page({
        name    : 'article.new',
        op_l    : new xs.ui.Button('返回', function() {stack.page_pop();}),
        op_r    : new xs.ui.Button('完成', function() {stack.page_pop();})
    });
    return page;
}

function create_button_new(stack) {
    var button = new xs.ui.Button($("<img src='res/new.png' />"), function() {
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

        list.append_text('文件夹', function() {
            var mask1 = new xs.ui.Mask();
            var dialog1 = new xs.ui.Dialog();
            dialog1.append_text_h1c('新建文件夹');
            dialog1.append_space('1em');
            dialog1.append_input({
                'type'          : 'text',
                'placeholder'   : '输入文件夹名称'
            });
            dialog1.append_button(new xs.ui.Button('创建', function() {
                dialog1.shake();
            })).to_high();

            mask1.bind('click', function() {
                mask1.disappear();
                dialog1.disappear();
            });

            mask.disappear();
            dialog.disappear();
            mask1.appear();
            dialog1.appear();
        });
        list.append_text('文章', function() {
            mask.disappear();
            dialog.disappear();
            stack.page_push(create_page_article_new(stack));
        });

        mask.appear();
        dialog.appear();
    });

    return button;
}

})(jQuery)

