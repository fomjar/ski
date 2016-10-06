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
    declare i_mid       integer     default -1;
    declare i_member    integer     default -1;
    declare i_type      tinyint     default -1;
    declare t_time      datetime    default null;
    declare c_message   longtext    default null;
    declare c_arg0      varchar(64) default null;
    declare c_arg1      varchar(64) default null;
    declare c_arg2      varchar(64) default null;
    declare c_arg3      varchar(64) default null;
    declare c_arg4      varchar(64) default null;

    declare done        integer default 0;
    declare rs          cursor for
                        select cm.i_crid, cm.i_mid, cm.i_member, cm.i_type, cm.t_time, cm.c_message, cm.c_arg0, cm.c_arg1, cm.c_arg2, cm.c_arg3, cm.c_arg4
                          from tbl_chatroom_message cm
                         where cm.t_time > _time
                         order by cm.t_time desc
                         limit _count;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_crid, i_mid, i_member, i_type, t_time, c_message, c_arg0, c_arg1, c_arg2, c_arg3, c_arg4;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_crid, 10, 16),
                '\t',
                conv(i_mid, 10, 16),
                '\t',
                conv(i_member, 10, 16),
                '\t',
                conv(i_type, 10, 16),
                '\t',
                ifnull(t_time, ''),
                '\t',
                ifnull(c_message, ''),
                '\t',
                ifnull(c_arg0, ''),
                '\t',
                ifnull(c_arg1, ''),
                '\t',
                ifnull(c_arg2, ''),
                '\t',
                ifnull(c_arg3, ''),
                '\t',
                ifnull(c_arg4, '')
        );

        fetch rs into i_crid, i_mid, i_member, i_type, t_time, c_message, c_arg0, c_arg1, c_arg2, c_arg3, c_arg4;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
