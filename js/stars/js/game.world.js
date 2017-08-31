define(function () {
    
    'use strict';
    
    var world = {};
    
    world.init = function (game) {
        var star = new game.view.Star();
        world.star = star;
        
        star.data.x = document.app.view.width / 2;
        star.data.y = document.app.view.height / 2;

        document.app.stage.addChild(star);
    };
    
    return world;
    
});