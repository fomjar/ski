(function() {

setup();

function setup() {
    setup_laya();
    setup_stage();
}

function setup_laya() {
    Laya.Config.isAntialias = true;
    Laya.init(g.d.size.stage_w, g.d.size.stage_h, Laya.WebGL);
    Laya.Stat.show(Laya.Browser.clientWidth - 130, 0);
}

function setup_stage() {
    Laya.stage.alignV = Laya.Stage.ALIGN_MIDDLE;
    Laya.stage.alignH = Laya.Stage.ALIGN_CENTER;

    Laya.stage.scaleMode = 'showall';
    Laya.stage.bgColor = g.d.color.stage;
}

})();