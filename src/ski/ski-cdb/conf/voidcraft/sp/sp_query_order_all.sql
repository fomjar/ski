delimiter // 
drop procedure if exists sp_query_order_all //   
create procedure sp_query_order_all (
    out i_code  integer,
    out c_desc  mediumblob
)  
begin  
    declare i_oid       integer     default -1;
    declare i_platform  integer     default -1;
    declare i_caid      integer     default -1;
    declare t_create    datetime    default null;

    declare done        integer default 0;
    declare rs          cursor for
                        select o.i_oid, o.i_platform, o.i_caid, o.t_create
                          from tbl_order o
                         order by o.t_create desc;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_oid, i_platform, i_caid, t_create;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_oid, 10, 16),
                '\t',
                conv(i_platform, 10, 16),
                '\t',
                conv(i_caid, 10, 16),
                '\t',
                ifnull(t_create, '')
        );

        fetch rs into i_oid, i_platform, i_caid, t_create;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
