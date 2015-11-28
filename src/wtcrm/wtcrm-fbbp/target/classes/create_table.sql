drop table if exists tbl_cmd_map;
create table tbl_cmd_map (
    c_cmd   varchar(255),
    c_mod   char(5),      --st:执行sql语句 sp:执行存储过程
    c_sql   varchar(255), --格式：先出参，后入参，变量使用$打头
    i_out   tinyint       --出参个数
);

insert into tbl_cmd_map values('test-st', 'st', 2, 'select c_cmd, c_mod from tbl_cmd_map where i_out = $out');
