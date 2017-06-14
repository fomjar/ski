
(function($) {

fomjar.framework.phase.append('dom', frsmain);

function frsmain() {
    build_head();
    build_body();
}

function build_head() {
    frs.ui.head().style_default();
    frs.ui.head().append_item('卡口管理', function() {window.location = 'app_face_outpost.html';});
    frs.ui.head().append_item('特征搜索', function() {window.location = 'app_face_property.html';});
    frs.ui.head().append_item('身份确认', function() {window.location = 'app_face_id.html';});
    frs.ui.head().append_item('实时布控', function() {window.location = 'app_face_monitor.html';}).addClass('active');
    frs.ui.head().append_item('轨迹管理', function() {window.location = 'app_face_trail.html';});
    frs.ui.head().append_item('人像库管理', function() {window.location = 'app_face_sub.html';});
    frs.ui.head().append_item('分析统计');
}

function build_body() {
    var tab = new frs.ui.Tab();
    tab.addClass('tab-shadow tab-fix');
    tab.css('height', '100%');
    tab.add_tab('任务清单', tab.tab_browse = create_tab_browse(), true);
    tab.add_tab('创建任务', tab.tab_create = create_tab_create());
    frs.ui.body().append(frs.ui.body().tab = tab);
}

function create_tab_browse() {
    var div = frs.ui.layout.lr($("<div></div>"));
    div.addClass('tab-browse');
    div.on_appear = function() {
        fomjar.util.async(update_browse_mon, frs.ui.DELAY / 2);
    };
    return div;
}

function create_tab_create() {
    var div = frs.ui.layout.lr($("<div></div>"));
    div.addClass('tab-create');
    div.on_appear = function() {
        fomjar.util.async(update_create_mon, frs.ui.DELAY / 2);
    };
}

function update_browse_mon() {
    var tab = frs.ui.body().tab.tab_browse;
    tab.l.children().detach();
    var list_mon = new frs.ui.List();
    tab.l.append(list_mon);
    
    var mask = frs.ui.Mask();
    var hud = frs.ui.hud.Major('正在获取');
    mask.appear();
    hud.appear();
    fomjar.net.send(ski.isis.INST_GET_MON, function(code, desc) {
        mask.disappear();
        hud.disappear();
        if (code) {
            new frs.ui.hud.Minor(desc).appear(1500);
            return;
        }
        $.each(desc, function(i, mon) {
            var devs, subs;
            $.each(mon.devs, function(i, dev) {devs += dev.path + ',';});
            $.each(mon.subs, function(i, sub) {subs += sub.name + ',';});
            var cell_mon = new frs.ui.ListCellTable([
                mon.mid,
                devs,
                subs,
                (mon.tv * 100).toFixed(1) + '%',
                new Date(mon.time).format('yyyy/MM/dd HH:mm:ss'),
                mon.logs.length + ' 条记录',
            ]);
            cell_mon.bind('click', function() {
                tab.r.children().detach();
                var list_log = new frs.ui.List();
                tab.r.append(list_log);
                
                $.each(mon.logs, function(i, log) {
                    var cell_log = new frs.ui.ListCellTable([
                        log.lid,
                        $('<img>').attr('src', log.pic_dev.path),
                        $('<img>').attr('src', log.pic_sub.path),
                        (log.tv * 100).toFixed(1) + '%',
                        new Date(log.time).format('yyyy/MM/dd HH:mm:ss'),
                    ]);
                    list_log.append(cell);
                });
            });
            list_mon.append(cell_mon);
        });
    });
};

function update_create_mon() {
    var tab = frs.ui.body().tab.tab_create;
    tab.l.children().detach();
    tab.r.children().detach();
    
    var list_devs = new frs.ui.List();
    var list_subs = new frs.ui.List();
    tab.l.append([list_devs, list_subs]);
    
    var mask = frs.ui.Mask();
    var hud = frs.ui.hud.Major('正在获取');
    mask.appear();
    hud.appear();
    var done_dev = false;
    var done_sub = false;
    fomjar.net.send(ski.isis.INST_GET_DEV, function(code, desc) {
        done_dev = true;
        if (done_dev && done_sub) {
            mask.disappear();
            hud.disappear();
        }
        if (code) {
            new frs.ui.hud.Minor(desc).appear(1500);
            return;
        }
        $.each(desc, function(i, dev) {
            list_devs.append_cell({
                major   : dev.path,
                minor   : dev.pics + ' 张图片'
            })
        });
    });
    fomjar.net.send(ski.isis.INST_GET_SUB, function(code, desc) {
        done_sub = true;
        if (done_dev && done_sub) {
            mask.disappear();
            hud.disappear();
        }
        if (code) {
            new frs.ui.hud.Minor(desc).appear(1500);
            return;
        }
        $.each(desc, function(i, sub) {
            list_subs.append_cell({
                major   : sub.name,
                minor   : sub.items + ' 个人像'
            })
        });
    });
}

})(jQuery)

