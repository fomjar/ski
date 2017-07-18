delimiter //
drop procedure if exists sp_query_notification_all //
create procedure sp_query_notification_all (
    out i_code  integer,
    out c_desc  mediumblob
)  
begin  
    declare i_nid       integer     default -1;
    declare i_caid      integer     default -1;
    declare c_content   varchar(64) default null;
    declare t_create    datetime    default null;

    declare done        integer default 0;
    declare rs          cursor for
                        select n.i_nid, n.i_caid, n.c_content, n.t_create
                          from tbl_notification n
                         order by n.t_create desc;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_nid, i_caid, c_content, t_create;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_nid, 10, 16),
                '\t',
                conv(i_caid, 10, 16),
                '\t',
                ifnull(c_content, ''),
                '\t',
                ifnull(t_create, '')
        );

        fetch rs into i_nid, i_caid, c_content, t_create;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
