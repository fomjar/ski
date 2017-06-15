
(function($) {

fomjar.framework.phase.append('dom', frsmain);

function frsmain() {
    build_head();
    build_body();
}

function build_head() {
    frs.ui.head().style_default();
}

function build_body() {
    frs.ui.layout.lr(frs.ui.body());
    build_body_l();
}

var dids;
function build_body_l() {
    var choose;
    frs.ui.body().l.append([
        $('<label>设备</label>'), choose = new frs.ui.Button('选择设备', function() {
            dids = [];
            frs.ui.choose_devs(function(devs) {
                $.each(devs, function(i, dev) {dids.push(dev.did);});
                choose.text('已选择：' + devs.length + ' 项');
            });
        }).to_major(),
        $('<label>上衣颜色</label>'), $("<select><option value='-1'>不限</option><option value='1'>黑</option><option value='2'>灰</option><option value='4'>白</option><option value='8'>红</option><option value='16'>褐</option><option value='32'>橙</option><option value='64'>黄</option><option value='128'>绿</option><option value='256'>蓝</option><option value='512'>紫</option><option value='1024'>粉</option><option value='-2'>未识别</option></select>"),
        $('<label>裤子颜色</label>'), $("<select><option value='-1'>不限</option><option value='1'>黑</option><option value='2'>灰</option><option value='4'>白</option><option value='8'>红</option><option value='16'>褐</option><option value='32'>橙</option><option value='64'>黄</option><option value='128'>绿</option><option value='256'>蓝</option><option value='512'>紫</option><option value='1024'>粉</option><option value='-2'>未识别</option></select>"),
        $('<label>是否骑车</label>'), $("<select><option>不限</option><option>步行</option><option>骑自行车</option><option>骑电瓶车</option><option>骑摩托车</option></select>"),
        new frs.ui.Button('开始搜索', search).to_major()
    ]);
}

function collect() {
    var gender  = parseInt($($('select')[0]).val());
    var age     = parseInt($($('select')[1]).val());
    var hat     = parseInt($($('select')[2]).val());
    var glass   = parseInt($($('select')[3]).val());
    var mask    = parseInt($($('select')[4]).val());
    var color   = parseInt($($('select')[5]).val());
    var nation  = parseInt($($('select')[6]).val());
    
    var data = {};
    if (dids) data.dids = dids;
    if (-1 != gender)   data.gender = gender;
    if (-1 != age)      data.age = age;
    if (-1 != hat)      data.hat = hat;
    if (-1 != glass)    data.glass = glass;
    if (-1 != mask)     data.mask = mask;
    if (-1 != color)    data.color = color;
    if (-1 != nation)   data.nation = nation;
    return data;
}

var pl = 30;
var pk;

function search() {
    if (!dids) {
        new frs.ui.hud.Minor('一定要选择设备').appear(1500);
        return;
    }
    pk = new Date().getTime();
    search_page(1);
}

function search_page(page) {
    var data = collect();
    data.pk = pk;
    data.pf = (page - 1) * pl;
    data.pt = page * pl - 1;
    
    var mask = new frs.ui.Mask();
    var hud = new frs.ui.hud.Major('正在搜索');
    mask.appear();
    hud.appear();
    fomjar.net.send(ski.isis.INST_GET_PIC, data, function(code, desc) {
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
        $.each(desc, function(i, pic) {
            if (0 == i) return;
            
            r.append(new frs.ui.Block({
                cover   : pic.path,
                name    : pic.name,
                buttons : [
                    new frs.ui.Button('确认', function() {window.location = 'app_face_id.html?urlr=' + pic.path;}).to_major(),
                    new frs.ui.Button('轨迹', function() {window.location = 'app_face_trail.html?urlr=' + pic.path;}).to_major(),
                    new frs.ui.Button('布控').to_major(),
                ]
            }));
        });
        var pager2 = new frs.ui.Pager(page, p.pa, function(i) {search_page(i);});
        var div_pager2 = $('<div></div>');
        div_pager2.append(pager2);
        r.append(div_pager2);
    });
}

})(jQuery)

