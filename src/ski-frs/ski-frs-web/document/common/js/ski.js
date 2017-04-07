
var ski = {};

(function($) {

ski.isis = {
    INST_AUTHORIZE          : 0x00000010,
    INST_UPDATE_PIC         : 0x00001001,
    INST_UPDATE_SUB_LIB     : 0x00001002,
    
    INST_QUERY_PIC          : 0x00002001,
    INST_QUERY_PIC_BY_FV_I  : 0x00002002,
    INST_QUERY_PIC_BY_FV    : 0x00002003,
    INST_QUERY_SUB_LIB      : 0x00002010,
    
    INST_APPLY_SUB_LIB_CHECK    : 0x00003001,
    INST_APPLY_SUB_LIB_IMPORT   : 0x00003002,
};

})(jQuery);