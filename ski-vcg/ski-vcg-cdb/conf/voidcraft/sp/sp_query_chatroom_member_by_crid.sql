delimiter //
drop procedure if exists sp_query_chatroom_member_by_crid //
create procedure sp_query_chatroom_member_by_crid (
    out i_code  integer,
    out c_desc  mediumblob,
    in  crid    integer
)  
begin  
    declare i_crid      integer     default -1;
    declare i_member    integer     default -1;

    declare done        integer default 0;
    declare rs          cursor for
                        select cm.i_crid, cm.i_member
                          from tbl_chatroom_member cm
                         where cm.i_crid = crid;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_crid, i_member;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_crid, 10, 16),
                '\t',
                conv(i_member, 10, 16)
        );

        fetch rs into i_crid, i_member;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
