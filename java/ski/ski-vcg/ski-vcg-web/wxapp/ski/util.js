
function ots(o) {
    switch (typeof o) {
    case 'object'   : return JSON.stringify(o);
    default         : return o.toString();
    }
}

function sto(s) {
    switch (typeof o) {
    case 'string'   : return JSON.parse(o);
    default         : return s;
    }
}

module.exports = {
    ots     : ots,
    sto     : sto,
};