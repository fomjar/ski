
(function($) {

fomjar.framework.phase.append('dom', build_foot);
fomjar.framework.phase.append('ren', init_event);
fomjar.framework.phase.append('ren', animate);
fomjar.framework.phase.append('ren', watch_location);

function build_foot() {
    var send = $('<div></div>');
    send.addClass('send');
    
    $('.sn .foot').append(send);
    
    $('.sn .foot .send').bind('click', function() {
        var dialog = sn.ui.dialog();
        dialog.append(create_send_panel(dialog));
        dialog.appear();
    });
}

function init_event() {
    // scroll event
    sn.ui.scroll($('.sn .body'),
        function() {    // begin
//             $('.sn .head').css('top', '-3em');
//             if (sn.user) $('.sn .foot').css('bottom', '-4em');
        },
        function() {    // end
//             $('.sn .head').css('top', '0');
//             if (sn.user) $('.sn .foot').css('bottom', '0');
        },
        function() {    // top
//             $('.sn .head').css('top', '0');
//             if (sn.user) $('.sn .foot').css('bottom', '0');
        },
        function() {    // bottom
            load_message(sn.msg.data.length, 10);
        },
        200);
    
    // pull event
    sn.ui.pull($('.sn .body'),
        function() {    // down
            load_message(0, 20);
        },
        function() {},  // up
        80);

}

function animate() {
    // revert
    var bg_img  = $('.sn .bg >img');
    var bg_mask = $('.sn .bg >div');
    var sn_head = $('.sn .head');
    
    bg_img.css('opacity', '0');
    bg_img.css('width', '100%');
    bg_img.css('height', '100%');
    bg_img.css(        'filter', 'none');
    bg_img.css('-webkit-filter', 'none');
    
    bg_mask.css('opacity', '0');
    
    sn_head.css('top', '-3em');
    
    // animate
    bg_img.bind('load', function() {
        bg_img.css('opacity', '1');
        
        setTimeout(function() {
            bg_img.css('width',  '105%');
            bg_img.css('height', '105%');
            
            setTimeout(function() {
                bg_img.css(        'filter', 'blur(.3em)');
                bg_img.css('-webkit-filter', 'blur(.3em)');
                
                bg_mask.css('opacity', '.3');
                
                sn_head.css('top', '0');
                
                animate_done();
            }, 3000);
        }, 500);
    });
}

function animate_done() {
    load_message(0, 20);
    
    if (sn.user) {
        $('.sn .foot').addClass('foot-appear');
    } else {
        sn.stub.login.push(function() {
            $('.sn .foot').addClass('foot-appear');
        });
    }
}

function watch_location() {
    var run = function(r){
        if (this.getStatus() == BMAP_STATUS_SUCCESS){
            var p = r.point;
            if (sn.user) {
                fomjar.net.send(ski.ISIS.INST_UPDATE_USER_STATE, {
                    state       : 1,
                    terminal    : 1,
                    location    : p.lat + ':' + p.lng
                }, function(code, desc) {});
            }
            new BMap.Geocoder().getLocation(p, function(rs) {
                var addr = rs.addressComponents.street + rs.addressComponents.streetNumber;
                if (0 < rs.surroundingPois.length) {
                    addr = rs.surroundingPois[0].title;
                }
                /*
                Object =
                    address: "中影国际影城南京雨花台南站店"
                    addressComponents: Object
                        city: "南京市"
                        district: "雨花台区"
                        province: "江苏省"
                        street: "明城大道"
                        streetNumber: ""
                    “Object”原型
                    business: "宁南"
                    point: H
                        lat: 31.98444
                        lng: 118.803924
                    “H”原型
                    surroundingPois: Array (4)
                        0 Object
                            Ui: "休闲娱乐"
                            address: "南京市雨花台区玉兰路99号(明发商业广场1幢B6区4层17室)"
                            city: "南京市"
                            eu: Array (1)
                                0 "休闲娱乐"
                            “Array”原型
                            phoneNumber: null
                            point: H
                                lat: 31.984822
                                lng: 118.804075
                            “H”原型
                            postcode: null
                            title: "中影国际影城南京雨花台南站店"
                            type: 0
                            uid: "9abe5786376f2dd98bfc06ef"
                        “Object”原型
                        1 {title: "永辉超市(雨花店)", uid: "7a443c7ec83b4ccd2fe67bb8", point: H, city: "南京市", Ui: "购物", …}
                        2 {title: "南京易居智能科技有限公司", uid: "3a46dbe174d7478203ec0106", point: H, city: "南京市", Ui: "公司企业", …}
                        3 {title: "德居欣舒适家居体验中心", uid: "f270eba5dda323d682288ae4", point: H, city: "南京市", Ui: "购物", …}
                    “Array”原型
                    “Object”原型
                */
                if (sn.user && (!sn.location || addr != sn.location.address)) {
                    var state_locate = sn.ui.state(1);
                    state_locate.find('>div').text(addr);
                    state_locate.find('>div').css('width', addr.length + 'em');
                    state_locate.flash();
                }
                sn.location = rs;
                if (sn.user) sn.location.address = addr;
                
                $.each(sn.stub.locate, function(i, f) {f(sn.location);});
            });
        } else {}
    };
    new BMap.Geolocation().getCurrentPosition(run);
    setTimeout(function() {if (sn.location) sn.ui.state(1).flash();}, 1000 * 5);
    setInterval(function() {new BMap.Geolocation().getCurrentPosition(run);}, 1000 * 10);
}

function create_send_panel(dialog) {
    dialog.addClass('dialog-send');
    
    var div = $('<div></div>');
    div.addClass('send-panel');
    
    div.append("<div><img src='res/msg-dist.png'/><div>" + sn.location.address + "</div></div>");
    
    var div_content = $('<div></div>');
    div_content.append("<textarea placeholder='想法 / 问询 / 活动 / 段子'></textarea>");
    div_content.append(sn.ui.choose_image(1024 * 1024 * 2, function(){}, function(){dialog.shake();}));
    
    div.append(div_content);
    div.append("<div><div class='button'>取消</div><div class='button button-default'>发送</div></div>");
    
    var div_tex = div.find('>*:nth-child(2) textarea');
    var div_ima = div.find('>*:nth-child(2) img');
    var div_can = div.find('>*:nth-child(3) .button:nth-child(1)');
    var div_sen = div.find('>*:nth-child(3) .button:nth-child(2)');
    
    div_can.bind('click', function() {dialog.disappear();});
    div_sen.doing = false;
    div_sen.bind('click', function() {
        var text    = div_tex.val();
        var image   = div_ima.attr('src');
        if (0 == text.length) {
            dialog.shake();
            return;
        }
        if (/^ +$/.test(text)) {
            dialog.shake();
            return;
        }
        
        if (div_sen.doing) return;
        
        div_sen.doing = true;
        div_sen.addClass('button-disable');
        
        text = new fomjar.util.base64().encode(text);
        fomjar.net.send(ski.ISIS.INST_UPDATE_MESSAGE, {
            coosys  : 1,
            lat     : sn.location.point.lat,
            lng     : sn.location.point.lng,
            type    : 0,
            text    : text,
            image   : image
        }, function(code, desc) {
            div_sen.doing = false;
            div_sen.removeClass('button-disable');
            if (0 == code) {
                dialog.disappear();
                load_message(0, 20);
            } else {
                dialog.shake();
                sn.ui.toast(desc);
            }
        });
    });
    
    return div;
}


var loading = false;

function load_message(pos, len) {
    if (!sn.location) return;
    if (loading) return;
    
    sn.ui.toast('正在加载', 1000 * 10);
    loading = true;
    if (!sn.uid) {
        sn.uid = new Date().getTime();
        fomjar.util.cookie('uid', sn.uid);
    }
    if (0 == pos) {
        $('.sn .body').children().detach();
        sn.msg.data = [];
    }
    fomjar.net.send(ski.ISIS.INST_QUERY_MESSAGE, {
        lat : sn.location.point.lat,
        lng : sn.location.point.lng,
        pos : pos,
        len : len
    }, function(code, desc) {
        loading = false;
        if (0 != code) return;
        
        if (0 == desc.length) {
            sn.ui.toast('已没有消息');
            return;
        }
        sn.ui.toast('加载完成', 500);
        
        var delay = 0;
        $.each(desc, function(i, data) {
            var exist = false;
            $.each(sn.msg.data, function(i, msg) {
                if (msg.data.mid == data.mid) {
                    exist = true;
                    return false;
                }
            });
            if (exist) return true;

            var msg = sn.msg.wrap(data);
            sn.msg.data.push(msg);
            
            setTimeout(function() {
                msg.ui.panel.css('opacity', '0');
                $('.sn .body').append(msg.ui.panel);
                setTimeout(function() {msg.ui.panel.css('opacity', '1');}, 0);
            }, delay);
            
            delay += 150;
        });

    });
}

})(jQuery)

