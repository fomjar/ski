delimiter //
drop procedure if exists sp_query_chatroom_message_all //
create procedure sp_query_chatroom_message_all (
    out i_code  integer,
    out c_desc  mediumblob,
    in  _count  integer,
    in  _time   datetime
)  
begin  
    declare i_crid      integer     default -1;
    declare i_member    integer     default -1;
    declare i_type      tinyint     default -1;
    declare c_message   text        default null;
    declare t_time      datetime    default null;

    declare done        integer default 0;
    declare rs          cursor for
                        select cm.i_crid, cm.i_member, cm.i_type, cm.c_message, cm.t_time
                          from tbl_chatroom_message cm
                         where cm.t_time > _time
                         order by cm.t_time desc
                         limit _count;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_crid, i_member, i_type, c_message, t_time;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_crid, 10, 16),
                '\t',
                conv(i_member, 10, 16),
                '\t',
                conv(i_type, 10, 16),
                '\t',
                ifnull(c_message, ''),
                '\t',
                ifnull(t_time, '')
        );

        fetch rs into i_crid, i_member, i_type, c_message, t_time;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
