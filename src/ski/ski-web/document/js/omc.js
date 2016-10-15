
var omc = {
    channel : function(i) {
        switch (i) {
        case 0:     return '淘宝';
        case 1:     return '微信';
        case 2:     return '支付宝';
        case 3:     return 'PSN';
        default:    return '未知';
        }
    }
};

(function($) {

fomjar.framework.phase.append('dom', build_frame);
fomjar.framework.phase.append('dom', build_frame_head);
fomjar.framework.phase.append('dom', build_frame_body);

function build_frame() {
    var omc = $('<div></div>');
    omc.addClass('omc');

    var frame = $('<div></div>');
    frame.addClass('frame');

    omc.append(frame);

    $('body').append(omc);
}

function build_frame_head() {
    var head = $('<div></div>');
    head.addClass('head');

    var menu = $('<div></div>');
    menu.addClass('menu');
    var m_game     = menu.clone();
    var m_account  = menu.clone();
    var m_ticket   = menu.clone();
    var m_report   = menu.clone();
    m_game.text('游戏');
    m_account.text('帐号');
    m_ticket.text('工单');
    m_report.text('统计');

    head.append(m_game);
    head.append(m_account);
    head.append(m_ticket);
    head.append(m_report);

    $('.omc .frame').append(head);
}

function build_frame_body() {
    var body = $('<div></div>');
    body.addClass('body');

    $('.omc .frame').append(body);
}


})(jQuery)

