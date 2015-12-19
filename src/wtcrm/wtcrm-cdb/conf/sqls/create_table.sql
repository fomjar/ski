drop table if exists tbl_cmd_map;
create table tbl_cmd_map (
    c_cmd   varchar(255),
    c_mod   char(5),      -- st:执行sql语句 sp:执行存储过程
    i_out   tinyint,      -- 出参个数
    c_sql   varchar(255)  -- 格式：先出参，后入参，入参使用$打头，入参名称必须与消息中的参数名称完全相同
);

drop table if exists tbl_order_taobao;
create table tbl_order_taobao (
    t_time       datetime,
    c_toid       varchar(20),
    c_fpid       varchar(16),
    c_tuid       varchar(32),
    c_tp_name    varchar(64),
    c_tp_attr    varchar(64),
    i_tp_price   decimal(7, 2),
    i_tp_count   integer,
    c_buyer_name varchar(10),
    c_buyer_tel  varchar(20),
    c_buyer_addr varchar(100),
    c_buyer_zip  varchar(10),
    i_status     tinyint
);