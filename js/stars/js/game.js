define(['game.view', 'game.world', 'pixi'], function (view, world, PIXI) {
    
    'use strict';
    
    var game = view;
    game.world = world;
    
    document.app = new PIXI.Application(window.innerWidth, window.innerHeight, {
        backgroundColor : 0x000000
    });

    document.body.appendChild(document.app.view);
    
    var frame = function (view) {
        if (view.data && view.data.tick) view.data.tick();
        if (view.clear) view.clear();
        if (view.draw)  view.draw();

        if (view.children) {
            for (var i = 0; i < view.children.length; i++) {
                frame(view.children[i]);
            }
        }
    };

    document.app.ticker.add(function (delta) {
        frame(document.app.stage);
    });
    
    return game;
    
});
