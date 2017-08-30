(function () {
    'use strict';

    PIXI.Container.prototype.tick = function () {};
    PIXI.Container.prototype.draw = function () {};
    
    document.mushroom = {
        frame   : function (root) {
            if (root.tick) root.tick();
            if (root.draw) root.draw();

            if (root.children) {
                for (var i = 0; i < root.children.length; i++)
                    document.mushroom.frame(root.children[i]);
            }
        },
        MushRoom : function () {
            this.__proto__ = PIXI.Graphics.prototype;
        }
    };
}());
