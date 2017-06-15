
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

var sel_mon = null;

function create_tab_browse() {
    var div = frs.ui.layout.lr($("<div></div>"));
    div.addClass('tab-browse');
    var bar = $('<div></div>');
    bar.addClass('bar');
    bar.append(new frs.ui.Button('删除此任务', function() {
        if (!sel_mon) {
            new frs.ui.hud.Minor('请选择一个任务').appear(1500);
            return;
        }
        fomjar.net.send(ski.isis.INST_DEL_MON, {mid : sel_mon.mid}, function(code, desc) {
            if (code) {
                new frs.ui.hud.Minor(desc).appear(1500);
                return;
            }
            new frs.ui.hud.Minor('删除成功').appear(1500);
            update_browse_mon();
        });
    }).to_major());
    div.append(bar);
    div.on_appear = function() {
        fomjar.util.async(update_browse_mon, frs.ui.DELAY / 2);
    };
    return div;
}

var sel_devs = {};
var sel_subs = {};

function create_tab_create() {
    var div = frs.ui.layout.lr($("<div></div>"));
    div.addClass('tab-create');
    var bar = $('<div></div>');
    bar.addClass('bar');
    bar.append(new frs.ui.Button('创建布控任务', function() {
        var dids = [];
        var sids = [];
        for (var i in sel_devs) dids.push(sel_devs[i].did);
        for (var i in sel_subs) sids.push(sel_subs[i].sid);
        if (!dids.length) {
            new frs.ui.hud.Minor('必须选择一个设备').appear(1500);
            return;
        }
        if (!sids.length) {
            new frs.ui.hud.Minor('必须选择一个人像库').appear(1500);
            return;
        }
        var mask = new frs.ui.Mask();
        var dialog = new frs.ui.Dialog();
        
        mask.bind('click', function() {
            mask.disappear();
            dialog.disappear();
        });
        
        dialog.css('width', '50%');
        dialog.append_text_h1c('创建布控任务');
        dialog.append_space('.5em');
        var input1 = dialog.append_input({'placeholder' : '任务名'});
        var input2 = dialog.append_input({'placeholder' : '阈值（1～99），默认70', 'type' : 'number'});
        input2[0].min = 1;
        input2[0].max = 99;
        dialog.append_button(new frs.ui.Button('确定', function() {
            if (!input1.val()) {
                new frs.ui.hud.Minor('必须输入任务名').appear(1500);
                dialog.shake();
                return;
            }
            var data = {};
            data.name = input1.val();
            if (input2.val()) data.tv = input2.val() / 100;
            else data.tv = 0.7;
            data.dids = dids;
            data.sids = sids;
            var mask1 = new frs.ui.Mask();
            var hud = new frs.ui.hud.Major('正在提交');
            mask1.appear();
            hud.appear();
            fomjar.net.send(ski.isis.INST_SET_MON, data, function(code, desc) {
                mask1.disappear();
                hud.disappear();
                if (code) {
                    new frs.ui.hud.Minor(desc).appear(1500);
                    dialog.shake();
                    return;
                }
                new frs.ui.hud.Minor('创建成功').appear(1500);
                mask.disappear();
                dialog.disappear();
            });
        }).to_major());
        
        mask.appear();
        dialog.appear();
    }).to_major());
    div.append(bar);
    div.on_appear = function() {
        fomjar.util.async(update_create_mon, frs.ui.DELAY / 2);
    };
    return div;
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
            var cell_mon = list_mon.append_cell({
                major   : mon.name,
                minor   : new Date(mon.time).format('yyyy/MM/dd') + ' ' + mon.logs.length + ' 条记录'
            });
            cell_mon.bind('click', function() {
                list_mon.find('>div').removeClass('active');
                cell_mon.addClass('active');
                sel_mon = mon;
                
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
        });
    });
};

function update_create_mon() {
    var tab = frs.ui.body().tab.tab_create;
    tab.l.children().detach();
    tab.r.children().detach();
    
    var list_devs = new frs.ui.List();
    var list_subs = new frs.ui.List();
    tab.l.append(list_devs);
    tab.r.append(list_subs);
    
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
            var cell = list_devs.append_cell({
                major   : dev.path,
                minor   : dev.pics + ' 张图片'
            });
            cell.is_select = false;
            cell.bind('click', function() {
                cell.is_select = !cell.is_select;
                if (cell.is_select) {
                    cell.addClass('active');
                    sel_devs[dev.did] = dev;
                } else {
                    cell.removeClass('active');
                    delete sel_devs[dev.did];
                }
            });
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
            var cell = list_subs.append_cell({
                major   : sub.name,
                minor   : sub.items + ' 个人像'
            });
            cell.is_select = false;
            cell.bind('click', function() {
                cell.is_select = !cell.is_select;
                if (cell.is_select) {
                    cell.addClass('active');
                    sel_subs[sub.sid] = sub;
                } else {
                    cell.removeClass('active');
                    delete sel_subs[sub.sid];
                }
            });
        });
    });
}

})(jQuery)

