-- cdb命令映射表
drop table if exists tbl_instruction;
create table tbl_instruction (
    i_inst  integer,
    c_mode  char(5),    -- st:执行sql语句 sp:执行存储过程
    i_out   tinyint,    -- 出参个数
    c_sql   text        -- 格式：先出参，后入参，出参以英文的?代替，入参使用$打头，入参名称必须与消息中的参数名称完全相同
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
    t_birth     date,           -- 出生日期
    i_cash      decimal(9, 2),  -- 现金（可退的）
    i_coupon    decimal(9, 2),  -- 优惠券（不可退）
    t_create    datetime        -- 创建时间
);

-- 平台账户与渠道账户的关系
drop table if exists tbl_platform_account_map;
create table tbl_platform_account_map (
    i_paid      integer,    -- 平台账户ID
    i_caid      integer     -- 渠道账户ID
);

-- 金额操作
drop table if exists tbl_platform_account_money;
create table tbl_platform_account_money (
    i_paid      integer,        -- 平台账户ID
    c_remark    varchar(64),    -- 备注
    t_time      datetime,       -- 操作时间
    i_type      tinyint,        -- 0-消费结算，1-现金操作，2-优惠券操作
    i_base      decimal(9, 2),  -- 基准值
    i_money     decimal(9, 2)   -- 变化值
);

-- 游戏账户
drop table if exists tbl_game_account;
create table tbl_game_account (
    i_gaid      integer,        -- 游戏账户ID，对应产品实例
    c_remark    varchar(64),    -- 备注
    c_user      varchar(32),    -- 用户名
    c_pass      varchar(32),    -- 密码
    c_name      varchar(32),    -- 名字/昵称
    t_birth     date,           -- 出生日期
    t_create    datetime        -- 创建时间
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
    c_name_zh_cn    varchar(64),    -- 简体中文名
    c_name_zh_hk    varchar(64),    -- 繁体中文名
    c_name_en       varchar(64),    -- 英文名
    c_name_ja       varchar(64),    -- 日文名
    c_name_ko       varchar(64),    -- 韩文名
    c_name_other    varchar(64),    -- 其他语言名
    c_platform      varchar(16),    -- 游戏和账号所属平台：PS4、XBOX ONE、PS5、XBOX TWO、PS3、XBOX 360
    c_category      varchar(64),    -- 分类(多种)
    c_language      varchar(32),    -- 语言(多种)
    c_size          varchar(16),    -- 大小
    c_vendor        varchar(64),    -- 发行商
    t_sale          date,           -- 发售日期
    c_url_icon      varchar(250),   -- 图标URL
    c_url_cover     varchar(250),   -- 封面URL
    c_url_poster    text,           -- 海报(多个)
    c_introduction  text,           -- 游戏说明
    c_version       text,           -- 版本说明
    c_vedio         text,           -- 视频脚本
    i_associator    tinyint,        -- 0-unecessary, 1-necessary
    c_play_mode     varchar(128),   -- 单人、1-n人本地同屏、在线
    c_peripheral    varchar(128),   -- 外设：摄像头、体感棒、VR头盔
    c_editor_word   text,           -- 编辑推荐
    i_ign_score     decimal(3, 2),  -- IGN评分
    c_producer      varchar(64)     -- 制作人
);

-- 订单
drop table if exists tbl_order;
create table tbl_order (
    i_oid       integer,        -- 订单ID
    i_platform  tinyint,        -- 订单来源平台类型：0-淘宝 1-微信
    i_caid      integer,        -- 渠道账户ID
    t_open      datetime,       -- 打开时间
    t_close     datetime        -- 关闭时间
);

-- 商品
drop table if exists tbl_commodity;
create table tbl_commodity (
    i_oid       integer,        -- 订单ID
    i_csn       integer,        -- 商品序列号
    c_remark    varchar(64),    -- 备注
    i_price     decimal(9, 2),  -- 单价
    i_count     integer,        -- 数量
    t_begin     datetime,       -- 购买/租用开始时间
    t_end       datetime,       -- 购买/租用结束时间
    i_expense   decimal(9, 2),  -- 商品费用
    c_arg0      varchar(64),    -- 参数0
    c_arg1      varchar(64),    -- 参数1
    c_arg2      varchar(64),    -- 参数2
    c_arg3      varchar(64),    -- 参数3
    c_arg4      varchar(64),    -- 参数4
    c_arg5      varchar(64),    -- 参数5
    c_arg6      varchar(64),    -- 参数6
    c_arg7      varchar(64),    -- 参数7
    c_arg8      varchar(64),    -- 参数8
    c_arg9      varchar(64)     -- 参数9
);

-- 渠道账户信息
drop table if exists tbl_channel_account;
create table tbl_channel_account (
    i_caid      integer,        -- 渠道账户ID
    c_user      varchar(32),    -- 用户名
    i_channel   tinyint,        -- 渠道：0-淘宝 1-微信 2-支付宝 3-PSN
    c_name      varchar(32),    -- 姓名
    i_gender    tinyint,        -- 性别：0-女 1-男 2-人妖
    c_phone     varchar(20),    -- 电话
    c_address   varchar(100),   -- 地址
    c_zipcode   varchar(10),    -- 邮编
    t_birth     date,           -- 生日
    t_create    datetime        -- 创建时间
);

-- 游戏价格管理
drop table if exists tbl_game_rent_price;
create table tbl_game_rent_price (
    i_gid   integer,
    i_type  tinyint,    -- 租赁类型：0-A租，1-B租
    i_price decimal(4, 2)
);

-- TAG管理
drop table if exists tbl_tag;
create table tbl_tag (
    i_type      tinyint,    -- 0-game，1-chatroom
    i_instance  integer,    -- 标记实例
    c_tag       varchar(16) -- tag值
);

-- 工单管理
drop table if exists tbl_ticket;
create table tbl_ticket (
    i_tid       integer,        -- 工单编号
    i_caid      integer,        -- 渠道用户ID
    t_open      datetime,       -- 打开时间
    t_close     datetime,       -- 关闭时间
    i_type      tinyint,        -- 0-退款申请，1-意见建议，2-通知提醒，3-预约预定，4-备忘纪要
    c_title     varchar(64),    -- 工单标题
    c_content   varchar(512),   -- 工单内容
    i_state     tinyint,        -- 工单状态：0-open, 1-close, 2-cancel
    c_result    varchar(64)     -- 处理结果
);

-- 通知
drop table if exists tbl_notification;
create table tbl_notification (
    i_nid       integer,        -- 通知编号
    i_caid      integer,        -- 被通知的用户
    c_content   varchar(64),    -- 通知内容
    t_create    datetime        -- 创建通知的时间
);

-- 访问记录
drop table if exists tbl_access_record;
create table tbl_access_record (
    i_caid      integer,        -- 渠道用户
    c_remote    varchar(250),   -- 远程主机
    c_local     varchar(250),   -- 本地主机和资源
    t_time      datetime        -- 访问事件
);

-- 渠道商品
drop table if exists tbl_channel_commodity;
create table tbl_channel_commodity (
    i_osn           integer,        -- 操作序列号
    i_cid           integer,        -- 商品ID(对应的)
    t_time          datetime,       -- 生成时间
    i_channel       tinyint,        -- 渠道类型: 0-淘宝 1-微信 2-支付宝
    c_item_url      varchar(250),   -- 商品引用链接
    c_item_cover    varchar(250),   -- 商品封面链接
    c_item_name     varchar(250),   -- 商品名称
    c_item_remark   varchar(250),   -- 商品备注
    i_item_sold     integer,        -- 商品售出数量
    c_item_price    varchar(32),    -- 商品价格(或范围)
    i_express_price decimal(4, 2),  -- 快递价格
    c_shop_url      varchar(250),   -- 店铺(级别)链接
    c_shop_name     varchar(64),    -- 店铺名称
    c_shop_owner    varchar(64),    -- 店铺卖家
    c_shop_rate     varchar(64),    -- 店铺级别
    c_shop_score    varchar(64),    -- 店铺评分
    c_shop_addr     varchar(100)    -- 店铺地址
);

-- 聊天室
drop table if exists tbl_chatroom;
create table tbl_chatroom (
    i_crid      integer,        -- 聊天室编号
    c_name      varchar(64),    -- 聊天室名称
    t_create    datetime        -- 创建时间
);

-- 聊天室成员
drop table if exists tbl_chatroom_member;
create table tbl_chatroom_member (
    i_crid      integer,    -- 聊天室编号
    i_member    integer     -- 聊天室成员(paid)
);

-- 聊天室消息
drop table if exists tbl_chatroom_message;
create table tbl_chatroom_message (
    i_crid      integer,    -- 聊天室编号
    i_member    integer,    -- 发送成员
    i_type      tinyint,    -- 0-纯文字，1-图片，2-语音
    c_message   text,       -- 消息内容(编码成字符串)
    t_time      datetime    -- 发送时间
);

