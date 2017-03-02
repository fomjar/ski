
(function($) {

fomjar.framework.phase.append('dom', frsmain);

function frsmain() {
    frs.ui.head().css('opacity', '0');
    
    build_logo();
    build_dialog();
}

function build_logo() {
    var logo = $('<div></div>');
    logo.addClass('logo');
    logo.text('XXXXXX系统');
    frs.ui.body().append(logo);
}

function build_dialog() {
    var dialog = new frs.ui.Dialog();
    
    dialog.append_text_h1c('登陆');
    dialog.append_space('1em');
    dialog.append_input({placeholder : '用户名'});
    dialog.append_input({placeholder : '密码', type : 'password'});
    dialog.append_button(new frs.ui.Button('登陆', function() {
        var user = $(dialog.find('input')[0]).val();
        var pass = $(dialog.find('input')[1]).val();
        if (!user) {
            dialog.shake();
            new frs.ui.hud.Minor('用户名不能为空').appear(1500);
            return;
        }
        if (!pass) {
            dialog.shake();
            new frs.ui.hud.Minor('密码不能为空').appear(1500);
            return;
        }
        fomjar.net.send(ski.isis.INST_AUTHORIZE, {
            user    : user,
            pass    : pass
        }, function(code, desc) {
            if (code) {
                dialog.shake();
                new frs.ui.hud.Minor('0x' + code.toString(16) + ':' + desc).appear(2500);
            } else {
                window.location = '/index.html';
            }
        });
    }).to_high());
    
    dialog.appear();
}

})(jQuery)

