delimiter // 
drop procedure if exists sp_query_commodity_all //   
create procedure sp_query_commodity_all (
    out i_code  integer,
    out c_desc  mediumblob
)  
begin  
    declare i_oid       integer         default -1;
    declare i_csn       integer         default -1;
    declare c_remark    varchar(64)     default null;
    declare i_price     decimal(9, 2)   default null;
    declare i_count     integer         default 0;
    declare t_begin     datetime        default null;
    declare t_end       datetime        default null;
    declare i_expense   decimal(9, 2)   default null;
    declare c_arg0      varchar(64)     default null;
    declare c_arg1      varchar(64)     default null;
    declare c_arg2      varchar(64)     default null;
    declare c_arg3      varchar(64)     default null;
    declare c_arg4      varchar(64)     default null;
    declare c_arg5      varchar(64)     default null;
    declare c_arg6      varchar(64)     default null;
    declare c_arg7      varchar(64)     default null;
    declare c_arg8      varchar(64)     default null;
    declare c_arg9      varchar(64)     default null;

    declare done        integer default 0;
    declare rs          cursor for
                        select c.i_oid, c.i_csn, c.c_remark, c.i_price, c.i_count, c.t_begin, c.t_end, c.i_expense, c.c_arg0, c.c_arg1, c.c_arg2, c.c_arg3, c.c_arg4, c.c_arg5, c.c_arg6, c.c_arg7, c.c_arg8, c.c_arg9
                          from tbl_commodity c
                         order by c.i_oid desc, c.i_csn;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_oid, i_csn, c_remark, i_price, i_count, t_begin, t_end, i_expense, c_arg0, c_arg1, c_arg2, c_arg3, c_arg4, c_arg5, c_arg6, c_arg7, c_arg8, c_arg9;
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
                ifnull(c_remark, ''),
                '\t',
                ifnull(i_price, '0.00'),
                '\t',
                ifnull(conv(i_count, 10, 16), '0'),
                '\t',
                ifnull(t_begin, ''),
                '\t',
                ifnull(t_end, ''),
                '\t',
                ifnull(i_expense, '0.00'),
                '\t',
                ifnull(c_arg0, ''),
                '\t',
                ifnull(c_arg1, ''),
                '\t',
                ifnull(c_arg2, ''),
                '\t',
                ifnull(c_arg3, ''),
                '\t',
                ifnull(c_arg4, ''),
                '\t',
                ifnull(c_arg5, ''),
                '\t',
                ifnull(c_arg6, ''),
                '\t',
                ifnull(c_arg7, ''),
                '\t',
                ifnull(c_arg8, ''),
                '\t',
                ifnull(c_arg9, '')
        );

        fetch rs into i_oid, i_csn, c_remark, i_price, i_count, t_begin, t_end, i_expense, c_arg0, c_arg1, c_arg2, c_arg3, c_arg4, c_arg5, c_arg6, c_arg7, c_arg8, c_arg9;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
