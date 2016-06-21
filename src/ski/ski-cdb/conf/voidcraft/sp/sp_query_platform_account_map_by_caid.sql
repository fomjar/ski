delimiter // 
drop procedure if exists sp_query_platform_account_map_by_caid //   
create procedure sp_query_platform_account_map_by_caid (
    out i_code  integer,
    out c_desc  mediumblob,
    in  caid    integer
)  
begin
    declare i_paid  integer default -1; -- 平台账户ID
    declare i_caid  integer default -1; -- 渠道账户ID

    declare done    integer default 0;
    declare rs      cursor for
                    select pam.i_paid, pam.i_caid
                      from tbl_platform_account_map pam
                     where pam.i_caid = caid
                     order by pam.i_paid, pam.i_caid;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_paid, i_caid;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_paid, 10, 16),
                '\t',
                conv(i_caid, 10, 16)
        );

        fetch rs into i_paid, i_caid;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
