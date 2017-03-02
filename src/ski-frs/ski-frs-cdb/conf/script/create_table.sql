
-- cdb命令映射表
drop table if exists tbl_instruction;
create table tbl_instruction (
    i_inst  integer,
    c_mode  char(5),    -- st:执行sql语句 sp:执行存储过程
    i_out   tinyint,    -- 出参个数
    c_sql   text        -- 格式：先出参，后入参，出参以英文的?代替，入参使用$打头，入参名称必须与消息中的参数名称完全相同
);

-- 图库
drop table if exists tbl_pic_lib;
create table tbl_pic_lib (
    i_plid  integer     auto_increment, -- 图库编号
    c_name  varchar(64),                -- 图库名称
    primary key(i_plid)
);

-- 图片
drop table if exists tbl_pic;
create table tbl_pic (
    i_pid   integer     auto_increment, -- 图片编号
    i_plid  integer,                    -- 图库编号
    c_path  text,                       -- 图片路径
    i_type  tinyint,                    -- 图片类型：0 - 大图(全图)，1 - 中图(半身)，2 - 小图(头像)
    t_time  datetime,                   -- 获取时间
    c_desc  text,                       -- 描述信息
    primary key(i_pid)
);

-- 主体
drop table if exists tbl_sub;
create table tbl_sub (
    i_sid   integer     auto_increment, -- 主体编号
    c_key   varchar(32),                -- 键
    c_val   text,                       -- 值
    primary key(i_sid)
);

-- 主体-图片
drop table if exists tbl_sub_pic;
create table tbl_sub_pic (
    i_sid   integer,    -- 主体编号
    i_pid   integer     -- 图片编号
);




