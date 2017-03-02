var frs = {};

(function($) {

fomjar.framework.phase.append('ini', function() {
    // initialize
    frs.config = function(key, val) {
        if (val) {
            return fomjar.util.cookie(key, val);
        } else {
            val = fomjar.util.cookie(key);
            if (val && 0 < val.length) return val;
            else return undefined;
        }
    }
    frs.token   = frs.config('token');
    frs.user    = frs.config('user');
});

fomjar.framework.phase.append('ini', function() {
    // authorize
    // window.location = '/login.html';
});

})(jQuery)

