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
    declare di_balance  decimal(9, 2) default 0.00;
    declare di_coupon   decimal(9, 2) default 0.00;

    select count(1)
      into di_count
      from tbl_platform_account
     where i_paid in (paid_from, paid_to);

    if paid_to = paid_from then
        set i_code = 2;
        set c_desc = 'illegal arguments, paid_to must be different from paid_from';
    elseif di_count < 2 then
        set i_code = 2;
        set c_desc = 'illegal arguments, paid_to or paid_from does not exist';
    else
        select sum(i_balance)
          into di_balance
          from tbl_platform_account
         where i_paid in (paid_from, paid_to);
        update tbl_platform_account
           set i_balance = di_balance
         where i_paid = paid_to;
        
        select sum(i_coupon)
          into di_coupon
          from tbl_platform_account
         where i_paid in (paid_from, paid_to);
        update tbl_platform_account
           set i_coupon = di_coupon
         where i_paid = paid_to;

        update tbl_platform_account_map
           set i_paid = paid_to
         where i_paid = paid_from;

        delete from tbl_platform_account
         where i_paid = paid_from;

        set i_code = 0;
    end if;
end //
delimiter ;
