-- cdb命令映射表
drop table if exists tbl_cmd_map;
create table tbl_cmd_map (
    c_cmd   varchar(255),
    c_mod   char(5),      -- st:执行sql语句 sp:执行存储过程
    i_out   tinyint,      -- 出参个数
    c_sql   varchar(255)  -- 格式：先出参，后入参，入参使用$打头，入参名称必须与消息中的参数名称完全相同
);

-- 账户基础信息
drop table if exists tbl_account_basic;
create table tbl_account_basic (
    i_aid       integer,        -- 账户ID
    c_user      varchar(32),    -- 用户名
    c_pass      varchar(32),    -- 密码
    c_pass_temp varchar(32)     -- 临时密码
);

-- 账户详细信息
drop table if exists tbl_account_detail;
create table tbl_account_detail (
    i_aid       integer,    -- 账户ID
    t_create    datetime,   -- 创建时间
    t_birth     date        -- 出生日期
);

-- 游戏信息表
drop table if exists tbl_game;
create table tbl_game (
    i_gid           integer,        -- 游戏ID
    c_platform      varchar(16),    -- 平台：PS4、XBOX ONE、PS5、XBOX TWO、PS3、XBOX 360
    c_country       varchar(32),    -- 国家
    c_url_icon      varchar(128),   -- 图标URL
    c_url_poster    varchar(128),   -- 海报URL
    c_url_price     varchar(128),   -- 询价网址
    c_url_buy       varchar(128),   -- 采购网址
    t_sale          date,           -- 发售日期
    c_name_cns      varchar(64),    -- 简体中文名
    c_name_cnt      varchar(64),    -- 繁体中文名
    c_name_en       varchar(64),    -- 英文名
    c_name_ori      varchar(64)     -- 原始名
);

-- 账户下的游戏
drop table if exists tbl_account_game;
create table tbl_account_game (
    i_aid   integer,    -- 账户ID
    i_gid   integer     -- 游戏ID
);

-- 账户租赁状态
drop table if exists tbl_account_rent;
create table tbl_account_rent (
    i_aid       integer,    -- 账户ID
    i_rent      tinyint     -- 租赁状态
);

-- 产品列表
drop table if exists tbl_product;
create table tbl_product (
    c_pid   varchar(16),    -- 产品ID
    i_type  tinyint,        -- 实例类型：0-游戏租赁 1-游戏出售 2-主机租赁 3-主机出售
    i_inst  integer         -- 实例ID
);

-- 淘宝订单表
drop table if exists tbl_order_taobao;
create table tbl_order_taobao (
    c_toid      varchar(20),
    c_tuid      varchar(32),
    c_pid       varchar(16),
    c_tp_name   varchar(64),
    c_tp_attr   varchar(64),
    i_tp_price  decimal(7, 2),
    i_tp_count  integer,
    c_tu_name   varchar(10),
    c_tu_tel    varchar(20),
    c_tu_addr   varchar(100),
    c_tu_zip    varchar(10),
    t_create    datetime,
    i_status    tinyint
);
