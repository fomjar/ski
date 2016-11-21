
-- cdb命令映射表
drop table if exists tbl_instruction;
create table tbl_instruction (
    i_inst  integer,
    c_mode  char(5),    -- st:执行sql语句 sp:执行存储过程
    i_out   tinyint,    -- 出参个数
    c_sql   text        -- 格式：先出参，后入参，出参以英文的?代替，入参使用$打头，入参名称必须与消息中的参数名称完全相同
);

-- 用户
drop table if exists tbl_user;
create table tbl_user (
    i_uid       integer,        -- 编号
    t_create    datetime,       -- 创建时间
    c_pass      varchar(16),    -- 密码
    c_phone     varchar(16),    -- 电话
    c_email     varchar(32),    -- 邮箱
    c_name      varchar(32),    -- 姓名
    c_cover     mediumtext,     -- 封面图
    i_gender    tinyint,        -- 性别：0 - 女；1 - 男
    primary key (i_uid)
);

-- 用户状态
drop table if exists tbl_user_state;
create table tbl_user_state (
    i_uid       integer,        -- 编号
    i_state     tinyint,        -- 状态：0 - 离线; 1 - 在线
    t_change    datetime,       -- 变化时间
    i_terminal  tinyint,        -- 终端：1 - web; 2 - app
    c_token     varchar(64),    -- 口令
    c_location  varchar(64),    -- 位置
    primary key (i_uid)
);

-- 用户状态历史
drop table if exists tbl_user_state_history;
create table tbl_user_state_history (
    i_uid       integer,        -- 编号
    i_state     tinyint,        -- 状态：0 - 离线; 1 - 在线
    t_change    datetime,       -- 变化时间
    i_terminal  tinyint,        -- 终端：1 - m-web; 2 - pc-web; 3 - app
    c_token     varchar(64),    -- 口令
    c_location  varchar(64)     -- 位置
);

-- 消息模板
create table tbl_message_geohash6 (
    c_mid       varchar(128),       -- 编号(geohash:timestamp)
    t_time      datetime,           -- 时间
    i_uid       integer,            -- 用户
    i_coosys    tinyint,            -- 坐标系统：0 - mars; 1 - baidu
    i_lat       decimal(24, 20),    -- 纬度
    i_lng       decimal(24, 20),    -- 经度
    c_geohash   varchar(16),        -- geohash编码
    i_type      tinyint,            -- 类型：0 - 普通
    c_text      text,               -- 消息文本
    c_image     mediumtext,         -- 消息图片
    primary key (c_mid)
);
drop table if exists tbl_message_geohash6;

-- 消息关注模板
create table tbl_message_geohash6_focus (
    c_mid       varchar(128),   -- 消息编号
    i_uid       integer,        -- 用户编号
    t_time      datetime,       -- 关注时间
    i_type      tinyint         -- 关注类型：0 - none; 1 - up; 2 - down
);
drop table if exists tbl_message_geohash6_focus;

-- 消息回复映射
create table tbl_message_geohash6_reply (
    c_mid   varchar(128),
    c_rid   varchar(128)
);
drop table if exists tbl_message_geohash6_reply;

-- 临时GeoHash序列
drop table if exists tmp_geohash;
create table tmp_geohash (c_geohash varchar(16));

-- 临时消息序列
drop table if exists tmp_message;
create table tmp_message (
    c_mid       varchar(128),       -- 编号(geohash:timestamp)
    t_time      datetime,           -- 时间
    i_uid       integer,            -- 用户
    i_coosys    tinyint,            -- 坐标系统：0 - mars; 1 - baidu
    i_lat       decimal(24, 20),    -- 纬度
    i_lng       decimal(24, 20),    -- 经度
    c_geohash   varchar(16),        -- geohash编码
    i_type      tinyint,            -- 类型：0 - 普通
    c_text      text,               -- 消息文本
    c_image     mediumtext,         -- 消息图片
    i_distance  integer,
    i_second    integer,
    i_focus     integer,
    i_reply     integer,
    i_weight    integer
);

-- 活动
drop table if exists tbl_activity;
create table tbl_activity (
    i_aid       integer,            -- 编号
    i_owner     integer,            -- 发起人
    t_create    datetime,           -- 创建时间
    i_lat       decimal(24, 20),    -- 维度
    i_lng       decimal(24, 20),    -- 经度
    c_geohash   varchar(16),        -- geohash编码
    c_title     varchar(256),       -- 标题
    c_text      text,               -- 描述
    c_image     mediumtext,         -- 图片
    c_begin     varchar(32),        -- 开始时间
    c_end       varchar(32),        -- 结束时间
    i_state     tinyint,            -- 状态：0 - 初始化；1 - 开始；2 - 关闭
    primary key (i_aid)
);

-- 活动角色
drop table if exists tbl_activity_role;
create table tbl_activity_role (
    i_aid   integer,        -- 活动编号
    i_arsn  tinyint,        -- 角色序号
    c_name  varchar(32),    -- 名称
    i_apply tinyint,        -- 可申请：0 - 不能；1 - 能
    i_count integer         -- 人数
);

-- 活动参与人
drop table if exists tbl_activity_player;
create table tbl_activity_player (
    i_aid   integer,    -- 活动编号
    i_uid   integer,    -- 用户
    i_arsn  tinyint,    -- 角色序号
    t_time  datetime    -- 加入时间
);

-- 活动模块
drop table if exists tbl_activity_module;
create table tbl_activity_module (
    i_aid       integer,        -- 活动编号
    i_amsn      tinyint,        -- 模块编号
    i_type      tinyint,        -- 模块类型: 1 - 投票
    c_title     varchar(256),   -- 标题
    c_text      text            -- 描述
);

-- 活动模块 - 权限
drop table if exists tbl_activity_module_privilege;
create table tbl_activity_module_privilege (
    i_aid       integer,    -- 活动编号
    i_amsn      tinyint,    -- 模块序号
    i_arsn      tinyint,    -- 角色序号
    i_privilege integer     -- 权限：1<<0 - 可读；1<<1 - 可写
);

-- 活动模块 - 投票
drop table if exists tbl_activity_module_vote;
create table tbl_activity_module_vote (
    i_aid       integer,        -- 活动编号
    i_amsn      tinyint,        -- 模块编号
    i_select    tinyint,        -- 选择类型: 0 - 单选，n - 多选，最多n个
    i_anonym    tinyint,        -- 匿名模式: 0 - 实名，1 - 匿名，2 - 半实名
    i_item      tinyint         -- 选项类型: 0 - 文字，1 - 图文，2 - 用户
);

-- 活动模块 - 投票项
drop table if exists tbl_activity_module_vote_item;
create table tbl_activity_module_vote_item (
    i_aid       integer,
    i_amsn      tinyint,
    i_amvisn    tinyint,
    c_arg0      mediumtext,
    c_arg1      mediumtext,
    c_arg2      mediumtext,
    c_arg3      mediumtext,
    c_arg4      mediumtext,
    c_arg5      mediumtext,
    c_arg6      mediumtext,
    c_arg7      mediumtext,
    c_arg8      mediumtext,
    c_arg9      mediumtext
);

-- 活动模块 - 投票项投票人
drop table if exists tbl_activity_module_vote_player;
create table tbl_activity_module_vote_player (
    i_aid       integer,
    i_amsn      tinyint,
    i_amvisn    tinyint,
    i_uid       integer,
    i_result    tinyint,    -- 投票结果：0 - 取消，1 - 确认
    t_time      datetime
);






