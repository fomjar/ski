
var ski = {};
ski.token   = (function() {return fomjar.util.cookie('token');})();
ski.user    = (function() {return fomjar.util.cookie('user');})();
ski.sendto  = function(url, data, cb) {
    if (2 == arguments.length) {
        cb = data;
        data = {};
    }
    if (undefined != ski.token) data.token  = ski.token;
    if (undefined != ski.user)  data.user   = ski.user;
    fomjar.net.sendto(url, data, cb);
};
ski.send    = function(inst, data, cb) {
    if (2 == arguments.length) {
        cb = data;
        data = {};
    }
    if (undefined != ski.token) data.token  = ski.token;
    if (undefined != ski.user)  data.user   = ski.user;
    fomjar.net.send(inst, data, cb);
};
