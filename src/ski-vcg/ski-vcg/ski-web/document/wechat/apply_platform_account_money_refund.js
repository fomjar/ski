(function($) {

fomjar.framework.phase.append('dom', build_list);
fomjar.framework.phase.append('dom', build_buttons);
fomjar.framework.phase.append('ren', setup);

function build_list() {
    var before = $('<div></div>');
    before.addClass('tips');
    before.text('只能退“现金”部分的金额，参见“小金库”');
    var k = $('<div></div>');
    k.text('退款金额');
    var v = $('<div></div>');
    v.attr('id', 'money');

    var cell = $('<div></div>');
    cell.addClass('cell-kv');
    cell.append([k, v]);

    var list = $('<div></div>');
    list.addClass('list');
    list.append(cell);

    $('.wechat .frame .body').append([before, list]);
}

function build_buttons() {
    var button = $('<div></div>');
    button.addClass('button button-major button-large');
    button.addClass('recharge-button-apply');
    button.text('退款');
    button.bind('click', apply);

    $('.wechat .frame .body').append(button);
}

function setup() {
    wechat.show_toast('正在加载...');
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_PLATFORM_ACCOUNT, {caid : fomjar.util.user()}, function(code, desc) {
        wechat.hide_toast();
        if (0 != code) {
            wechat.show_toast(desc, 10000);
            return;
        }
        $('#money').text(desc.cash_rt + ' 元');
    });

}

function apply() {
    wechat.show_toast('正在处理...');
    fomjar.net.sendto(fomjar.net.api()+'/pay/refund', {inst : fomjar.net.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY}, function(code, desc) {
        wechat.hide_toast();
        if (0 != code) {
            wechat.show_toast(desc, 10000);
            return;
        }
        wechat.show_toast('退款成功');
        setTimeout(function() {history.back(-1);}, 1000);
    });
}


})(jQuery)

