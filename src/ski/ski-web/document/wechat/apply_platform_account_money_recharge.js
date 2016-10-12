
fomjar.framework.phase.append('dom', build_list);
fomjar.framework.phase.append('dom', build_buttons);
fomjar.framework.phase.append('ren', wx_config);

function build_list() {
    var before = $('<div></div>');
    before.addClass('tips');
    before.text('请选择充值金额');
    var after = $('<div></div>')
    after.addClass('tips');
    after.text('账户余额可以全额退款，请放心充值');
    var k = $('<div></div>');
    k.text('充值金额');
    var v = $('<select></select>');
    v.append("<option value='20'>20元</option>");
    v.append("<option value='50'>50元</option>");
    v.append("<option value='120' selected='selected'>120元</option>");

    var cell = $('<div></div>');
    cell.addClass('cell-kv');
    cell.append([k, v]);

    var list = $('<div></div>');
    list.addClass('list');
    list.append(cell);

    $('.wechat .frame .body').append([before, list, after]);
}

function build_buttons() {
    var button = $('<div></div>');
    button.addClass('button button-major button-large');
    button.text('充值');
    button.bind('click', apply);

    $('.wechat .frame .body').append(button);
}

function wx_config() {
    var appId;
    var timestamp;
    var nonceStr;
    var signature;
    // prepare
    fomjar.net.sendto(fomjar.net.api()+'/pay/recharge/prepare', {inst : fomjar.net.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY}, function(code, desc) {
        appId       = desc.appid;
        timestamp   = desc.timestamp;
        nonceStr    = desc.noncestr;
        signature   = desc.signature;
        wx.config({
            'debug'     : false,        // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
            'appId'     : appId,        // 必填，公众号的唯一标识
            'timestamp' : timestamp,    // 必填，生成签名的时间戳
            'nonceStr'  : nonceStr,     // 必填，生成签名的随机串
            'signature' : signature,    // 必填，签名，见附录1
            'jsApiList' : ['chooseWXPay']            // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
        });
    });
}

function apply() {
    // 生成预支付订单
    var money = $('select').val();
    wechat.show_toast('正在创建订单...');
    fomjar.net.sendto(fomjar.net.api()+'/pay/recharge/apply', {inst : fomjar.net.ISIS.INST_ECOM_APPLY_PLATFORM_ACCOUNT_MONEY}, function(code, desc) {
        ski.ui.hide_toast();
        if ('SUCCESS' == desc.return_code) {
            // 调起支付
            wx.chooseWXPay({
                'appId'     : desc.pay.appId,     // 必填，公众号的唯一标识
                'timestamp' : desc.pay.timeStamp, // 支付签名时间戳，注意微信jssdk中的所有使用timestamp字段均为小写。但最新版的支付后台生成签名使用的timeStamp字段名需大写其中的S字符
                'nonceStr'  : desc.pay.nonceStr,  // 支付签名随机串，不长于 32 位
                'package'   : desc.pay.package,   // 统一支付接口返回的prepay_id参数值，提交格式如：prepay_id=***）
                'signType'  : desc.pay.signType,  // 签名方式，默认为'SHA1'，使用新版支付需传入'MD5'
                'paySign'   : desc.pay.paySign,   // 支付签名
                'success'   : function (res) {
                    wechat.show_toast('充值成功');
                    setTimeout(function() {history.back(-1)}, 1000);
                }
            });
        } else {
            wechat.show_toast(desc.return_msg, 10000);
        }
    });
}

