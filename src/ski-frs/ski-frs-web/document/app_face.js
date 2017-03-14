
(function($) {

fomjar.framework.phase.append('dom', frsmain);

function frsmain() {
    var tab = new frs.ui.Tab();
    tab.add_tab('上传', create_tab_upload(), true);
    tab.add_tab('检索', create_tab_search());
    frs.ui.body().append(tab);
}

function create_tab_upload() {
    var div = $('<div></div>');
    
    var input = $("<input type='file' accept='image/*' width : 4em'>");
    var button = new frs.ui.Button('搜索').to_major();
    var image = $('<img>');
    
    div.append([input, button, image]);

    input.bind('change', function(e) {
        var files = e.target.files || e.dataTransfer.files;
        if (!files || !files[0]) return;

        var file = files[0];
        var reader = new FileReader();
        reader.onload = function(e1) {
            image.attr('src', e1.target.result);
        };
        reader.readAsDataURL(file);
    });

    frs.ui.body().append(div);
    return div;
}

function create_tab_search() {
    return $('<div>zxcv</div>');
}

})(jQuery)

