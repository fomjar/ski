
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
    tab.add_tab('清单视图', tab.tab_list = $('<div></div>'), true);
    tab.add_tab('预览视图', tab.tab_prev = frs.ui.layout.lr($('<div></div>')));
    tab.tab_prev.css('height', '100%');
    tab.tab_list.css('height', '100%');

    frs.ui.body().append(frs.ui.body().tab = tab);
    
    build_body_dev_prev();
    build_body_dev_list();
}

function build_body_dev_prev() {
    var bar = $('<div></div>');
    bar.addClass('bar-s');
    bar.append(new frs.ui.Button(new frs.ui.shape.Plus('1px', '#333366', '1.5em', '1.5em'), op_create_dev));
    
    frs.ui.body().tab.tab_prev.l.append(bar);
}

function build_body_dev_list() {
    var bar = $('<div></div>');
    bar.addClass('bar-l');
    bar.append([
        new frs.ui.Button('创建设备', op_create_dev),
    ]);
    var head = new frs.ui.ListCellTable(['编号', '路径', '创建时间', 'IP地址', '设备状态', '操作']);
    head.css('font-weight', '700');
    head.css('padding', '1em');
    head.css('background', 'white');
    var list = new frs.ui.List();
    frs.ui.body().tab.tab_list.append([bar, head, list]);
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
    
        update_dev_prev(desc);
        update_dev_list(desc);
    });
}

function update_dev_prev(devs) {
    var tab_prev = frs.ui.body().tab.tab_prev;
    tab_prev.l.find('.jstree').detach();
    select_devs_prev();
    
    var data = tree_dev(devs);
    var tree = $('<div></div>').jstree({core : {data : data.children}});
    tree.data = data;
    frs.ui.body().tab.tab_prev.l.append(tree);
    
    tree.on('select_node.jstree', function(e, data) {
        var node = tree.data.find_child_deep(data.node.text);
        var devs = $.map(node.leaves(), function(n) {return n.dev;});
        select_devs_prev(devs);
    });
}

function update_dev_list(devs) {
    var tab_list = frs.ui.body().tab.tab_list;
    var list = tab_list.find('.list');
    list.children().detach();
    
    $.each(devs, function(i, dev) {
        var btn_del;
        dev.div = $('<div></div>');
        dev.div.text('正在检测');
        list.append(new frs.ui.ListCellTable([
            dev.did,
            dev.path,
            dev.time.replace('.0', ''),
            dev.ip,
            dev.div,
            [btn_del = new frs.ui.Button('删除', function() {op_delete_dev(dev);}).to_major()]
        ]));
        
        btn_del.css('background', '#663333');
    });
    
    if (devs && devs.length) {
        fomjar.util.async(function() {
            var player = new frs.video.Player(devs);
            player.login();
            $.each(devs, function(i, dev) {
                player.info(i, function(dev, xml) {
                    dev.div.text('在线');
                    dev.div.css('color', 'green');
                }, function(dev) {
                    dev.div.text('离线');
                    dev.div.css('color', 'red');
                });
            });
            player.destory();
        });
    }
}

function select_devs_prev(devs) {
    var tab_prev = frs.ui.body().tab.tab_prev;
    if (tab_prev.r.player) tab_prev.r.player.destory();
    tab_prev.r.children().detach();
    
    if (!devs || !devs.length) return;
    
    var bar = $('<div></div>');
    bar.addClass('bar-l');
    if (1 == devs.length) {
        var btn_del;
        bar.append([
            btn_del = new frs.ui.Button('删除此设备', function() {op_delete_dev(devs[0]);}).to_major(),
        ]);
        btn_del.css('float', 'right');
        btn_del.css('background', '#663333');
    }
    
    var div_player = $('<div></div>');
    div_player.addClass('player');
    var id = 'player_' + new Date().getTime().toString();
    div_player.attr('id', id);
    
    tab_prev.r.append([bar, div_player]);
    
    if (frs.video.available()) {
        var player = new frs.video.Player(devs, id);
        tab_prev.r.player = player;
        fomjar.util.async(function() {
            player.login(
                function() {},
                function() {alert('登陆失败');}
            );
            player.play();
        });
    }
}

function update_pic() {
}

function op_create_dev() {
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
    var user = dialog.append_input({placeholder : '用户名'});
    var pass = dialog.append_input({placeholder : '密码', type : 'password'});
    var checkNoDid = function() {
        if (!path.val()) {
            new frs.ui.hud.Minor('显示路径不能为空').appear(1500);
            dialog.shake();
            return false;
        }
        if (!ip.val()) {
            new frs.ui.hud.Minor('IP地址不能为空').appear(1500);
            dialog.shake();
            return false;
        }
        if (!user.val()) {
            new frs.ui.hud.Minor('用户名不能为空').appear(1500);
            dialog.shake();
            return false;
        }
        if (!pass.val()) {
            new frs.ui.hud.Minor('密码不能为空').appear(1500);
            dialog.shake();
            return false;
        }
        return true;
    };
    dialog.append_buttons([
        new frs.ui.Button('自动获取编号', function() {
            if (!checkNoDid()) return;
            
            fomjar.util.async(function() {
                var player = new frs.video.Player([{ip : ip.val(), user : user.val(), pass : pass.val()}]);
                player.login(function(dev, xml) {
                    player.info(0, function(dev, xml) {did.val(xml.childNodes[0].childNodes[3].textContent);});
                }, function() {
                    new frs.ui.hud.Minor('登陆设备失败').appear(1500);
                    dialog.shake();
                });
                player.destory();
            });
        }).to_major(),
        new frs.ui.Button('提交', function() {
            if (!did.val()) {
                new frs.ui.hud.Minor('编号不能为空').appear(1500);
                dialog.shake();
                return false;
            }
            if (!checkNoDid()) return;
            
            fomjar.net.send(ski.isis.INST_UPDATE_DEV, {
                did     : did.val(),
                path    : path.val(),
                ip      : ip.val(),
                user    : user.val(),
                pass    : pass.val()
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
        }).to_major()
    ]);
    
    mask.appear();
    dialog.appear();
}

function op_delete_dev(dev) {
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
            if (!cur.find_child(t)) {
                c = tree_node(t);
                cur.children.push(c);
            } else c = cur.find_child(t);
            
            cur = c;
            i = j + 1;
        }
        
        var t = path.substring(i);
        if (!cur.find_child(t)) {
            var c = tree_node(t + '(' + dev.did + ')');
            c.dev = dev;
            cur.children.push(c);
        }
    });
    return tree;
}

function tree_node(text) {
    var node = {
        text        : text,
        children    : [],
        state       : {
            opened  : true,
        },
        find_child  : function(text) {
            var r = null;
            $.each(node.children, function(i, c) {
                if (c.text == text) {
                    r = c;
                    return false;
                }
            });
            return r;
        },
        find_child_deep : function(text) {
            if (node.text == text) return node;
            
            var r = null;
            $.each(node.children, function(i, c) {
                if (r = c.find_child_deep(text)) return false;
            });
            return r;
        },
        leaves  : function() {
            var r = [];
            if (!node.children.length) r.push(node);
            else {
                $.each(node.children, function(i, c) {
                    r = r.concat(c.leaves());
                });
            }
            return r;
        }
    };
    return node;
}

})(jQuery)

