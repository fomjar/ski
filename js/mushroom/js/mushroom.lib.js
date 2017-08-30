(function () {
    'use strict';

    PIXI.Container.prototype.tick = function () {};
    PIXI.Container.prototype.draw = function () {};
    
    document.mushroom = {};
    
    document.mushroom.frame = function (root) {
        if (root.tick)  root.tick();
        if (root.clear) root.clear();
        if (root.draw)  root.draw();

        if (root.children) {
            for (var i = 0; i < root.children.length; i++)
                document.mushroom.frame(root.children[i]);
        }
    };
    document.mushroom.MushRoom = function() {};
    document.mushroom.MushRoom.prototype = new PIXI.Graphics();
    document.mushroom.MushRoom.prototype.draw = function () {
        this.beginFill(0x0000FF, 0.5);
        this.drawCircle(this.position.x, this.position.y, 30);
        this.endFill();
    };
}());
