delete from tbl_instruction where i_inst = (conv('00002103', 16, 10) + 0);
insert into tbl_instruction values((conv('00002103', 16, 10) + 0), 'sp', 2, "sp_apply_platform_account_merge(?, ?, $paid_to, $paid_from)");

-- 查询游戏
delimiter //
drop procedure if exists sp_apply_platform_account_merge // 
create procedure sp_apply_platform_account_merge (
    out i_code      integer,
    out c_desc      mediumblob,
    in  paid_to     integer,
    in  paid_from   integer
)
begin
    declare di_count    integer default -1;
    declare di_caid     integer default -1;

    declare done        integer default 0;
    declare rs          cursor for
                        select pam.i_caid
                          from tbl_platform_account_map pam
                         where pam.i_paid = paid_from;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    select count(1)
      into di_count
      from tbl_platform_account
     where i_paid in (paid_from, paid_to);

    if di_count < 2 then
        set i_code = 2;
        set c_desc = 'illegal arguments, paid_to or paid_from does not exist';
    else
        /* 打开游标 */
        open rs;  
        /* 逐个取出当前记录i_gaid值*/
        fetch rs into di_caid;
        /* 遍历数据表 */
        while (done = 0) do
            update tbl_platform_account_map
               set i_paid = paid_to
             where i_caid = di_caid;

            fetch rs into di_caid;
        end while;
        /* 关闭游标 */
        close rs;

        update tbl_platform_account
           set i_balance = (
                    select sum(i_balance)
                      from tbl_platform_account
                     where i_paid in (paid_from, paid_to)
                )
         where i_paid = paid_to;
        update tbl_platform_account
           set i_coupon = (
                    select sum(i_coupon)
                      from tbl_platform_account
                     where i_paid in (paid_from, paid_to)
                )
         where i_paid = paid_to;

        delete from tbl_platform_account
         where i_paid = paid_from;

        set i_code = 0;
    end if;
end //
delimiter ;
