
(function($) {

fomjar.framework.phase.append('dom', frsmain);

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
    frs.ui.head().append_item('人像库管理', function() {window.location = 'app_face_sub.html';});
    frs.ui.head().append_item('分析统计');
}

function build_body() {
    var tab = new frs.ui.Tab();
    tab.addClass('tab-shadow tab-fix');
    tab.css('height', '100%');
    tab.add_tab('卡口预览', tab.tab_browse = create_tab_browse(), true);
    tab.add_tab('文件导入', tab.tab_import = create_tab_import());
    frs.ui.body().append(frs.ui.body().tab = tab);
}

function create_tab_browse() {
    var div = frs.ui.layout.lrb($('<div></div>'));
    div.addClass('tab-browse');
    div.on_appear = function() {
        fomjar.util.async(update_dev, frs.ui.DELAY / 2);
    }
    return div;
}

function create_tab_import() {
    var div = frs.ui.layout.lr($('<div></div>'));
    div.addClass('tab-import');
    div.l.append([
        $('<label>选择服务器</label>'), $('<select></select>'),
        $('<label>文件真实路径</label>'), $("<input type='text' placeholder='视频文件的本地路径'>"),
        $('<label>文件虚拟路径</label>'), $("<input type='text' placeholder='设备虚拟显示路径'>"),
        new frs.ui.Button('开始分析', function() {
            var mask = new frs.ui.Mask();
            mask.appear();
            var hud = frs.ui.hud.Major('正在创建离线设备');
            hud.appear();
            var opp = div.l.find('select').val();
            var path_real = div.l.find('input:nth-child(4)').val();
            var path_view = div.l.find('input:nth-child(6)').val();
            fomjar.net.send(ski.isis.INST_SET_DEV, {
                path : path_view
            }, function(code, desc) {
                mask.disappear();
                hud.disappear();
                if (code) {
                    new frs.ui.hud.Minor(desc).appear(1500);
                    return;
                }
                
                var did = desc.did;
                mask.appear();
                hud.text('正在提交分析');
                hud.appear();
                fomjar.net.send(ski.isis.INST_APPLY_DEV_IMPORT, {
                    opp     : opp,
                    did     : did,
                    path    : path_real
                }, function(code, desc) {
                    mask.disappear();
                    hud.disappear();
                    if (code) {
                        new frs.ui.hud.Minor(desc).appear(1500);
                        return;
                    }
                    
                    new frs.ui.hud.Minor('已经在分析了').appear(1500);
                });
            });
        }).to_major(),  // submit
    ]);
    div.on_appear = function() {
        fomjar.util.async(function() {
            var mask = new frs.ui.Mask();
            var hud = frs.ui.hud.Major('正在获取');
            mask.appear();
            hud.appear();
            fomjar.net.send(ski.isis.INST_GET_OPP, function(code, desc) {
                mask.disappear();
                hud.disappear();
                if (code) {
                    new frs.ui.hud.Minor(desc).appear(1500);
                    return;
                }
                var select = div.l.find('select');
                select.children().detach();
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
    fomjar.net.send(ski.isis.INST_GET_DEV, function(code, desc) {
        mask.disappear();
        hud.disappear();
        
        if (code) {
            new frs.ui.hud.Minor(desc).appear(1500);
            return;
        }
    
        update_dev_browse(desc);
    });
}

function update_dev_browse(devs) {
    var tab = frs.ui.body().tab.tab_browse;
    tab.l.find('.jstree').detach();
    select_devs_browse();
    
    var data = frs.tree.dev(devs);
    var tree = $('<div></div>').jstree({core : {data : data.children}});
    tree.data = data;
    tab.l.append(tree);
    
    tree.on('select_node.jstree', function(e, data) {
        var node = tree.data.find_child_deep(data.node.text);
        var devs = $.map(node.leaves(), function(n) {return n.dev;});
        select_devs_browse(devs);
    });
}

function select_devs_browse(devs) {
    var tab = frs.ui.body().tab.tab_browse;
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

