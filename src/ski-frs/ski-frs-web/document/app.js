
(function($) {

fomjar.framework.phase.append('dom', build_app);

function App(name, color, url) {
    var div = $('<div></div>');
    div.addClass('app');
    div.text(name);
    div.css('background', color);
    if (url) div.bind('click', function() {window.location = url;});
    return div;
}

function build_app() {
    var dialog = new frs.ui.Dialog();
    
    dialog.append_text_h1('应用');
    
    dialog.append(new App('人脸', '#ff9999', 'app_face_trail.html'));
    dialog.append(new App('车辆', '#99ff99'));
    dialog.append(new App('高级', '#9999ff'));
    
    var other = $('<div></div>');
    other.addClass('others');
    other.append(new frs.ui.Button('库管理 >', function() {window.location = 'manage.html';}));
    dialog.append(other);
    
    dialog.appear();
}

})(jQuery)

