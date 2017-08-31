define(function () {
    
    'use strict';
    
    var world = {};
    
    world.init = function (game) {
        
        document.app.stage.addChild(new game.VPaneAsset());
        
        var button = new game.VButtonPrimary('test', function () {window.alert('test');});
        button.data.x = 300;
        button.data.y = 100;
        
        document.app.stage.addChild(button);
        
        var star = new game.VStar('home');
        
        star.data.x = document.app.view.width / 2;
        star.data.y = document.app.view.height / 2;

        document.app.stage.addChild(star);
    };
    
    return world;
    
});