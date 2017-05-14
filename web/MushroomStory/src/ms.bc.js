ms.bc = {};

(function() {

ms.bc.scene = {};
ms.bc.scene.current = null;
ms.bc.scene.switch = function(scene) {
    var children = Laya.stage._childs;
    if (0 < children.length) {
        for (var i = 0; i < children.length; i++) {
            if (i < children.length - 1) children[i].hide();
            else children[i].hide({done : function() {scene.setup();}});    // last
        }
    } else {scene.setup();}
    ms.bc.scene.current = scene;
};
ms.bc.scene.Scene = function() {
    var sc = {};
    sc.setup = function() {};
    return sc;
};


ms.bc.scene.SceneLauncher = function() {
    var sc = new ms.bc.scene.Scene();
    sc.setup = function() {
        var logo = new ms.ui.Text();
        logo.text       = '蘑菇物语\nMushroom Story';
        logo.color      = g.d.color.ui_bd;
        logo.bold       = true;
        logo.fontSize   = g.d.font.logo;
        logo.width      = g.d.stage.width;
        logo.pos(0, logo.fontSize);

        var name = new ms.ui.Input();
        name.input.prompt = '输入昵称';
        name.size(name.input.fontSize * 16, name.input.fontSize * 2);
        name.pos(g.d.stage.width / 2, g.d.stage.height - name.height * 4);
        name.paint();

        var submit = new ms.ui.Button();
        submit.label = '开始游戏';
        submit.pos(g.d.stage.width / 2, name.y + name.input.fontSize * 3);
        submit.paint();

        var error = new ms.ui.Text();
        error.color = g.d.color.ui_er;
        error.bold = true;
        error.pos(name.x + name.width / 2, name.y - name.height / 2);

        logo.show();
        name.show();
        submit.show();
        error.show();

        submit.on(Laya.Event.MOUSE_UP, submit, function() {
            var text = name.input.text;
            if (0 == text.length) {
                error.text = '昵称不能为空';
                error.show();
                return;
            }
            ms.bc.scene.switch(new ms.bc.scene.ScenePlay());
        });
    };
    return sc;
};

ms.bc.scene.ScenePlay = function() {
    var build_map = function() {

    };
    var build_ui = function() {

    };

    var sc = new ms.bc.scene.Scene();
    sc.setup = function() {
        build_map();
        build_ui();
    };
    return sc;
};

})();