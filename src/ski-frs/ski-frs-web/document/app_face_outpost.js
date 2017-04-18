
(function($) {

fomjar.framework.phase.append('dom', frsmain);

function frsmain() {
    build_head();
    build_body();
}

function build_head() {
    frs.ui.head().add_item('卡口管理', function() {window.location = 'app_face_outpost.html';}).addClass('active');
    frs.ui.head().add_item('特征搜索', function() {window.location = 'app_face_property.html';});
    frs.ui.head().add_item('身份确认', function() {window.location = 'app_face_id.html';});
    frs.ui.head().add_item('实时布控');
    frs.ui.head().add_item('轨迹管理', function() {window.location = 'app_face_trail.html';});
    frs.ui.head().add_item('人像库管理', function() {window.location = 'app_face_lib.html';});
    frs.ui.head().add_item('分析统计');
}

function build_body() {
    var tab = new frs.ui.Tab();
    tab.add_tab('在线卡口', $('<div></div>'));
    tab.add_tab('离线卡口', $('<div></div>'), true);
    frs.ui.body().append(tab);
}

})(jQuery)

