define(['game.view', 'game.world', 'pixi'], function (view, world, PIXI) {
    
    'use strict';
    
    var game = {
        view    : view,
        world   : world
    };
    
    document.app = new PIXI.Application(window.innerWidth, window.innerHeight, {
        backgroundColor : 0x000000
    });

    document.body.appendChild(document.app.view);
    
    var frame = function (root_view) {
        if (root_view.data && root_view.data.tick) {root_view.data.tick(); }
        if (root_view.clear) {root_view.clear(); }
        if (root_view.draw) {root_view.draw(); }

        if (root_view.children) {
            var i = 0;
            for (i = 0; i < root_view.children.length; i += 1) {
                frame(root_view.children[i]);
            }
        }
    };

    document.app.ticker.add(function (delta) {
        frame(document.app.stage);
    });
    
    return game;
    
});
