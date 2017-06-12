
(function($) {

fomjar.framework.phase.append('dom', frsmain);

var urll = fomjar.util.args.urll;
var urlr = fomjar.util.args.urlr;

function frsmain() {
    build_head();
    build_body();
}

function build_head() {
    frs.ui.head().style_default();
    frs.ui.head().append_item('卡口管理', function() {window.location = 'app_face_outpost.html';});
    frs.ui.head().append_item('特征搜索', function() {window.location = 'app_face_property.html';});
    frs.ui.head().append_item('身份确认', function() {window.location = 'app_face_id.html';}).addClass('active');
    frs.ui.head().append_item('实时布控', function() {window.location = 'app_face_real.html';});
    frs.ui.head().append_item('轨迹管理', function() {window.location = 'app_face_trail.html';});
    frs.ui.head().append_item('人像库管理', function() {window.location = 'app_face_sub.html';});
    frs.ui.head().append_item('分析统计');
}

function build_body() {
    frs.ui.layout.lr(frs.ui.body());
    build_body_l();
}

var sub;
function build_body_l() {
    var image = $('<img>');
    var input = $("<input type='file' accept='image/*'>");
    var input_tv = $("<input placeholder='默认70' type='number'>");
    var choose;
    frs.ui.body().l.append([
        $('<label>上传</label>'), $('<div></div>').append([image, input]),
        $('<label>主体</label>'), choose = new frs.ui.Button('选择人像库', function() {
            frs.ui.choose_sub(function(s) {
                sub = s;
                choose.text('已选择：' + s.name);
            });
        }).to_major(),
        $('<label>相似度(1~99)</label>'),  input_tv,
        $('<label>性别</label>'),    $("<select><option value='-1'>不限</option><option value='0'>女</option><option value='1'>男</option></select>"),
        $('<label>出生年份</label>'), $("<input placeholder='起始年份' type='text' >"), $("<input placeholder='截止年份' type='text' >"),
        new frs.ui.Button('开始确认', search).to_major()
    ]);
    input.bind('change', function(e) {
        var files = e.target.files || e.dataTransfer.files;
        if (!files || !files[0]) return;

        var file = files[0];
        fomjar.graphics.image_base64_local(file, function(base64) {image.attr('src', base64);});
    });
    input_tv[0].max = 99;
    input_tv[0].min = 1;
    
    if (urll) input.val(urll);
    if (urlr) {
        fomjar.graphics.image_base64_remote(urlr, function(base64) {image.attr('src', base64);});
    }
}

function collect() {
    var min         = 0.7
    var input       = frs.ui.body().l.find('input[type=number]');
    if (input.val()) min = parseFloat(input.val()) / 100;
    var gender      = parseInt($($('select')[0]).val());
    var birth_min   = parseInt($($('input')[2]).val());
    var birth_max   = parseInt($($('input')[3]).val());
    
    var data = {};
    if (fv) {
        data.fv = fv;
        data.min = min;
        data.max = 1.0;
    } else data.data = frs.ui.body().l.find('img').attr('src');
    if (sub) data.sid = sub.sid;
    if (-1 != gender) data.gender       = gender;
    if (birth_min)    data.birth_min    = birth_min;
    if (birth_max)    data.birth_max    = birth_max;
    return data;
}

var fv;
var pl = 30;
var pk;

function search() {
    if (!frs.ui.body().l.find('img').attr('src')) {
        new frs.ui.hud.Minor('必须要选择一张图片').appear();
        return;
    }
    var mask = new frs.ui.Mask();
    var hud = new frs.ui.hud.Major('正在上传');
    mask.appear();
    hud.appear();
    fomjar.net.send(ski.isis.INST_GET_PIC_FV, {
        data : frs.ui.body().l.find('img').attr('src'),
    }, function(code, desc) {
        mask.disappear();
        hud.disappear();
        if (code) {
            new frs.ui.hud.Minor(desc).appear(1500);
        } else {
            fv = desc.fv;
            pk = new Date().getTime();
            search_page(1);
        }
    });
}

function search_page(page) {
    var data = collect();
    data.pk = pk;
    data.pf = (page - 1) * pl;
    data.pt = page * pl - 1;
    
    var mask = new frs.ui.Mask();
    var hud = new frs.ui.hud.Major('正在获取');
    mask.appear();
    hud.appear();
    fomjar.net.send(ski.isis.INST_GET_SUB_ITEM, data, function(code, desc) {
        mask.disappear();
        hud.disappear();
        if (code) {
            new frs.ui.hud.Minor(desc).appear(1500);
            return;
        }
        
        var p = desc[0];
        var r = frs.ui.body().r;
        r.children().detach();
        var pager1 = new frs.ui.Pager(page, p.pa, function(i) {search_page(i);});
        var div_pager1 = $('<div></div>');
        div_pager1.append(pager1);
        r.append(div_pager1);
        $.each(desc, function(i, item) {
            if (0 == i) return;
            
            r.append(new frs.ui.Block({
                cover   : (item.pics.length > 0 ? item.pics[0].path : ''),
                name    : '相似度：' + (100 * item.tv).toFixed(1) + '%<br/>人像库：' + item.sname
            }));
        });
        var pager2 = new frs.ui.Pager(page, p.pa, function(i) {search_page(i);});
        var div_pager2 = $('<div></div>');
        div_pager2.append(pager2);
        r.append(div_pager2);
    });
}


})(jQuery)

