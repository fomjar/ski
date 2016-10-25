(function($) {

fomjar.framework.phase.append('dom', build);
fomjar.framework.phase.append('ren', setup);

function build() {
    var icon = $('<div></div>');
    icon.addClass('money-icon');
    icon.append("<img src='res/money.png' />");

    var money = $('<div></div>');
    money.addClass('money-value');

    var tips = $('<div></div>');
    tips.addClass('tips');
    tips.text('友情提醒：退款要归还所有在体验游戏才行哦！');

    var button_recharge = $('<div></div>');
    button_recharge.addClass('button button-major button-large');
    button_recharge.text('充值');
    button_recharge.bind('touchend', function() {window.location = 'apply_platform_account_money_recharge.html';});
    var button_refund = $('<div></div>');
    button_refund.addClass('button button-minor button-large');
    button_refund.text('退款');
    button_refund.bind('touchend', function() {window.location = 'apply_platform_account_money_refund.html';});

    var table = $('<table></table>');
    var tr = $('<tr></tr>');
    var td_rc = $('<td></td>');
    var td_rf = $('<td></td>');
    td_rc.append(button_recharge);
    td_rf.append(button_refund);
    tr.append([td_rc, td_rf]);
    table.append(tr);

    $('.vcg .frame .body').append([icon, money, tips, table]);
}

function setup() {
    vcg.show_toast('正在获取...');
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT, {caid : fomjar.util.user()}, function(code, desc) {
        vcg.hide_toast();
        if (0 != code) {
            vcg.show_toast('获取失败', 10000);
            return;
        }

        $('.money-value').text('¥ ' + desc.cash_rt);
    });
}


})(jQuery)

