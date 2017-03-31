
(function($) {

fomjar.framework.phase.append('dom', frsmain);

function frsmain() {
    var tab = new frs.ui.Tab();
    tab.add_tab('上传', create_tab_upload(), true);
    tab.add_tab('检索', create_tab_search());
    frs.ui.body().append(tab);
    
    var rst = $('<div></div>');
    rst.addClass('rst');
    frs.ui.body().append(rst);
}

function create_tab_upload() {
    var div = $('<div></div>');
    div.addClass('tab_upload');
    
    var image = $('<img>');
    var input = $("<input type='file' accept='image/*'>");
    
    div.append([image, input]);

    input.bind('change', function(e) {
        var files = e.target.files || e.dataTransfer.files;
        if (!files || !files[0]) return;

        var file = files[0];
        var reader = new FileReader();
        reader.onload = function(e1) {
            image.attr('src', e1.target.result);
            func_upload(e1.target.result);
        };
        reader.readAsDataURL(file);
    });

    frs.ui.body().append(div);
    return div;
}

function func_upload(img) {
    func_upload_pages(img, 1);
}

function func_upload_pages(img, page) {
    var mask = new frs.ui.Mask();
    var hud = new frs.ui.hud.Major('正在获取');
    mask.appear();
    hud.appear();
    fomjar.net.send(ski.isis.INST_QUERY_PIC_BY_FV, {
        pic : img,
        tv  : 0.3,
        pf  : (page - 1) * 20,
        pt  : 20
    }, function(code, desc) {
        mask.disappear();
        hud.disappear();
        if (code) {
            new frs.ui.hud.Minor(desc).appear(1500);
        } else {
            var rst = $('.frs .body .rst');
            rst.children().detach();
            var pager1 = new frs.ui.Pager(page, 9999, function(i) {func_upload_pages(img, i);});
            var div_pager1 = $('<div></div>');
            div_pager1.append(pager1);
            rst.append(div_pager1);
            $.each(desc, function(i, pic) {
                rst.append(new frs.ui.BlockPicture({
                    cover   : 'pic/' + pic.name,
                    name    : '相似度：' + (100 * pic.tv).toFixed(1) + '%<br/>时间：' + pic.time.split('.')[0]
                }));
            });
            var pager2 = new frs.ui.Pager(page, 9999, function(i) {func_upload_pages(img, i);});
            var div_pager2 = $('<div></div>');
            div_pager2.append(pager2);
            rst.append(div_pager2);
        }
    });
}

function create_tab_search() {
    return $('<div>zxcv</div>');
}

})(jQuery)

