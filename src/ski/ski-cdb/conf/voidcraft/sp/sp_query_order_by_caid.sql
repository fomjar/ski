delimiter // 
drop procedure if exists sp_query_order_by_caid //   
create procedure sp_query_order_by_caid (
    out i_code  integer,
    out c_desc  mediumblob,
    in  caid    integer
)  
begin  
    declare i_oid           integer         default -1;
    declare i_platform      integer         default -1;
    declare i_caid          integer         default -1;
    declare i_oisn          integer         default -1;
    declare t_oper_time     datetime        default null;
    declare i_oper_type     integer         default -1;
    declare i_oper_object   integer         default -1;
    declare i_money         decimal(8, 2)   default 0;
    declare c_remark        varchar(64)     default null;

    declare done        integer default 0;
    declare rs          cursor for
                        select o.i_oid, o.i_platform, o.i_caid, oi.i_oisn, oi.t_oper_time, oi.i_oper_type, oi.i_oper_object, oi.i_money, oi.c_remark
                          from tbl_order o, tbl_order_item oi
                         where o.i_oid = oi.i_oid
                           and o.i_caid = caid
                         order by o.i_oid, oi.i_oisn;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_oid, i_platform, i_caid, i_oisn, t_oper_time, i_oper_type, i_oper_object, i_money, c_remark;
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
                conv(i_oisn, 10, 16),
                '\t',
                t_oper_time,
                '\t',
                conv(i_oper_type, 10, 16),
                '\t',
                conv(i_oper_object, 10, 16),
                '\t',
                ifnull(i_money, '0.00'),
                '\t',
                ifnull(c_remark, '')
        );

        fetch rs into i_oid, i_platform, i_caid, i_oisn, t_oper_time, i_oper_type, i_oper_object, i_money, c_remark;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
