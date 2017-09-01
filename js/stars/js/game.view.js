define(['game.data', 'tween', 'pixi'], function (data, tween, PIXI) {
    
    'use strict';
    
    let view = data;
    
    view.View = class View extends PIXI.Graphics {
        constructor () {
            super();
            
            let find_data = (proto) => {
                var name = 'D' + proto.constructor.name.substring(1);
                if (data[name]) this.data = new data[name]();
                else {
                    if (proto.__proto__) find_data(proto.__proto__);
                    else this.data = new data.Data();
                }
            };
            find_data(this.__proto__);
            
            this.data.xetter('x', (x) => this.position.x = x);
            this.data.xetter('y', (y) => this.position.y = y);

            this.interactive    = true;
            this.state  = 'default';
        }
        
        draw () {}

        auto_interactive (s) {
            this.removeAllListeners('pointerover');
            this.removeAllListeners('pointerout');
            this.removeAllListeners('pointerdown');
            this.removeAllListeners('pointerup');
            this.removeAllListeners('pointerupoutside');
            
            this.on('pointerover', () => {
                if (s) {
                    data.tween(this.scale, 'x', s);
                    data.tween(this.scale, 'y', s);
                }
                this.state = 'over';
            });
            this.on('pointerout', () => {
                if (s) {
                    data.tween(this.scale, 'x', 1);
                    data.tween(this.scale, 'y', 1);
                }
                this.state = 'default';
            });
            this.on('pointerdown', () => {
                if (s) {
                    data.tween(this.scale, 'x', 1);
                    data.tween(this.scale, 'y', 1);
                }
                this.state = 'down';
            });
            this.on('pointerup', () => {
                if (s) {
                    data.tween(this.scale, 'x', s);
                    data.tween(this.scale, 'y', s);
                }
                this.state = 'over';
            });
            this.on('pointerupoutside', () => {
                if (s) {
                    data.tween(this.scale, 'x', 1);
                    data.tween(this.scale, 'y', 1);
                }
                this.state = 'default'
            });
        }
    };
    
    view.VPane = class VPane extends view.View {
        constructor () {super();}
        
        draw () {
            this.beginFill(this.data.color_bg, this.data.alpha);
            this.lineStyle(this.data.border, this.data.color_bd, this.data.alpha);
            this.drawRoundedRect(- this.data.width / 2, - this.data.height / 2, this.data.width, this.data.height, this.data.round);
            this.endFill();
        }
    };
    
    
    view.VPaneResource = class VPaneResource extends view.VPane {
        constructor () {
            super();
            
            let icon_stone = new view.VButton('石').style_icon_small();
            icon_stone.data.x = - this.data.width / 4;
            icon_stone.data.y = 0;
            
            this.addChild(icon_stone);
        }
    };
    
    
    view.VButton = class VButton extends view.VPane {
        constructor (text) {
            super();
            
            this.text   = new PIXI.Text(text, new PIXI.TextStyle({fontWeight : '100'}));
            this.addChild(this.text);
            
            this.data.xetter('width', (width) => {
                this.text.pivot.x = this.text.width / 2;
                this.text.pivot.y = this.text.height / 2 - 1;
            });
            this.data.xetter('height', (height) => {
                this.text.style.fontSize    = height * 2 / 3;
                this.text.pivot.x = this.text.width / 2;
                this.text.pivot.y = this.text.height / 2 - 1;
            });
        }
        
        style_icon_small () {
            this.buttonMode = false;
            this.data.style_icon_small();
            this.auto_interactive();
            return this;
        }
        style_icon_middle () {
            this.buttonMode = false;
            this.data.style_icon_middle();
            this.auto_interactive();
            return this;
        }
        style_icon_large () {
            this.buttonMode = false;
            this.data.style_icon_large();
            this.auto_interactive();
            return this;
        }
        style_primary () {
            this.buttonMode = true;
            this.data.style_primary();
            this.auto_interactive(1.05);
            return this;
        }

        click (action) {
            this.on('pointerup', action);
            return this;
        }
        
        draw () {
            super.draw();
            
            switch (this.state) {
                case 'over':
                    this.beginFill(0xffffff, 0.2);
                    this.lineStyle(this.data.border, 0xffffff, 0.2);
                    this.drawRoundedRect(- this.data.width / 2, - this.data.height / 2, this.data.width, this.data.height, this.data.round);
                    this.endFill();
                    break;
                case 'down':
                    this.beginFill(0x000000, 0.2);
                    this.lineStyle(this.data.border, 0x000000, 0.2);
                    this.drawRoundedRect(- this.data.width / 2, - this.data.height / 2, this.data.width, this.data.height, this.data.round);
                    this.endFill();
                    break;
            }
        }
    };
    
    view.VStar = class VStar extends view.View {
        constructor (type) {
            super();
            this.auto_interactive(1.1);
            this.data.type = type;
        }
        
        draw () {
            switch (this.data.type) {
                case 'home':
                    this.beginFill(this.data.color_bg, 1);
                    this.lineStyle(this.data.border, this.data.color_bd, 1);
                    this.drawCircle(0, 0, this.data.radius);
                    this.endFill();
                    break;
            }
        }
    };
    
    
    return view;
});