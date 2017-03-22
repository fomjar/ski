
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
    var mask = new frs.ui.Mask();
    mask.appear();
    fomjar.util.async(function() {mask.disappear();}, 3000);
    new frs.ui.hud.Major('正在搜索...').appear(3000);
}

function create_tab_search() {
    return $('<div>zxcv</div>');
}

})(jQuery)

