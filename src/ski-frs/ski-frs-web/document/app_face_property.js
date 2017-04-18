
(function($) {

fomjar.framework.phase.append('dom', frsmain);

function frsmain() {
    build_head();
    build_body();
}

function build_head() {
    frs.ui.head().add_item('卡口管理', function() {window.location = 'app_face_outpost.html';});
    frs.ui.head().add_item('特征搜索', function() {window.location = 'app_face_property.html';}).addClass('active');
    frs.ui.head().add_item('身份确认', function() {window.location = 'app_face_id.html';});
    frs.ui.head().add_item('实时布控');
    frs.ui.head().add_item('轨迹管理', function() {window.location = 'app_face_trail.html';});
    frs.ui.head().add_item('人像库管理', function() {window.location = 'app_face_lib.html';});
    frs.ui.head().add_item('分析统计');
}

function build_body() {
    frs.ui.body().style_lr();
    build_body_l();
}

function build_body_l() {
    frs.ui.body().l.append([
        $('<label>地点</label>'), $('<div></div>').append(new frs.ui.Button('选择地点').to_minor()),
        $('<label>日期</label>'), $('<div></div>').append(new frs.ui.Button('选择日期').to_minor()),
        $('<label>性别</label>'), $("<select><option>全部</option><option>男</option><option>女</option></select>"),
        $('<label>年龄</label>'), $("<select><option>全部</option><option>0～50</option><option>51～100</option></select>"),
        $('<label>纹理</label>'), $("<select><option>全部</option><option>横条纹</option><option>竖条纹</option></select>"),
        $('<label>配饰</label>'), $("<select><option>全部</option><option>口罩</option><option>墨镜</option></select>"),
    ]);
    
}

})(jQuery)

