(function($) {

fomjar.framework.phase.append('dom', build_head);
fomjar.framework.phase.append('dom', build_body);
fomjar.framework.phase.append('dom', build_post);
fomjar.framework.phase.append('dom', build_intr);
fomjar.framework.phase.append('dom', build_ccs);
fomjar.framework.phase.append('dom', build_rent);
fomjar.framework.phase.append('dom', build_chat);

fomjar.framework.phase.append('ren', setup);

function build_head() {
    var head = $('<div></div>');
    head.addClass('game-head');
    head.append("<img class='background'/>");
    head.append("<img class='cover'/>");
    head.append("<div class='name_zh'></div>");
    head.append("<div class='name_en'></div>");
    head.append("<div class='category'></div>");
    head.append("<div class='language'></div>");
    head.append("<div class='sale'></div>");

    $('.vcg .frame .body').append(head);
}

function build_body() {
    var body = $('<div></div>');
    body.addClass('game-body');

    $('.vcg .frame .body').append(body);
}

function build_post() {
    var poster = $('<div></div>');
    poster.addClass('poster');

    $('.game-body').append(poster);
}

function build_intr() {
    var introduction = $('<div></div>');
    introduction.addClass('intr');

    $('.game-body').append(introduction);
}

function build_ccs() {
    var ccs = $('<div></div>');
    ccs.addClass('ccs');

    var category = $('<div></div>');
    category.addClass('category');
    category.text('适合您的光盘出售');

    var tips = $('<div></div>');
    tips.addClass('tips');
    
    var box = $('<div><table><tr></tr></table></div>');
    box.addClass('box');

    ccs.append([category, tips, box]);

    $('.game-body').append(ccs);
}

function build_rent() {
    var rent = $('<div></div>');
    rent.addClass('rent');
    var category = $('<div></div>');
    category.addClass('category');
    category.text('即刻体验这款游戏');
    var list = $('<div></div>');
    list.addClass('list');
    var button = $('<div></div>');
    button.addClass('button button-major button-small');
    button.text('我要体验');
    // button.bind('touchend', function() {window.location = 'apply_rent_begin.html?gid=' + fomjar.util.args().gid});
    button.bind('touchend', function() {vcg.show_toast('请到淘宝\"VC电玩\"进行体验', 10000);});

    rent.append([category, list, button]);

    $('.game-body').append(rent);
}

function build_chat() {
    $('.game-body').append("<div style='margin-top: 20px; height : 3px;' ontouchend=\"window.location='query_chatroom.html?gid="+fomjar.util.args().gid+"'\"></div>");
}

function setup() {
    var gid = fomjar.util.args().gid;
    vcg.show_toast('正在获取...');
    fomjar.net.send(fomjar.net.ISIS.INST_ECOM_QUERY_GAME, {gid : gid}, function(code, desc) {
        vcg.hide_toast();
        if (0 != code) {
            vcg.show_toast(desc, 10000);
            return;
        }

        var game = desc;

        // title
        fomjar.util.title(game.name_zh_cn);

        // head
        $('.game-head .background').css('background-image', 'url('+game.url_cover+')');
        $('.game-head .cover').attr('src', game.url_cover);
        $('.game-head .name_zh').text(game.name_zh_cn);
        $('.game-head .name_en').text(game.name_en);
        $('.game-head .category').text(game.category);
        $('.game-head .language').text(game.language);
        $('.game-head .sale').text(game.sale);
        
        // body
        // vedio
        if (0 < game.vedio.length) {
            $('.poster').append("<video width='100%' height='100%' controls autobuffer src='" + game.vedio + "'/>");
        }
        // poster
        $.each(game.url_poster, function(i, p) {
            if (0 < p.length)
                $('.poster').append("<div style='background-image: url(" + p + ")'/>");
        });
        // skippr
        if (0 == $('.poster >*').length) {
            $('.poster').hide();
        } else {
            $('.poster').skippr({
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
        $('.intr').append('<p>'+game.introduction.replace(/\|/g, '</p><p>') + '</p>');
        // css
        if (0 == game.ccs.total) {
            $('.ccs').hide();
        } else {
            $('.ccs .tips').text('我们在共 '+game.ccs.total+' 款商品中为您挑选了以下几款商品');

            $.each(game.ccs.sold, function(i, cc) {
                var td = $('<td></td>');
                var box_cc = $('<div></div>');
                box_cc.addClass('cc');
                box_cc.append("<img src='"+cc.item_cover+"' />");
                box_cc.append('<div>🔥销量最高</div>');
                box_cc.append('<div>到手价：¥ '+(Number(cc.item_price.split(' - ')[0])+Number(cc.express_price))+'</div>');
                box_cc.bind('click', function() {on_cc_click(cc);});
                td.append(box_cc);
                $('.box tr').append(td);
            });
            $.each(game.ccs.conv, function(i, cc) {
                var td = $('<td></td>');
                var box_cc = $('<div></div>');
                box_cc.addClass('cc');
                box_cc.append("<img src='"+cc.item_cover+"' />");
                box_cc.append('<div>💰价格最低</div>');
                box_cc.append('<div>到手价：¥ '+(Number(cc.item_price.split(' - ')[0])+Number(cc.express_price))+'</div>');
                box_cc.bind('click', function() {on_cc_click(cc);});
                td.append(box_cc);
                $('.box tr').append(td);
            });
            $.each(game.ccs.trus, function(i, cc) {
                var td = $('<td></td>');
                var box_cc = $('<div></div>');
                box_cc.addClass('cc');
                box_cc.append("<img src='"+cc.item_cover+"' />");
                box_cc.append('<div>🏆信用最高</div>');
                box_cc.append('<div>到手价：¥ '+(Number(cc.item_price.split(' - ')[0])+Number(cc.express_price))+'</div>');
                box_cc.bind('click', function() {on_cc_click(cc);});
                td.append(box_cc);
                $('.box tr').append(td);
            });
            $.each(game.ccs.near, function(i, cc) {
                var td = $('<td></td>');
                var box_cc = $('<div></div>');
                box_cc.addClass('cc');
                box_cc.append("<img src='"+cc.item_cover+"' />");
                box_cc.append('<div>🚀离您最近</div>');
                box_cc.append('<div>到手价：¥ '+(Number(cc.item_price.split(' - ')[0])+Number(cc.express_price))+'</div>');
                box_cc.bind('click', function() {on_cc_click(cc);});
                td.append(box_cc);
                $('.box tr').append(td);
            });

            var cc_detail = $('<div></div>');
            cc_detail.addClass('list');
            cc_detail.addClass('cc-detail');
            cc_detail.append("<div class='cell-sg' style='height: 4em'><div id='item_name' style='width: 100%'></div></div>");
            cc_detail.append("<div class='cell-kv'><div>售价</div><div id='item_price'></div></div>");
            cc_detail.append("<div class='cell-kv'><div>快递</div><div id='express_price'></div></div>");
            cc_detail.append("<div class='cell-kv'><div>销量</div><div id='item_sold'></div></div>");
            cc_detail.append("<div class='cell-kv'><div>等级</div><div id='shop_rate'></div></div>");
            cc_detail.append("<div class='cell-kv'><div>店铺</div><div id='shop_name'></div></div>");
            cc_detail.append("<div class='cell-kv'><div>掌柜</div><div id='shop_owner'></div></div>");
            cc_detail.append("<div class='cell-kv'><div>地址</div><div id='shop_addr'></div></div>");
            cc_detail.append("<a><div class='button button-major button-small'>长按拷贝宝贝链接<br>到浏览器打开或在APP中直接搜索</div></a>")
            $('.ccs').append(cc_detail);
        }
        // rent
        if (game.rent_avail_a && 0 != game.rent_price_a) {
            $('.rent .list').append("<div class='cell-kv'><div>认证</div><div>"+game.rent_price_a+' 元/天</div></div>');
        }
        if (game.rent_avail_b && 0 != game.rent_price_b) {
            $('.rent .list').append("<div class='cell-kv'><div>不认证</div><div>"+game.rent_price_b+' 元/天</div></div>');
        }
        if ((!game.rent_avail_a || 0 == game.rent_price_a)
            && (!game.rent_avail_b || 0 == game.rent_price_b)) {
            $('.rent').hide();
        }
    });

    animate();
}

function on_cc_click(cc) {
    $('.cc-detail #item_name').text(cc.item_name);
    $('.cc-detail #item_price').text('¥ ' + cc.item_price);
    $('.cc-detail #express_price').text('¥ ' + cc.express_price);
    $('.cc-detail #item_sold').text(cc.item_sold);
    $('.cc-detail #shop_rate').text(cc_shop_rate(cc.shop_rate));
    $('.cc-detail #shop_name').text(cc.shop_name);
    $('.cc-detail #shop_owner').text(cc.shop_owner);
    $('.cc-detail #shop_addr').text(cc.shop_addr);
    $('.cc-detail a').attr('href', cc.item_url.replace(/https/, 'taobao').replace(/http/, 'taobao'));

    $('.cc-detail').show();
    setTimeout(function() {$('.cc-detail').css('opacity', '1');}, 0);
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

function animate() {
    setTimeout(function() {$('.game-head').css('height', '10em');}, 500);
}



})(jQuery)

