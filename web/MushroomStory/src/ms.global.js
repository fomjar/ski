var g = {};
var ms = {};

(function() {

g.d = {};
// basic
g.d.size = {
    stage_w : 1280,
    stage_h : 800,
    icon_l  : 40,
    icon_m  : 30,
    icon_s  : 20,
};
g.d.font = {
    logo        : g.d.size.stage_h / 10,
    ui_major    : 14,
};
g.d.color = {
    stage   : '#669966',
    ui_bg   : '#666666',
    ui_bd   : '#333355',
    ui_da   : '#999999',
    ui_fg   : '#eeeeee',
    ui_er   : '#553333',
    ui_lw   : 2,
    ui_rr   : g.d.font.ui_major / 3,
    sp_bg   : '#eeeeee',
    sp_bd   : '#666666',
    sp_rd   : 5,
    sp_lw   : 1,
};
// component
g.d.spore = {
    speed   : 0.6,
};

})();
