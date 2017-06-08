
(function($) {

fomjar.framework.phase.append('dom', frsmain);

function frsmain() {
    build_head();
    build_body();
}

function build_head() {
    frs.ui.head().style_default();
    frs.ui.head().append_item('卡口管理', function() {window.location = 'app_face_outpost.html';});
    frs.ui.head().append_item('特征搜索', function() {window.location = 'app_face_property.html';}).addClass('active');
    frs.ui.head().append_item('身份确认', function() {window.location = 'app_face_id.html';});
    frs.ui.head().append_item('实时布控');
    frs.ui.head().append_item('轨迹管理', function() {window.location = 'app_face_trail.html';});
    frs.ui.head().append_item('人像库管理', function() {window.location = 'app_face_sub.html';});
    frs.ui.head().append_item('分析统计');
}

function build_body() {
    frs.ui.layout.lr(frs.ui.body());
    build_body_l();
}

var sub = null;
function build_body_l() {
    var choose;
    frs.ui.body().l.append([
        $('<label>主体</label>'), choose = new frs.ui.Button('选择人像库', function() {
            frs.ui.choose_sub(function(s) {
                sub = s;
                choose.text('已选择：' + s.name);
            });
        }).to_major(),
        $('<label>姓名</label>'), $("<input placeholder='不限' type='text' >"),
        $('<label>性别</label>'), $("<select><option value='-1'>不限</option><option value='0'>女</option><option value='1'>男</option></select>"),
        $('<label>生日（YYYYMMDD）</label>'), $("<input placeholder='不限' type='text' >"),
        $('<label>身份证号</label>'), $("<input placeholder='不限' type='text' >"),
        $('<label>电话</label>'), $("<input placeholder='不限' type='number' >"),
        $('<label>地址</label>'), $("<input placeholder='不限' type='text' >"),
        new frs.ui.Button('开始搜索', search).to_major()
    ]);
}

function collect() {
    var name    = $($('input')[0]).val();
    var gender  = parseInt($('select').val());
    var birth   = $($('input')[1]).val();
    var idno    = $($('input')[2]).val();
    var phone   = $($('input')[3]).val();
    var addr    = $($('input')[4]).val();
    
    var data = {};
    if (sub)    data.sid    = sub.sid;
    if (name)   data.name   = name;
    if (-1 != gender) data.gender = gender;
    if (birth)  data.birth  = birth;
    if (idno)   data.idno   = idno;
    if (phone)  data.phone  = phone;
    if (addr)   data.addr   = addr;
    return data;
}

var pl = 30;
var pk;

function search() {
    pk = new Date().getTime();
    search_page(1);
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
            
            r.append(new frs.ui.BlockPicture({
                cover   : (item.pids.length > 0 ? item.pids[0].path : ''),
                name    : item.sname + '<br/>' + item.idno
            }));
        });
        var pager2 = new frs.ui.Pager(page, p.pa, function(i) {search_page(i);});
        var div_pager2 = $('<div></div>');
        div_pager2.append(pager2);
        r.append(div_pager2);
    });
}

})(jQuery)

