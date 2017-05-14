(function() {

main();

function main() {
    build_launch();
}

function build_launch() {
    var logo = new Laya.Text();
    logo.text = '蘑菇物语\nMushroom Story';
    logo.align = 'center';
    logo.fontSize = g.d.font.logo;
    logo.width = g.d.stage.width;
    logo.y = logo.fontSize;
    logo.color = g.d.color.ui_bg;

    var name = new ms.ui.Input();
    name.input.prompt = '输入昵称';
    name.size(name.input.fontSize * 16, name.input.fontSize * 2);
    name.pos(g.d.stage.width / 2, g.d.stage.height / 2);
    name.paint();

    var submit = new ms.ui.Button();
    submit.label = '开始游戏';
    submit.pos(g.d.stage.width / 2, name.y + name.input.fontSize * 3);
    submit.paint();

    Laya.stage.addChild(logo);
    Laya.stage.addChild(name);
    Laya.stage.addChild(submit);
}

function build_world() {
}

function build_human() {
}

})();
