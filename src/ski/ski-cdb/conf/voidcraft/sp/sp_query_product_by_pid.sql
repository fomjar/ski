delimiter // 
drop procedure if exists sp_query_product_by_pid //   
create procedure sp_query_product_by_pid (
    out i_code  integer,
    out c_desc  mediumblob,
    in  pid     integer
)  
begin
    declare i_pid       integer default -1;
    declare i_prod_type integer default -1;
    declare i_prod_inst integer default -1;

    declare done        integer default 0;
    declare rs          cursor for
                        select p.i_pid, p.i_prod_type, p.i_prod_inst
                          from tbl_product p
                         where p.i_pid = pid
                         order by p.i_pid;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_pid, i_prod_type, i_prod_inst;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_pid, 10, 16),
                '\t',
                conv(i_prod_type, 10, 16),
                '\t',
                conv(i_prod_inst, 10, 16)
        );

        fetch rs into i_pid, i_prod_type, i_prod_inst;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
