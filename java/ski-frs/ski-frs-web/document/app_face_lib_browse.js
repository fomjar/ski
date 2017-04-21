
(function($) {

fomjar.framework.phase.append('dom', frsmain);
// fomjar.framework.phase.append('ren', update);

var slid = parseInt(fomjar.util.args.slid, 16);

function frsmain() {
    build_head();
    build_body();
}

function build_head() {
    frs.ui.head().style_default();
    frs.ui.head().append_item('卡口管理', function() {window.location = 'app_face_outpost.html';});
    frs.ui.head().append_item('特征搜索', function() {window.location = 'app_face_property.html';});
    frs.ui.head().append_item('身份确认', function() {window.location = 'app_face_id.html';});
    frs.ui.head().append_item('实时布控');
    frs.ui.head().append_item('轨迹管理', function() {window.location = 'app_face_trail.html';});
    frs.ui.head().append_item('人像库管理', function() {window.location = 'app_face_lib.html';}).addClass('active');
    frs.ui.head().append_item('分析统计');
}

function build_body() {
    var tool = $('<div></div>').append([
        new frs.ui.Button('添加人像', tool_add).to_minor(),
        $("<input placeholder='搜索'>"),
        $("<select><option value='人名'>人名</option><option value='电话'>电话</option><option value='地址'>地址</option><option value='身份证'>身份证</option></select>"),
    ]);
    var head = new Cell([
        $('<div>名称</div>'),
        $('<div>主体数量</div>'),
        $('<div>创建时间</div>'),
        $('<div>操作</div>'),
    ]);
    var list = new frs.ui.List();
    
    frs.ui.body().append([tool, $('<div></div>').append([head, list])]);
    
    tool.find('input').bind('keydown', function(e) {
        if (13 == e.keyCode) {
            var type = tool.find('select').val();
            var text = tool.find('input').val().trim();
            tool_search(type, text);
        }
    })
}

function Cell(array) {
    var cell = $('<div></div>');
    cell.addClass('cell');
    cell.append(array);
    return cell;
}

function update() {
    var mask = new frs.ui.Mask();
    var hud = frs.ui.hud.Major('正在获取');
    mask.appear();
    hud.appear();
    
    var list = frs.ui.body().find('.list');
    list.children().detach();
    fomjar.net.send(ski.isis.INST_QUERY_SUB_LIB, function(code, desc) {
        mask.disappear();
        hud.disappear();
        
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
                    new frs.ui.Button('编辑', function() {op_edit(l);}).to_major(),
                    new frs.ui.Button('浏览', function() {op_browse(l);}).to_major(),
                    new frs.ui.Button('导入', function() {op_import(l);}).to_major(),
                    new frs.ui.Button('删除', function() {op_delete(l);}).to_major(),
                ]),
            ]));
        });
    });
}

function tool_add() {
    var mask = new frs.ui.Mask();
    var dialog = new frs.ui.Dialog();
    mask.bind('click', function() {
        mask.disappear();
        dialog.disappear();
    });
    
    dialog.append_text_h1c('添加人像');
    dialog.append_space('.5em');
    dialog.append_input({placeholder : '姓名'});
    dialog.append_button(new frs.ui.Button('提交', function() {
        var name = dialog.find('input').val().trim();
        if (!name) {
            new frs.ui.hud.Minor('姓名不能为空').appear(1500);
            dialog.shake();
            return;
        }
    }).to_major());
    
    mask.appear();
    dialog.appear();
}

function tool_search(type, text) {
    new frs.ui.hud.Minor(type+':'+text).appear(1500);
}

function op_edit(sublib) {
    new frs.ui.hud.Minor('编辑:' + sublib.name).appear(1500);
}

function op_browse(sublib) {
    new frs.ui.hud.Minor('浏览:' + sublib.name).appear(1500);
}

function op_import(sublib) {
    new frs.ui.hud.Minor('导入:' + sublib.name).appear(1500);
}

function op_delete(sublib) {
    new frs.ui.hud.Minor('删除:' + sublib.name).appear(1500);
}

})(jQuery)

