
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

fomjar.framework.phase.append('dom', build_frame);
fomjar.framework.phase.append('dom', build_frame_navigator);
fomjar.framework.phase.append('dom', build_frame_content);

function build_frame() {
    var frame = $('<div></div>');
    frame.addClass('omc-frame');

    $('body').append(frame);
}

function build_frame_navigator() {
    var navigator = $('<div></div>');
    navigator.addClass('omc-frame-navigator');

    var menu = $('<div></div>');
    menu.addClass('omc-frame-navigator-menu');

    var m_game     = menu.clone();
    var m_account  = menu.clone();
    var m_ticket   = menu.clone();
    var m_report   = menu.clone();
    m_game.text('游戏');
    m_account.text('帐号');
    m_ticket.text('工单');
    m_report.text('统计');

    navigator.append(m_game);
    navigator.append(m_account);
    navigator.append(m_ticket);
    navigator.append(m_report);

    $('.omc-frame').append(navigator);
}

function build_frame_content() {
    var content = $('<div></div>');
    content.addClass('omc-frame-content');

    $('.omc-frame').append(content);
}

