delimiter // 
drop procedure if exists sp_query_order_item_all //   
create procedure sp_query_order_item_all (
    out i_code  integer,
    out c_desc  mediumblob
)  
begin  
    declare i_oid           integer         default -1;
    declare i_oisn          integer         default -1;
    declare t_oper_time     datetime        default null;
    declare i_oper_type     integer         default -1;
    declare c_remark        varchar(64)     default null;
    declare c_oper_arg0     varchar(64)     default null;
    declare c_oper_arg1     varchar(64)     default null;
    declare c_oper_arg2     varchar(64)     default null;
    declare c_oper_arg3     varchar(64)     default null;
    declare c_oper_arg4     varchar(64)     default null;
    declare c_oper_arg5     varchar(64)     default null;
    declare c_oper_arg6     varchar(64)     default null;
    declare c_oper_arg7     varchar(64)     default null;
    declare c_oper_arg8     varchar(64)     default null;
    declare c_oper_arg9     varchar(64)     default null;

    declare done        integer default 0;
    declare rs          cursor for
                        select oi.i_oid, oi.i_oisn, oi.t_oper_time, oi.i_oper_type, oi.c_remark, oi.c_oper_arg0, oi.c_oper_arg1, oi.c_oper_arg2, oi.c_oper_arg3, oi.c_oper_arg4, oi.c_oper_arg5, oi.c_oper_arg6, oi.c_oper_arg7, oi.c_oper_arg8, oi.c_oper_arg9
                          from tbl_order_item oi
                         order by oi.i_oid desc, oi.i_oisn desc;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_oid, i_oisn, t_oper_time, i_oper_type, c_remark, c_oper_arg0, c_oper_arg1, c_oper_arg2, c_oper_arg3, c_oper_arg4, c_oper_arg5, c_oper_arg6, c_oper_arg7, c_oper_arg8, c_oper_arg9;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_oid, 10, 16),
                '\t',
                conv(i_oisn, 10, 16),
                '\t',
                ifnull(t_oper_time, ''), -- must be not null
                '\t',
                ifnull(conv(i_oper_type, 10, 16), ''),
                '\t',
                ifnull(c_remark, ''),
                '\t',
                ifnull(c_oper_arg0, ''),
                '\t',
                ifnull(c_oper_arg1, ''),
                '\t',
                ifnull(c_oper_arg2, ''),
                '\t',
                ifnull(c_oper_arg3, ''),
                '\t',
                ifnull(c_oper_arg4, ''),
                '\t',
                ifnull(c_oper_arg5, ''),
                '\t',
                ifnull(c_oper_arg6, ''),
                '\t',
                ifnull(c_oper_arg7, ''),
                '\t',
                ifnull(c_oper_arg8, ''),
                '\t',
                ifnull(c_oper_arg9, '')
        );

        fetch rs into i_oid, i_oisn, t_oper_time, i_oper_type, c_remark, c_oper_arg0, c_oper_arg1, c_oper_arg2, c_oper_arg3, c_oper_arg4, c_oper_arg5, c_oper_arg6, c_oper_arg7, c_oper_arg8, c_oper_arg9;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
