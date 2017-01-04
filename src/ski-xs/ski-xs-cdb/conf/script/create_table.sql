
-- cdb命令映射表
drop table if exists tbl_instruction;
create table tbl_instruction (
    i_inst  integer,
    c_mode  char(5),    -- st:执行sql语句 sp:执行存储过程
    i_out   tinyint,    -- 出参个数
    c_sql   text,       -- 格式：先出参，后入参，出参以英文的?代替，入参使用$打头，入参名称必须与消息中的参数名称完全相同
    primary key (i_inst)
);


-- 用户
drop table if exists tbl_user;
create table tbl_user (
    i_uid       integer,        -- 编号
    t_create    datetime,       -- 创建时间
    c_pass      varchar(16),    -- 密码
    c_phone     varchar(16),    -- 电话
    c_email     varchar(32),    -- 邮箱
    c_cover     varchar(256),   -- 封面
    c_name      varchar(32),    -- 名字
    i_gender    tinyint,        -- 性别：0-女；1-男
    primary key (i_uid)
);

-- 文件夹
drop table if exists tbl_folder;
create table tbl_folder (
    i_fid       integer,        -- 编号
    i_uid       integer,        -- 用户
    i_fsn       integer,        -- 序号
    t_create    datetime,       -- 创建时间
    c_path      varchar(256),   -- 路径：/日记/2016年/1月/
    i_type      tinyint,        -- 类型：0-系统(不可删除)，1-用户
    primary key (i_fid)
);

-- 文件夹-文章 映射
drop table if exists tbl_folder_article;
create table tbl_folder_article (
    i_fid   integer,
    i_aid   integer
);

-- 文章
drop table if exists tbl_article;
create table tbl_article (
    i_aid       integer,        -- 编号
    i_author    integer,        -- 作者
    t_create    datetime,       -- 创建时间
    t_modify    datetime,       -- 修改时间
    c_location  varchar(64),    -- 地址
    i_weather   tinyint,        -- 天气
    c_title     varchar(64),    -- 标题
    i_status    tinyint,        -- 状态：0-正常；1-回收；2-删除
    primary key (i_aid)
);

-- 段落
drop table if exists tbl_paragraph;
create table tbl_paragraph (
    i_aid   integer,    -- 文章编号
    i_pid   integer,    -- 段落编号
    i_psn   integer,    -- 段落序号
    primary key (i_pid)
);

-- 元素
drop table if exists tbl_element;
create table tbl_element (
    i_pid   integer,    -- 段落编号
    i_esn   integer,    -- 元素序号
    i_et    tinyint,    -- 元素类型：0-文字；1-图片；2-音频；3-视频
    c_ec    mediumtext  -- 元素内容
);

-- TAG
drop table if exists tbl_tag;
create table tbl_tag (
    i_tid   integer,        -- TAG编号
    i_it    integer,        -- 实例类型：0-文章
    i_io    integer,        -- 实例对象
    c_tag   varchar(256)    -- TAG内容
);

-- 用户状态
drop table if exists tbl_user_state;
create table tbl_user_state (
    i_uid   integer,        -- 用户
    c_token varchar(64),    -- 凭证
    i_state tinyint,        -- 状态：0-离线；1-在线
    t_time  datetime,       -- 变化时间
    primary key (i_uid)
);




