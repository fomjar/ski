
(function($) {

fomjar.framework.phase.append('dom', frsmain);
fomjar.framework.phase.append('ren', update);

function frsmain() {
    build_head();
    build_body();
}

function build_head() {
    frs.ui.head().add_item('特征搜索', function() {window.location = 'app_face_property.html';});
    frs.ui.head().add_item('身份确认', function() {window.location = 'app_face_id.html';});
    frs.ui.head().add_item('实时布控');
    frs.ui.head().add_item('轨迹管理', function() {window.location = 'app_face_trail.html';});
    frs.ui.head().add_item('人员库管理', function() {window.location = 'app_face_lib.html';}).addClass('active');
    frs.ui.head().add_item('分析统计');
}

function build_body() {
    var tool = $('<div></div>').append([
        new frs.ui.Button('创建', create_lib).to_minor(),
    ]);
    var head = new Cell([
        $('<div>名称</div>'),
        $('<div>人数</div>'),
        $('<div>创建时间</div>'),
        $('<div>操作</div>'),
    ]);
    var list = new frs.ui.List();
    
    frs.ui.body().append([tool, $('<div></div>').append([head, list])]);
}

function Cell(array) {
    var cell = $('<div></div>');
    cell.addClass('cell');
    cell.append(array);
    return cell;
}

function update() {
    var list = frs.ui.body().find('.list');
    list.children().detach();
    fomjar.net.send(ski.isis.INST_QUERY_SUB_LIB, function(code, desc) {
        if (code) {
            new frs.ui.hud.Minor('获取清单失败: ' + desc).appear(1500);
            return;
        }
        $.each(desc, function(i, l) {
            list.append(new Cell([
                $('<div></div>').append(l.name),
                $('<div></div>').append(l.count),
                $('<div></div>').append(l.time.replace('.0', '')),
                $('<div></div>').append([
                    new frs.ui.Button('编辑').to_major(),
                    new frs.ui.Button('浏览').to_major(),
                    new frs.ui.Button('导入').to_major(),
                    new frs.ui.Button('删除').to_major(),
                ]),
            ]));
        });
    });
}

function create_lib() {
    var mask = new frs.ui.Mask();
    var dialog = new frs.ui.Dialog();
    dialog.css('width', '50%');
    mask.bind('click', function() {
        mask.disappear();
        dialog.disappear();
    });
    
    dialog.append_text_h1c('创建库');
    dialog.append_space('.5em');
    dialog.append_input({placeholder : '库名称'});
    dialog.append_button(new frs.ui.Button('提交', function() {
        var name = dialog.find('input').val().trim();
        if (!name) {
            new frs.ui.hud.Minor('库名称不能为空').appear(1500);
            dialog.shake();
            return;
        }
        
        fomjar.net.send(ski.isis.INST_UPDATE_SUB_LIB, {
            name : name,
            type : 0
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

})(jQuery)

