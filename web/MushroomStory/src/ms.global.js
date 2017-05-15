var g = {};
var ms = {};

(function() {

g.d = {};
g.d.stage = {
    width   : 1280,
    height  : 800,
};
// basic
g.d.font = {
    logo        : g.d.stage.height / 10,
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
g.d.stage.background = g.d.color.stage;
g.d.spore = {
    speed   : 0.6,
}

})();
