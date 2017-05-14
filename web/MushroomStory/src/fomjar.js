fomjar = {};

(function() {

// watch
if (!Object.prototype.watch) {
    Object.prototype.watch = function (property_name, handler_before, handler_after) {
        var val_old = this[property_name];
        var val_new = val_old;
        var getter = function() {
            return val_new;
        };
        var setter = function(val) {
            if (handler_before) handler_before.call(this, property_name, val_new, val);
            val_old = val_new;
            val_new = val;
            if (handler_after) handler_after.call(this, property_name, val_old, val_new);
            return val_old;
        };
        if (delete this[property_name]) { 
            if (Object.defineProperty) { // ECMAScript 5
                Object.defineProperty(this, property_name, {get: getter,set: setter});
            } else if (Object.prototype.__defineGetter__ && Object.prototype.__defineSetter__) {
                Object.prototype.__defineGetter__.call(this, property_name, getter);
                Object.prototype.__defineSetter__.call(this, property_name, setter);
            }
        }
    };
}

if (!Object.prototype.unwatch) {
    Object.prototype.unwatch = function (property_name) {
        var val = this[property_name];
        delete this[property_name]; 
        this[property_name] = val;
    };
}

})();

