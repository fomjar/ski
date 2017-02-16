

setup();

function setup() {
    Laya.init(ch.d.stage.width, ch.d.stage.height, Laya.WebGL);
    Laya.Stat.show(Laya.Browser.clientWidth - 150, 0);

    setupStage();
    setupEngine();
    new ch.event.EventDispatcher().open();
}

function setupStage() {
    Laya.stage.alignV = Laya.Stage.ALIGN_MIDDLE;
    Laya.stage.alignH = Laya.Stage.ALIGN_CENTER;

    Laya.stage.scaleMode = 'showall';
    Laya.stage.bgColor = ch.d.stage.background;
}

function setupEngine() {
    // 初始化物理引擎
    var engine = Matter.Engine.create();
    engine.world.gravity.y = 0;
    Matter.Engine.run(engine);
    Matter.engine = engine;

    var laya_render = LayaRender.create({
        engine      : engine,
        container   : Laya.stage,
        width       : Laya.stage.width,
        height      : Laya.stage.height,
        options     : {
            wireframes      : false,
            background      : ch.d.stage.background,
        }
    });
    LayaRender.run(laya_render);
    Laya.render = laya_render;

}
