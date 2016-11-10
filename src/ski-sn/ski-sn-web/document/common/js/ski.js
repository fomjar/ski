
var ski = {};

ski.ISIS = {
    /** 认证 */
    INST_APPLY_AUTHORIZE            : 0x00001001,
    /** 验证 */
    INST_APPLY_VERIFY               : 0x00001002,
    
    /** 查询用户 */
    INST_QUERY_USER                 : 0x00002001,
    /** 查询用户配置 */
    INST_QUERY_USER_CONFIG          : 0x00002002,
    /** 查询消息 */
    INST_QUERY_MESSAGE              : 0x00002003,
    /** 查询用户状态 */
    INST_QUERY_USER_STATE           : 0x00002004,
    /** 查询用户状态历史 */
    INST_QUERY_USER_STATE_HISTORY   : 0x00002005,
    /** 查询消息关注 */
    INST_QUERY_MESSAGE_FOCUS        : 0x00002006,
    /** 查询消息回复 */
    INST_QUERY_MESSAGE_REPLY        : 0x00002007,
    
    /** 更新用户 */
    INST_UPDATE_USER                : 0x00003001,
    /** 更新用户配置 */
    INST_UPDATE_USER_CONFIG         : 0x00003002,
    /** 更新消息 */
    INST_UPDATE_MESSAGE             : 0x00003003,
    /** 更新用户状态 */
    INST_UPDATE_USER_STATE          : 0x00003004,
    /** 更新消息关注 */
    INST_UPDATE_MESSAGE_FOCUS       : 0x00003006,
    /** 更新消息回复 */
    INST_UPDATE_MESSAGE_REPLY       : 0x00003007,
    
    INST_UNKNOWN                    : 0xFFFFFFFF
};