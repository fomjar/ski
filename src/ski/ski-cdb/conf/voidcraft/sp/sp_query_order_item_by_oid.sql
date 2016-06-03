delimiter // 
drop procedure if exists sp_query_order_item_by_oid //   
create procedure sp_query_order_item_by_oid (
    out i_code  integer,
    out c_desc  mediumblob,
    in  oid     integer
)  
begin  
    declare i_oid           integer         default -1;
    declare i_oisn          integer         default -1;
    declare t_oper_time     datetime        default null;
    declare i_oper_type     integer         default -1;
    declare i_oper_object   integer         default -1;
    declare c_remark        varchar(64)     default null;
    declare c_oper_arg0     varchar(64)     default null;
    declare c_oper_arg1     varchar(64)     default null;
    declare c_oper_arg2     varchar(64)     default null;

    declare done        integer default 0;
    declare rs          cursor for
                        select oi.i_oid, oi.i_oisn, oi.t_oper_time, oi.i_oper_type, oi.i_oper_object, oi.c_remark, oi.c_oper_arg0, oi.c_oper_arg1, oi.c_oper_arg2
                          from tbl_order_item oi
                         where oi.i_oid = oid
                         order by oi.i_oisn desc;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_oid, i_oisn, t_oper_time, i_oper_type, i_oper_object, c_remark, c_oper_arg0, c_oper_arg1, c_oper_arg2;
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
                ifnull(conv(i_oper_object, 10, 16), ''),
                '\t',
                ifnull(c_remark, ''),
                '\t',
                ifnull(c_oper_arg0, ''),
                '\t',
                ifnull(c_oper_arg1, ''),
                '\t',
                ifnull(c_oper_arg2, '')
        );

        fetch rs into i_oid, i_oisn, t_oper_time, i_oper_type, i_oper_object, c_remark, c_oper_arg0, c_oper_arg1, c_oper_arg2;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
