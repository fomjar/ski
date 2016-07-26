var ski = {};

ski.getChannelDesc = function(channel) {
    switch (channel) {
        case 0: return '淘宝';
        case 1: return '微信';
        case 2: return '支付宝';
        default: return '未知';
    }
};
