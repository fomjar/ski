
(function($) {

fomjar.framework.phase.append('dom', animate);

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
    load_message();
    if (sn.user) {
        $('.sn .foot').css('bottom', '0');
    } else {
        sn.stub.login.push(function() {
            $('.sn .foot').css('bottom', '0')
        });
    }
}

function load_message() {
    var iv = setInterval(function() {
        if (!sn.location) return;
        
        fomjar.net.send(ski.ISIS.INST_QUERY_MESSAGE, {
            lat : sn.location.point.lat,
            lng : sn.location.point.lng,
            pos : sn.message.length,
            len : 20
        }, function(code, desc) {
            if (0 != code) return;
            
            $.each(desc, function(i, msg) {sn.message.push(msg);});
            $.each(sn.message, function(i, msg) {
                if (!msg.visible) {
                    msg.visible = true;
                    $('.sn .body').append(create_message_panel(msg));
                }
            });
            clearInterval(iv);
        });
    }, 1000);
}

function create_message_panel(msg) {
    var div = $('<div></div>');
    div.addClass('msg');
    var mc = $('<div></div>');
    mc.addClass('mc');
    var ma = $('<div></div>');
    ma.addClass('ma');
    
    var mh = $('<div></div>');
    mh.addClass('mh');
    mh.append("<div><img src='" + msg.ucover + "'/><div>" + msg.uname + "</div></div>")
    mh.append("<div><div>" + msg.distance + "米</div><img src='res/msg-dist.png'/><div>" + msg.second + "秒</div><img src='res/msg-time.png'/></div>")
    var mb = $('<div></div>');
    mb.addClass('mb');
    if (0 < msg.mtext.length)  mb.append('<div>' + new fomjar.util.base64().decode(msg.mtext)  + '</div>');
    if (0 < msg.mimage.length) mb.append("<img src='" + msg.mimage + "' / >");
    var mf = $('<div></div>');
    mf.addClass('mf');
    mf.append("<div class='button'>回复(" + msg.reply + ")</div>");
    
    mc.append([mh, mb, mf]);
    
    ma.append("<div><img src='res/msg-up.png' /></div><div>" + msg.focus + "</div><div><img src='res/msg-down.png' /></div>");
    
    var table = $('<table></table>');
    var tr = $('<tr></tr');
    var td_mc = $('<td></td>');
    td_mc.append(mc);
    var td_ma = $("<td></td>");
    td_ma.append(ma);
    tr.append([td_mc, td_ma]);
    table.append(tr);
    
    div.append(table);
    return div;
}

})(jQuery)

