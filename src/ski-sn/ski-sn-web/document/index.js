
(function($) {

fomjar.framework.phase.append('dom', init_animate);

function init_animate() {
    bg_animate();
}

function bg_animate() {
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
                
                if (ski.user) {
                    $('.sn .foot').css('bottom', '0');
                    show_message();
                }
            }, 3000);
        }, 500);
    });
    
    var i = setInterval(function() {
        if (ski.user && ski.user.location) {
            load_message(0, 30);
            clearInterval(i);
        }
    }, 1000);
    
}

function load_message(pos, len) {
    if (pos == 0) ski.message = [];
    
    fomjar.net.send(ski.ISIS.INST_QUERY_MESSAGE, {
        lat : ski.user.location.point.lat,
        lng : ski.user.location.point.lng,
        pos : pos,
        len : len
    }, function(code, desc) {
        if (0 == code) ski.message = desc;
    });
}

function clear_message() {
    $('.sn .body').html('');
}

function show_message() {
    $.each(ski.message, function(i, msg) {
        if (!msg.visible) {
            msg.visible = true;
            $('.sn .body').append(create_message_panel(msg));
        }
    });
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

