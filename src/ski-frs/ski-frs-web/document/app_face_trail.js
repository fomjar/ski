
(function($) {

fomjar.framework.phase.append('dom', frsmain);

function frsmain() {
    build_head();
    build_body();
}

function build_head() {
    frs.ui.head().add_item('特征搜索', function() {window.location = 'app_face_property.html';});
    frs.ui.head().add_item('身份确认', function() {window.location = 'app_face_id.html';});
    frs.ui.head().add_item('实时布控');
    frs.ui.head().add_item('轨迹管理', function() {window.location = 'app_face_trail.html';}).addClass('active');
    frs.ui.head().add_item('人员库管理');
    frs.ui.head().add_item('分析统计');
}

function build_body() {
    frs.ui.body().style_lr();
    
    build_body_l();
}

function build_body_l() {
    var image = $('<img>');
    var input = $("<input type='file' accept='image/*'>");
    var input_tv = $("<input type='number' placeholder='默认60'>");
    
    frs.ui.body().l.append([
        $('<label>上传</label>'),
        $('<div></div>').append([image, input]),
        $('<label>相似度(1~99)</label>'), input_tv,
    ]);

    input.bind('change', function(e) {
        var files = e.target.files || e.dataTransfer.files;
        if (!files || !files[0]) return;

        var file = files[0];
        var reader = new FileReader();
        reader.onload = function(e1) {
            image.attr('src', e1.target.result);
            func_upload_init(e1.target.result);
        };
        reader.readAsDataURL(file);
    });
    input_tv[0].max = 99;
    input_tv[0].min = 1;
    input_tv.bind('keydown', function(e) {
        if (e.keyCode == '13') {
            if (image.attr('src')) {
                func_upload_pages(1);
            }
        }
    });
}

function func_upload_init(img) {
    var mask = new frs.ui.Mask();
    var hud = new frs.ui.hud.Major('正在上传');
    mask.appear();
    hud.appear();
    fomjar.net.send(ski.isis.INST_QUERY_PIC_BY_FV_I, {
        pic : img,
    }, function(code, desc) {
        mask.disappear();
        hud.disappear();
        if (code) {
            new frs.ui.hud.Minor(desc).appear(1500);
        } else {
            func_upload_pages(1);
        }
    });
}

var page_len = 30;

function func_upload_pages(page) {
    var tv = 0.6;
    var input = frs.ui.body().l.find('input[type=number]');
    if (input.val()) {
        tv = parseFloat(input.val()) / 100;
    }
    var mask = new frs.ui.Mask();
    var hud = new frs.ui.hud.Major('正在匹配');
    mask.appear();
    hud.appear();
    fomjar.net.send(ski.isis.INST_QUERY_PIC_BY_FV, {
        tv  : tv,
        pf  : (page - 1) * page_len,
        pt  : page_len
    }, function(code, desc) {
        mask.disappear();
        hud.disappear();
        if (code) {
            new frs.ui.hud.Minor(desc).appear(1500);
        } else {
            var r = frs.ui.body().r;
            r.children().detach();
            var pager1 = new frs.ui.Pager(page, 9999, function(i) {func_upload_pages(i);});
            var div_pager1 = $('<div></div>');
            div_pager1.append(pager1);
            r.append(div_pager1);
            $.each(desc, function(i, pic) {
                r.append(new frs.ui.BlockPicture({
                    cover   : 'pic/' + pic.name,
                    name    : '相似度：' + (100 * pic.tv0).toFixed(1) + '%<br/>时间：' + pic.time.split('.')[0]
                }));
            });
            var pager2 = new frs.ui.Pager(page, 9999, function(i) {func_upload_pages(i);});
            var div_pager2 = $('<div></div>');
            div_pager2.append(pager2);
            r.append(div_pager2);
        }
    });
}

})(jQuery)

