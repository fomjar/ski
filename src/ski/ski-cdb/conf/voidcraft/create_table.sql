-- cdb命令映射表
drop table if exists tbl_instruction;
create table tbl_instruction (
    i_inst  integer,
    c_mode  char(5),      -- st:执行sql语句 sp:执行存储过程
    i_out   tinyint,      -- 出参个数
    c_sql   varchar(255)  -- 格式：先出参，后入参，出参以英文的?代替，入参使用$打头，入参名称必须与消息中的参数名称完全相同
);

-- 平台账户信息
drop table if exists tbl_platform_account;
create table tbl_platform_account (
    i_paid      integer,        -- 平台账户ID
    c_user      varchar(32),    -- 用户名
    c_pass      varchar(32),    -- 密码
    c_name      varchar(32),    -- 姓名
    c_mobile    varchar(20),    -- 手机
    c_email     varchar(32),    -- 邮箱
    t_create    datetime,       -- 创建时间
    t_birth     date            -- 出生日期
);

-- 平台账户与渠道账户的关系
drop table if exists tbl_platform_account_relationship;
create table tbl_platform_account_relationship (
    i_paid      integer,    -- 平台账户ID
    i_caid      integer     -- 渠道账户ID
);

-- 游戏账户
drop table if exists tbl_game_account;
create table tbl_game_account (
    i_gaid      integer,        -- 游戏账户ID，对应产品实例
    c_user      varchar(32),    -- 用户名
    c_pass_a    varchar(32),    -- 密码A
    c_pass_b    varchar(32),    -- 密码B
    c_pass_curr varchar(32),    -- 当前密码
    t_birth     date            -- 出生日期
);

drop table if exists tbl_game_account_game;
create table tbl_game_account_game (
    i_gaid  integer,    -- 游戏ID
    i_gid   integer     -- 游戏账户ID
);

-- 当前租赁状态
drop table if exists tbl_game_account_rent;
create table tbl_game_account_rent(
    i_gaid      integer,    -- 游戏账号ID
    i_type      tinyint,    -- 租赁类型：0-A租，1-B租
    i_caid      integer,    -- 渠道账户账户ID
    i_state     tinyint,    -- 租赁状态: 0-空闲，1-租用，2-锁定
    t_change    datetime    -- 变化时间
);

-- 平台账户流水
drop table if exists tbl_game_account_rent_history;
create table tbl_game_account_rent_history (
    i_gaid      integer,    -- 游戏账号ID
    i_type      tinyint,    -- 租赁类型：0-A租，1-B租
    i_caid      integer,    -- 渠道账户ID
    i_state     tinyint,    -- 变化前的状态
    t_change    datetime    -- 变化时间
);

-- 游戏信息表
drop table if exists tbl_game;
create table tbl_game (
    i_gid           integer,        -- 游戏ID
    c_platform      varchar(16),    -- 游戏和账号所属平台：PS4、XBOX ONE、PS5、XBOX TWO、PS3、XBOX 360
    c_country       varchar(32),    -- 国家
    c_url_icon      varchar(128),   -- 图标URL
    c_url_poster    varchar(128),   -- 海报URL
    c_url_buy       varchar(128),   -- 采购网址
    t_sale          date,           -- 发售日期
    c_name_zh       varchar(64),    -- 中文名
    c_name_en       varchar(64)     -- 英文名
);

-- 订单
drop table if exists tbl_order;
create table tbl_order (
    i_oid       integer,        -- 订单ID
    i_platform  tinyint,        -- 平台类型：0-淘宝 1-微信 2-支付宝
    i_caid      integer,        -- 渠道账户ID
    t_create    datetime        -- 创建时间
);

-- 订单项
drop table if exists tbl_order_item;
create table tbl_order_item (
    i_oid           integer,        -- 平台订单ID
    i_oisn          integer,
    t_oper_time     datetime,
    i_oper_type     integer,        -- 操作类型，0-购买，1-充值，2-起租，3-退租，4-停租，5-续租，6-换租，7-赠券
    c_remark        varchar(64),
    c_oper_arg0     varchar(64),    -- 参数
    c_oper_arg1     varchar(64),    -- 参数
    c_oper_arg2     varchar(64),    -- 参数
    c_oper_arg3     varchar(64),    -- 参数
    c_oper_arg4     varchar(64),    -- 参数
    c_oper_arg5     varchar(64),    -- 参数
    c_oper_arg6     varchar(64),    -- 参数
    c_oper_arg7     varchar(64),    -- 参数
    c_oper_arg8     varchar(64),    -- 参数
    c_oper_arg9     varchar(64)     -- 参数
);

-- 渠道账户信息
drop table if exists tbl_channel_account;
create table tbl_channel_account (
    i_caid      integer,        -- 渠道账户ID
    c_user      varchar(32),    -- 用户名
    i_channel   tinyint,        -- 渠道：0-淘宝 1-微信 2-支付宝
    c_nick      varchar(32),    -- 昵称
    i_gender    tinyint,        -- 性别：0-女 1-男 2-人妖
    c_phone     varchar(20),    -- 电话
    c_address   varchar(100),   -- 地址
    c_zipcode   varchar(10),    -- 邮编
    t_birth     date            -- 生日
);

-- 游戏价格管理
drop table if exists tbl_game_rent_price;
create table tbl_game_rent_price (
    i_gid   integer,
    i_type  tinyint,    -- 租赁类型：0-A租，1-B租
    i_price decimal(4, 2)
);

