delimiter //
drop procedure if exists sp_query_chatroom_by_crid //
create procedure sp_query_chatroom_by_crid (
    out i_code  integer,
    out c_desc  mediumblob,
    in  crid    integer
)  
begin  
    declare i_crid      integer     default -1;
    declare c_name      varchar(64) default null;
    declare t_create    datetime    default null;

    declare done        integer default 0;
    declare rs          cursor for
                        select cr.i_crid, cr.c_name, cr.t_create
                          from tbl_chatroom cr
                         where cr.i_crid = crid
                         order by cr.t_create;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_crid, c_name, t_create;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_crid, 10, 16),
                '\t',
                ifnull(c_name, ''),
                '\t',
                ifnull(t_create, '')
        );

        fetch rs into i_crid, c_name, t_create;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
