-- cdb命令映射表
drop table if exists tbl_cmd_map;
create table tbl_cmd_map (
    i_cmd   integer,
    c_mod   char(5),      -- st:执行sql语句 sp:执行存储过程
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

-- 平台账户拥有的游戏账户
drop table if exists tbl_platform_account_relationship;
create table tbl_platform_account_relationship (
    i_paid  integer,        -- 平台账户ID
    c_caid      varchar(64) -- 渠道账户ID
);

-- 游戏账户
drop table if exists tbl_game_account;
create table tbl_game_account (
    i_gaid      integer,        -- 游戏账户ID
    c_user      varchar(32),    -- 用户名
    c_pass_cur  varchar(32),    -- 当前密码
    c_pass_a    varchar(32),    -- 密码A
    c_pass_b    varchar(32)     -- 密码B
);

-- 游戏账户租赁状态
drop table if exists tbl_game_account_rent;
create table tbl_game_account_rent (
    i_gaid  integer,    -- 游戏账户ID
    i_rent  tinyint     -- 租赁状态
);

-- 游戏账户下的游戏
drop table if exists tbl_game_account_game;
create table tbl_game_account_game (
    i_gaid  integer,    -- 游戏账户ID
    i_gid   integer     -- 游戏ID
);

-- 游戏信息表
drop table if exists tbl_game;
create table tbl_game (
    i_gid           integer,        -- 游戏ID
    c_platform      varchar(16),    -- 游戏和账号所属平台：PS4、XBOX ONE、PS5、XBOX TWO、PS3、XBOX 360
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

-- 产品
drop table if exists tbl_product;
create table tbl_product (
    i_pid   integer,    -- 产品ID
    i_type  integer,    -- 产品类型：0-PS4游戏A租赁 1-PS4游戏B租赁 2-PS4游戏出售 3-PS4主机租赁 4-PS4主机出售
    i_inst  integer     -- 实例类型，如游戏ID等
);

-- 订单
drop table if exists tbl_order;
create table tbl_order (
    i_poid      integer,        -- 平台订单ID
    i_coid      integer,        -- 渠道订单ID
    i_channel   tinyint,        -- 渠道类型：0-淘宝 1-微信 2-京东
    c_caid      varchar(64),    -- 渠道账户ID
    t_place     datetime        -- 下单时间
);

-- 订单产品信息
drop table if exists tbl_order_product;
create table tbl_order_product (
    i_poid              integer,        -- 平台订单ID
    i_pid               integer,        -- 产品ID
    i_type              integer,        -- 实例类型
    c_name              varchar(64),    -- 名称
    i_price             decimal(7, 2),  -- 单价
    i_state             tinyint,        -- 订单产品状态：0-未发货 1-已发货 2-已提货 3-已退货
    c_take_code         varchar(64),    -- 提取码
    i_take_inst         integer,        -- 提货实例，如游戏账户ID
    t_return_apply      datetime,       -- 退货申请时间
    t_return_done       datetime,       -- 退货完成时间
    i_refund            decimal(7, 2)   -- 退款金额
    c_reserve1          varchar(64),    -- 保留信息1
    c_reserve2          varchar(64),    -- 保留信息2
    c_reserve3          varchar(64)     -- 保留信息3
);

-- 平台账户流水
drop table if exists tbl_journal_game_account;
create table tbl_journal_game_account (
    i_gaid          integer,        -- 游戏账号
    c_caid          varchar(64),    -- 渠道账户ID
    t_change        datetime,       -- 变化时间
    i_state_before  tinyint,        -- 变化前的状态
    i_state_after   tinyint,        -- 变化后的状态
    i_cause         tinyint         -- 成因：0-用户操作 1-系统操作 2-人工维护操作
);

-- 渠道账户信息
drop table if exists tbl_channel_account;
create table tbl_channel_account (
    c_caid      varchar(64),    -- 渠道账户ID
    c_user      varchar(32),    -- 用户名
    i_channel   tinyint,        -- 渠道：0-淘宝 1-微信
    c_nick      varchar(32),    -- 昵称
    i_gender    tinyint,        -- 性别：0-女 1-男 2-人妖
    c_mobile    varchar(20),    -- 电话
    c_address   varchar(100),   -- 地址
    c_zipcode   varchar(10),    -- 邮编
    t_birth     date            -- 生日
);
