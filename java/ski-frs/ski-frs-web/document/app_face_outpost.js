
(function($) {

fomjar.framework.phase.append('dom', frsmain);

function frsmain() {
    build_head();
    build_body();
}

function build_head() {
    frs.ui.head().style_default();
    frs.ui.head().append_item('卡口管理', function() {window.location = 'app_face_outpost.html';}).addClass('active');
    frs.ui.head().append_item('特征搜索', function() {window.location = 'app_face_property.html';});
    frs.ui.head().append_item('身份确认', function() {window.location = 'app_face_id.html';});
    frs.ui.head().append_item('实时布控');
    frs.ui.head().append_item('轨迹管理', function() {window.location = 'app_face_trail.html';});
    frs.ui.head().append_item('人像库管理', function() {window.location = 'app_face_lib.html';});
    frs.ui.head().append_item('分析统计');
}

function build_body() {
    var tab = new frs.ui.Tab();
    tab.addClass('tab-shadow tab-fix');
    tab.css('height', '100%');
    tab.add_tab('在线卡口', tab.tab_on = create_tab_online());
    tab.add_tab('离线卡口', tab.tab_of = create_tab_offline(), true);
    frs.ui.body().append(frs.ui.body().tab = tab);
}

function create_tab_online() {
    var div = frs.ui.layout.lrb($('<div></div>'));
    var tree = $('<div></div>').jstree({core : {
        data : [
            {text : 'test1test1test1'},
            {text : 'test2test2test2'},
            {text : 'test3test3test3'},
            {text : 'test4test4test4',
                children : [
                    {text : 'asdfasdfasdf'},
                    {text : 'zxcvzxcvzxcv'}
                ]
            }
        ]
    }});
    div.l.append(tree);
    return div;
}

function create_tab_offline() {
    var div = $('<div></div>');
    div.addClass('tab-of');
    div.append([
        $('<div></div>').append([
            $("<select><option>OPP-1</option><option>OPP-2</option><option>OPP-3</option></select>"),
            $("<input type='text' placeholder='输入服务器中视频文件的本地路径'>"),
            new frs.ui.Button('开始分析', function() {
                div.find('>div').css('top', '0');
                fomjar.util.async(function() {div.find('>div').css('top', '');}, 1000);
            }).to_major()
        ]),
    ]);
    div.on_appear = function() {
        fomjar.util.async(function() {div.find('input').focus();}, frs.ui.DELAY / 2);
    };
    return div;
}

})(jQuery)

