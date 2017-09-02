define(['game.view', 'pixi'], function (view, PIXI) {
    
    'use strict';
    
    let game = view;    
    
    game.init = function () {
        init_app();
        init_asset();
        init_view();
    };
    
    let init_app = function () {
        let screen = {width : window.innerWidth, height : window.innerHeight};
        
        let app = new PIXI.Application(screen.width, screen.height, {
            backgroundColor : 0x000000,
            antialias       : true,
        });

        document.body.appendChild(app.view);

        let frame = function (view) {
            if (view.clear) view.clear();
            if (view.draw)  view.draw();

//            (()=>{
//                if (!view.beginFill) return;
//                
//                view.beginFill(0, 0);
//                view.lineStyle(1, 0xffffff, 1);
//                view.drawRect(- view.data.width / 2, - view.data.height / 2, view.data.width, view.data.height);
//                view.endFill();
//            })();
            
            if (view.children) {
                for (let i = 0; i < view.children.length; i++) {
                    frame(view.children[i]);
                }
            }
        };

        app.ticker.add(function (delta) {
            frame(app.stage);
        });
        
        game.screen = screen;
        game.app = app;
    };
    
    let init_asset = function () {
        game.asset = {
            home    : {x : game.screen.width / 2, y : game.screen.height / 2},
        };
    }
    
    let init_view = function () {
        var home = new game.VStar('home');
        home.data.bindi(game.asset.home);
        game.app.stage.addChild(home);
        
        game.app.stage.addChild(new game.VPaneResource());
    };
    
    return game;
    
});
