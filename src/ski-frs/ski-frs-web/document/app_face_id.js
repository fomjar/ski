
(function($) {

fomjar.framework.phase.append('dom', frsmain);

function frsmain() {
    build_head();
    build_body();
}

function build_head() {
    frs.ui.head().add_item('卡口管理', function() {window.location = 'app_face_outpost.html';});
    frs.ui.head().add_item('特征搜索', function() {window.location = 'app_face_property.html';});
    frs.ui.head().add_item('身份确认', function() {window.location = 'app_face_id.html';}).addClass('active');
    frs.ui.head().add_item('实时布控');
    frs.ui.head().add_item('轨迹管理', function() {window.location = 'app_face_trail.html';});
    frs.ui.head().add_item('人像库管理', function() {window.location = 'app_face_lib.html';});
    frs.ui.head().add_item('分析统计');
}

function build_body() {
    frs.ui.layout.lr(frs.ui.body());
    build_body_l();
}

function build_body_l() {
    var image = $('<img>');
    var input = $("<input type='file' accept='image/*'>");
    var input_tv = $("<input type='number' placeholder='默认60'>");
    
    frs.ui.body().l.append([
        $('<label>信息库</label>'), $('<select><option>全部</option><option>杀人犯</option><option>放火犯</option></select>'),
        $('<label>上传</label>'), $('<div></div>').append([image, input]),
        $('<label>相似度(1~99)</label>'), input_tv,
    ]);

}

})(jQuery)

