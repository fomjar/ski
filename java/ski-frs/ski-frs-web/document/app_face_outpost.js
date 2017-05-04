
(function($) {

fomjar.framework.phase.append('dom', frsmain);
fomjar.framework.phase.append('ren', update_dev);

function frsmain() {
    build_head();
    build_body();
}

function build_head() {
    frs.ui.head().style_default();
    frs.ui.head().append_item('卡口管理', function() {window.location = 'app_face_outpost.html';}).addClass('active');
    frs.ui.head().append_item('特征搜索', function() {window.location = 'app_face_property.html';});
    frs.ui.head().append_item('身份确认', function() {window.location = 'app_face_id.html';});
    frs.ui.head().append_item('实时布控');
    frs.ui.head().append_item('轨迹管理', function() {window.location = 'app_face_trail.html';});
    frs.ui.head().append_item('人像库管理', function() {window.location = 'app_face_lib.html';});
    frs.ui.head().append_item('分析统计');
}

function build_body() {
    var tab = new frs.ui.Tab();
    tab.addClass('tab-shadow tab-fix');
    tab.css('height', '100%');
    tab.add_tab('在线卡口', tab.tab_on = create_tab_online(), true);
    tab.add_tab('离线卡口', tab.tab_of = create_tab_offline());
    frs.ui.body().append(frs.ui.body().tab = tab);
}

function create_tab_online() {
    var div = frs.ui.layout.lrb($('<div></div>'));
    div.addClass('tab-on');
    return div;
}

function create_tab_offline() {
    var div = $('<div></div>');
    div.addClass('tab-of');
    var select;
    var path_local;
    var path_view;
    var submit;
    div.append([
        $('<div></div>').append([
            select = $("<select></select>"),
            path_local = $("<input type='text' placeholder='视频文件的本地路径'>"),
            path_view = $("<input type='text' placeholder='设备虚拟显示路径'>"),
            submit = new frs.ui.Button('开始分析', function() {
                div.find('>div').css('top', '0');
                
                var mask = new frs.ui.Mask();
                mask.appear();
                var hud = frs.ui.hud.Major('正在创建离线设备');
                hud.appear();
                var did = 'offline-' + new Date().getTime().toString(16);
                fomjar.net.send(ski.isis.INST_UPDATE_DEV, {
                    did     : did,
                    path    : path_view.val()
                }, function(code, desc) {
                    hud.disappear();
                    if (code) {
                        new frs.ui.hud.Minor(desc).appear(1500);
                        return;
                    }
                    
                    hud.text('正在提交分析');
                    hud.appear();
                    fomjar.net.send(ski.isis.INST_APPLY_DEV_IMPORT, {
                        did     : did,
                        path    : path_local.val()
                    }, function(code, desc) {
                        hud.disappear();
                        if (code) {
                            new frs.ui.hud.Minor(desc).appear(1500);
                            return;
                        }
                        
                        new frs.ui.hud.Minor('已经在分析了').appear(1500);
                    });
                });
            }).to_major()
        ]),
    ]);
    div.on_appear = function() {
        fomjar.util.async(function() {
            select.children().detach()
            
            var mask = new frs.ui.Mask();
            var hud = frs.ui.hud.Major('正在获取');
            mask.appear();
            hud.appear();
            fomjar.net.send(ski.isis.INST_QUERY_OPP, function(code, desc) {
                mask.disappear();
                hud.disappear();
                if (code) {
                    new frs.ui.hud.Minor(desc).appear(1500);
                    return;
                }
                $.each(desc, function(i, a) {select.append("<option value='" + a.server + "'>" + a.server + '(' + a.host + ':' + a.port + ')' + "</option>");});
            });
        }, frs.ui.DELAY / 2);
    };
    return div;
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
    
        update_dev_online(desc);
    });
}

function update_dev_online(devs) {
    var tab = frs.ui.body().tab.tab_on;
    tab.l.find('.jstree').detach();
    select_devs_online();
    
    var data = frs.tree.dev(devs);
    var tree = $('<div></div>').jstree({core : {data : data.children}});
    tree.data = data;
    tab.l.append(tree);
    
    tree.on('select_node.jstree', function(e, data) {
        var node = tree.data.find_child_deep(data.node.text);
        var devs = $.map(node.leaves(), function(n) {return n.dev;});
        select_devs_online(devs);
    });
}

function select_devs_online(devs) {
    var tab = frs.ui.body().tab.tab_on;
    if (tab.r.player) tab.r.player.destory();
    tab.r.children().detach();
    
    if (!devs || !devs.length) return;
    
    var div_player = $('<div></div>');
    div_player.css('width', '100%');
    div_player.css('height', '100%');
    var id = 'player_' + new Date().getTime().toString();
    div_player.attr('id', id);
    
    tab.r.append([div_player]);
    
    if (frs.video.available()) {
        var player = new frs.video.Player(devs, id);
        tab.r.player = player;
        fomjar.util.async(function() {
            player.login(
                function() {},
                function() {alert('登陆失败');}
            );
            player.play();
        });
    }
}



})(jQuery)

