
(function($) {

fomjar.framework.phase.append('dom', frsmain);
fomjar.framework.phase.append('ren', update);

var menu = fomjar.util.args.menu || 'dev';

function frsmain() {
    build_head();
    build_body();
}

function build_head() {
    frs.ui.head().style_default();
    var menu_dev = frs.ui.head().append_item('设备管理', function() {window.location = window.location.pathname + '?menu=dev';});
    var menu_pic = frs.ui.head().append_item('图库管理', function() {window.location = window.location.pathname + '?menu=pic';});
    switch (menu) {
    case 'dev': menu_dev.addClass('active'); break;
    case 'pic': menu_pic.addClass('active'); break;
    }
}

function build_body() {
    switch (menu) {
    case 'dev': build_body_dev(); break;
    case 'pic': build_body_pic(); break;
    }
}

function build_body_dev() {
    var tab = new frs.ui.Tab();
    tab.addClass('tab-shadow tab-fix');
    tab.css('height', '100%');
    tab.add_tab('预览视图', tab.tab_tree = frs.ui.layout.lr($('<div></div>')), true);
    tab.add_tab('清单视图', tab.tab_list = $('<div></div>'));
    tab.tab_tree.css('height', '100%');
    tab.tab_list.css('height', '100%');

    frs.ui.body().append(frs.ui.body().tab = tab);
    
    build_body_dev_tree();
}

function build_body_dev_tree() {
    var bar = $('<div></div>');
    bar.addClass('bar-s');
    bar.append(new frs.ui.Button(new frs.ui.shape.Plus('1px', '#333344', '1.5em', '1.5em'), create_dev));
    
    frs.ui.body().tab.tab_tree.l.append(bar);
}

function build_body_pic() {
    var bar = $('<div></div>');
    bar.addClass('bar-l');
    
    bar.append([
        new frs.ui.Button('批量导入'),
        new frs.ui.Button('批量删除')
    ]);
    
    frs.ui.body().append(bar);
}

function update() {
    switch (menu) {
    case 'dev': update_dev(); break;
    case 'pic': update_pic(); break;
    }
}

function update_dev() {
    var tab_tree = frs.ui.body().tab.tab_tree;
    
    tab_tree.l.find('.jstree').detach();
    select_dev();
    
    var mask = new frs.ui.Mask();
    var hud = frs.ui.hud.Major('正在获取');
    mask.appear();
    hud.appear();
    fomjar.net.send(ski.isis.INST_QUERY_DEV, function(code, desc) {
        mask.disappear();
        hud.disappear();
        
        if (code) {
            new frs.ui.hud.Minor(desc).appear(1500);
            return;
        }
        
        var tree = $('<div></div>').jstree({core : {data : tree_dev(desc)}});
        frs.ui.body().tab.tab_tree.l.append(tree);
        tree.on('select_node.jstree', function(e, data) {if (data.node.original.leaf) select_dev(data.node.original.dev);});
    });
}

function tree_dev(devs) {
    var tree = tree_node('root');
    $.each(devs, function(i, dev) {
        var i = 0;
        var j = 0;
        var path = dev.path;
        var cur = tree;
        while (-1 < (j = path.indexOf('/', i))) {
            var t = path.substring(i, j);
            var c;
            if (!cur.find(t)) {
                c = tree_node(t);
                cur.children.push(c);
            } else c = cur.find(t);
            
            cur = c;
            i = j + 1;
        }
        
        var t = path.substring(i);
        if (!cur.find(t)) {
            var c = tree_node(t);
            c.leaf = true;
            c.dev = dev;
            cur.children.push(c);
        }
    });
    return tree.children;
}

function tree_node(text) {
    var node = {
        text        : text,
        children    : [],
        state       : {
            opened  : true,
        },
        find        : function(text) {
            var r;
            $.each(node.children, function(i, c) {
                if (c.text == text) {
                    r = c;
                    return false;
                }
            });
            return r;
        }
    };
    return node;
}

function update_pic() {
    
}

function create_dev() {
    var mask = new frs.ui.Mask();
    var dialog = new frs.ui.Dialog();
    mask.bind('click', function() {
        mask.disappear();
        dialog.disappear();
    });
    
    dialog.append_text_h1c('创建设备');
    dialog.append_space('.5em');
    var did  = dialog.append_input({placeholder : '编号'});
    var path = dialog.append_input({placeholder : '显示路径，以英文“/”号分割'});
    var ip   = dialog.append_input({placeholder : 'IP地址'});
    dialog.append_button(new frs.ui.Button('提交', function() {
        if (!did.val()) {
            new frs.ui.hud.Minor('编号不能为空').appear(1500);
            dialog.shake();
            return;
        }
        if (!path.val()) {
            new frs.ui.hud.Minor('显示路径不能为空').appear(1500);
            dialog.shake();
            return;
        }
        fomjar.net.send(ski.isis.INST_UPDATE_DEV, {
            did     : did.val(),
            path    : path.val(),
            ip      : ip.val()
        }, function(code, desc) {
            if (code) {
                new frs.ui.hud.Minor(desc).appear(1500);
                dialog.shake();
                return;
            }
            new frs.ui.hud.Minor('创建成功').appear(1500);
            mask.disappear();
            dialog.disappear();
            update();
        });
    }).to_major());
    
    mask.appear();
    dialog.appear();
}

function select_dev(dev) {
    var tab_tree = frs.ui.body().tab.tab_tree;
    
    tab_tree.r.children().detach();
    
    if (!dev) return;
    
    var bar = $('<div></div>');
    bar.addClass('bar-l');
    var btn_del;
    bar.append([
        btn_del = new frs.ui.Button('删除此设备', function() {
            var mask = new frs.ui.Mask();
            var dialog = new frs.ui.Dialog();
            mask.bind('click', function() {
                mask.disappear();
                dialog.disappear();
            });
            
            dialog.append_text_h1c('删除设备');
            dialog.append_space('.5em');
            dialog.append_text_p1('确定要删除设备：' + dev.did + ':' + dev.path + '吗？');
            dialog.append_button(new frs.ui.Button('确认', function() {
                fomjar.net.send(ski.isis.INST_UPDATE_DEV_DEL, {did : dev.did}, function(code, desc) {
                    if (code) {
                        new frs.ui.hud.Minor(desc).appear(1500);
                        dialog.shake();
                        return;
                    }
                    new frs.ui.hud.Minor('删除成功').appear(1500);
                    mask.disappear();
                    dialog.disappear();
                    update();
                });
            }).to_major());
            
            mask.appear();
            dialog.appear();
        }).to_major(),
    ]);
    tab_tree.r.append(bar);
    
    btn_del.css('float', 'right');
    btn_del.css('background', '#996666');
}

})(jQuery)

