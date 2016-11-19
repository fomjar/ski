
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
    /** 查询活动 */
    INST_QUERY_ACTIVITY             = 0x00002008,
    /** 查询角色 */
    INST_QUERY_ACTIVITY_ROLE        = 0x00002009,
    /** 查询活动参与人 */
    INST_QUERY_ACTIVITY_PLAYER      = 0x0000200A,
    /** 查询活动与模块关联 */
    INST_QUERY_ACTIVITY_MODULE      = 0x0000200B,
    /** 查询活动模块权限 */
    INST_QUERY_ACTIVITY_MODULE_PRIVILEGE    = 0x0000200C,
    /** 查询活动模块 - 投票 */
    INST_QUERY_ACTIVITY_MODULE_VOTE         = 0x0000200D,
    /** 查询活动模块 - 投票项 */
    INST_QUERY_ACTIVITY_MODULE_VOTE_ITEM    = 0x0000200E,
    /** 查询活动模块 - 投票成员 */
    INST_QUERY_ACTIVITY_MODULE_VOTE_PLAYER  = 0x0000200F,
    
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
    /** 更新活动 */
    INST_UPDATE_ACTIVITY            = 0x00003008,
    /** 更新活动角色 */
    INST_UPDATE_ACTIVITY_ROLE       = 0x00003009,
    /** 更新活动参与人 */
    INST_UPDATE_ACTIVITY_PLAYER     = 0x0000300A,
    /** 更新活动与模块关联 */
    INST_UPDATE_ACTIVITY_MODULE     = 0x0000300B,
    /** 更新活动模块权限 */
    INST_UPDATE_ACTIVITY_MODULE_PRIVILEGE   = 0x0000300C,
    /** 更新活动模块 - 投票 */
    INST_UPDATE_ACTIVITY_MODULE_VOTE        = 0x0000300D,
    /** 更新活动模块 - 投票项 */
    INST_UPDATE_ACTIVITY_MODULE_VOTE_ITEM   = 0x0000300E,
    /** 更新活动模块 - 投票成员 */
    INST_UPDATE_ACTIVITY_MODULE_VOTE_PLAYER = 0x0000300F,
    
    INST_UNKNOWN                    : 0xFFFFFFFF
};