
(function($) {

fomjar.framework.phase.append('dom', build_filter);
fomjar.framework.phase.append('dom', build_result);

function build_filter() {
    var div = $('<div></div>');

    var input = $("<input type='file' accept='image/*' width : 4em'>");
    var button = new frs.ui.Button('搜索');
    button.to_minor();
    var div_input = $('<div></div>');
    div_input.append([input, button]);
    var image = $('<img>');
    var div_image = $('<div></div>');
    div_image.append(image);
    div.append([div_input, div_image]);

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
}

function build_result() {
}

})(jQuery)

