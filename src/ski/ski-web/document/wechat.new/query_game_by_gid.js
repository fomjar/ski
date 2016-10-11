
fomjar.framework.phase.append('dom', build_head);
fomjar.framework.phase.append('dom', build_body);
fomjar.framework.phase.append('dom', build_post);
fomjar.framework.phase.append('dom', build_intr);
fomjar.framework.phase.append('dom', build_ccs);
fomjar.framework.phase.append('dom', build_rent);

fomjar.framework.phase.append('ren', setup);

function build_head() {
    var head = $('<div></div>');
    head.addClass('game-head');
    head.append("<img id='game_background'/>");
    head.append("<img id='game_cover'/>");
    head.append("<div id='game_name_zh'></div>");
    head.append("<div id='game_name_en'></div>");
    head.append("<div id='game_category'></div>");
    head.append("<div id='game_language'></div>");
    head.append("<div id='game_sale'></div>");

    $('.wechat-frame-content').append(head);
}

function build_body() {
    var body = $('<div></div>');
    body.addClass('game-body');

    $('.wechat-frame-content').append(body);
}

function build_post() {
    var poster = $('<div></div>');
    poster.addClass('game-body-poster');

    $('.game-body').append(poster);
}

function build_intr() {
    var introduction = $('<div></div>');
    introduction.addClass('game-body-introduction');

    $('.game-body').append(introduction);
}

function build_ccs() {
    var ccs = $('<div></div>');
    ccs.addClass('game-body-ccs');

    var category = $('<div></div>');
    category.addClass('game-body-category');
    category.text('适合您的光盘出售');

    var tips = $('<div></div>');
    tips.addClass('game-body-tips');
    
    var box = $('<div><table><tr></tr></table></div>');
    box.addClass('game-body-ccs-box');

    ccs.append([category, tips, box]);

    $('.game-body').append(ccs);
}

function build_rent() {}

function setup() {
    var gid = fomjar.util.args().gid;
    wechat.show_toast('正在获取...');
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_GAME, {gid : gid}, function(code, desc) {
        wechat.hide_toast();
        if (0 != code) {
            wechat.show_toast(desc, 10000);
            return;
        }

        var game = desc;

        // title
        fomjar.util.title(game.name_zh_cn);

        // head
        $('#game_background').css('background-image', 'url('+game.url_cover+')');
        $('#game_cover').attr('src', game.url_cover);
        $('#game_name_zh').text(game.name_zh_cn);
        $('#game_name_en').text(game.name_en);
        $('#game_category').text(game.category);
        $('#game_language').text(game.language);
        $('#game_sale').text(game.sale);
        
        // body
        // vedio
        if (0 < game.vedio.length) {
            $('.game-body-poster').append("<video width='100%' height='100%' controls autobuffer src='" + game.vedio + "'/>");
        }
        // poster
        $.each(game.url_poster, function(i, p) {
            if (0 < p.length)
                $('.game-body-poster').append("<div style='background-image: url(" + p + ")'/>");
        });
        // skippr
        if (0 == $('.game-body-poster>*').length) {
            $('.game-body-poster').hide();
        } else {
            $('.game-body-poster').skippr({
                transition          : 'fade',
                speed               : 200,
                easing              : 'easeOutQuart',
                navType             : 'bubble',
                childrenElementType : 'div',
                arrows              : true,
                autoPlay            : false,
                autoPlayDuration    : 5000,
                keyboardOnAlways    : true,
                hidePrevious        : false
            });
        }
        // introduction
        $('.game-body-introduction').append('<p>'+game.introduction.replace(/\|/g, '</p><p>') + '</p>');
        // css
        if (0 == game.ccs.total) {
            $('.game-body-ccs').hide();
        } else {
            $('.game-body-ccs .game-body-tips').text('我们在共 '+game.ccs.total+' 款商品中为您挑选了以下几款商品');

            $.each(game.ccs.sold, function(i, cc) {
                var td = $('<td></td>');
                var box_cc = $('<div></div>');
                box_cc.addClass('game-body-cc');
                box_cc.append("<img src='"+cc.item_cover+"' />");
                box_cc.append('<div>🔥销量最高</div>');
                box_cc.append('<div>到手价：¥ '+(Number(cc.item_price.split(' - ')[0])+Number(cc.express_price))+'</div>');
                box_cc.bind('click', function() {on_cc_click(cc);});
                td.append(box_cc);
                $('.game-body-ccs-box tr').append(td);
            });
            $.each(game.ccs.conv, function(i, cc) {
                var td = $('<td></td>');
                var box_cc = $('<div></div>');
                box_cc.addClass('game-body-cc');
                box_cc.append("<img src='"+cc.item_cover+"' />");
                box_cc.append('<div>💰价格最低</div>');
                box_cc.append('<div>到手价：¥ '+(Number(cc.item_price.split(' - ')[0])+Number(cc.express_price))+'</div>');
                box_cc.bind('click', function() {on_cc_click(cc);});
                td.append(box_cc);
                $('.game-body-ccs-box tr').append(td);
            });
            $.each(game.ccs.trus, function(i, cc) {
                var td = $('<td></td>');
                var box_cc = $('<div></div>');
                box_cc.addClass('game-body-cc');
                box_cc.append("<img src='"+cc.item_cover+"' />");
                box_cc.append('<div>🏆信用最高</div>');
                box_cc.append('<div>到手价：¥ '+(Number(cc.item_price.split(' - ')[0])+Number(cc.express_price))+'</div>');
                box_cc.bind('click', function() {on_cc_click(cc);});
                td.append(box_cc);
                $('.game-body-ccs-box tr').append(td);
            });
            $.each(game.ccs.near, function(i, cc) {
                var td = $('<td></td>');
                var box_cc = $('<div></div>');
                box_cc.addClass('game-body-cc');
                box_cc.append("<img src='"+cc.item_cover+"' />");
                box_cc.append('<div>🚀离您最近</div>');
                box_cc.append('<div>到手价：¥ '+(Number(cc.item_price.split(' - ')[0])+Number(cc.express_price))+'</div>');
                box_cc.bind('click', function() {on_cc_click(cc);});
                td.append(box_cc);
                $('.game-body-ccs-box tr').append(td);
            });

            var cc_detail = $('<div></div>');
            cc_detail.addClass('wechat-list');
            cc_detail.addClass('game-body-cc-detail');
            cc_detail.append("<div class='wechat-list-cell-kv' style='height: 48px'><div id='cc_item_name' style='width: 100%'></div></div>");
            cc_detail.append("<div class='wechat-list-cell-kv'><div>售价</div><div id='cc_item_price'></div></div>");
            cc_detail.append("<div class='wechat-list-cell-kv'><div>快递</div><div id='cc_express_price'></div></div>");
            cc_detail.append("<div class='wechat-list-cell-kv'><div>销量</div><div id='cc_item_sold'></div></div>");
            cc_detail.append("<div class='wechat-list-cell-kv'><div>等级</div><div id='cc_shop_rate'></div></div>");
            cc_detail.append("<div class='wechat-list-cell-kv'><div>店铺</div><div id='cc_shop_name'></div></div>");
            cc_detail.append("<div class='wechat-list-cell-kv'><div>掌柜</div><div id='cc_shop_owner'></div></div>");
            cc_detail.append("<div class='wechat-list-cell-kv'><div>地址</div><div id='cc_shop_addr'></div></div>");
            cc_detail.append("<a><div class='wechat-button wechat-button-major wechat-button-small'>长按拷贝宝贝链接<br>到浏览器打开或在APP中直接搜索</div></a>")
            $('.game-body-ccs').append(cc_detail);
        }
    });
}

function on_cc_click(cc) {
    $('#cc_item_name').text(cc.item_name);
    $('#cc_item_price').text('¥ ' + cc.item_price);
    $('#cc_express_price').text('¥ ' + cc.express_price);
    $('#cc_item_sold').text(cc.item_sold);
    $('#cc_shop_rate').text(cc_shop_rate(cc.shop_rate));
    $('#cc_shop_name').text(cc.shop_name);
    $('#cc_shop_owner').text(cc.shop_owner);
    $('#cc_shop_addr').text(cc.shop_addr);
    $('.game-body-cc-detail a').attr('href', cc.item_url.replace(/https/, 'taobao').replace(/http/, 'taobao'));

    $('.game-body-cc-detail').show();
    $('.game-body-cc-detail').css('opacity', '1');
}

function cc_shop_rate(rate) {
    var r = rate.split(' ');
    var s;
    var p = '';
    switch (r[0]) {
    case 'tb-rank-cap':
        s = '👑';
        break;
    case 'tb-rank-blue':
        s = '💎';
        break;
    case 'tb-rank-red':
        s = '❤️';
        break;
    }
    for (var i = 0; i < r[1]; i++) p += s;
    return p;
}


