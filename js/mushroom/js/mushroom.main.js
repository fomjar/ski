(function () {
    'use strict';

    // do init
    (function init() {
        var width = window.innerWidth,
            height = window.innerHeight;
        document.app = new PIXI.Application(width, height, {
            backgroundColor : 0x000000
        });
        document.body.appendChild(document.app.view);

        document.app.ticker.add(function (delta) {
            document.mushroom.frame(document.app.stage);
        });
    }());

}());

