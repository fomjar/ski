define(['game.data', 'tween', 'pixi'], function (data, tween, PIXI) {
    
    'use strict';
    
    var view = {};
    
    view.BaseView = function () {
        this.data   = null;
        this.interactive    = true;
//        this.buttonMode     = true;
    };
    view.BaseView.prototype = new PIXI.Graphics();
    view.BaseView.prototype.draw    = function () {};
    
    view.Star = function () {
        this.data = new data.Star();
        this.on('pointerover',  function () {this.data.tween('scale', 1.05); });
        this.on('pointerout',   function () {this.data.tween('scale', 1); });
    };
    view.Star.prototype = new view.BaseView();
    view.Star.prototype.draw = function () {
        this.beginFill(this.data.color_bg, this.data.alpha);
        this.lineStyle(4, this.data.color_bd, this.data.alpha);
        this.drawCircle(this.data.x, this.data.y, this.data.radius * this.data.scale);
        this.endFill();
    };
    
    return view;
});