delimiter //
drop procedure if exists sp_query_access_record_all //
create procedure sp_query_access_record_all (
    out i_code  integer,
    out c_desc  mediumblob
)  
begin
    declare i_caid      integer;        -- 渠道用户
    declare c_remote    varchar(250);   -- 远程主机
    declare c_local     varchar(250);   -- 本地主机和资源
    declare t_time      datetime;       -- 访问事件

    declare done            integer default 0;
    declare rs              cursor for
                            select a.i_caid, a.c_remote, a.c_local, a.t_time
                              from tbl_access_record a
                             order by a.t_time desc;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_caid, c_remote, c_local, t_time;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_caid, 10, 16),
                '\t',
                ifnull(c_remote, ''),
                '\t',
                ifnull(c_local, ''),
                '\t',
                ifnull(t_time, '')
        );

        fetch rs into i_caid, c_remote, c_local, t_time;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
