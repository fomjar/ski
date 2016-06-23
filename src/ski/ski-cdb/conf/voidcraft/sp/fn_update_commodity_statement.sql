-- 指定商品租赁结算
delimiter //
drop function if exists fn_update_commodity_statement // 
create function fn_update_commodity_statement (
    oid       integer,  -- 订单ID
    csn       integer   -- 商品序列号
)
returns integer
begin
    declare di_paid     integer         default -1;
    declare di_balance  decimal(9, 2)   default 0.00;
    declare di_coupon   decimal(9, 2)   default 0.00;

    declare di_price    decimal(9, 2)   default 0.00;
    declare dt_end      datetime        default null;
    declare dt_begin    datetime        default null;
    declare di_times    integer         default 0;
    declare di_expense  decimal(9, 2)   default 0.00;

    select pam.i_paid
      into di_paid
      from tbl_platform_account_map pam, tbl_order o
     where pam.i_caid = o.i_caid
       and o.i_oid = oid;

    select i_balance, i_coupon
      into di_balance, di_coupon
      from tbl_platform_account
     where i_paid = di_paid;

    select i_price, t_begin, t_end
      into di_price, dt_begin, dt_end
      from tbl_commodity
     where i_oid = oid
       and i_csn = csn;

    -- 至少算24小时，之后不满12小时算12小时
    set di_times = ceil(timestampdiff(second, dt_end, dt_begin) / 60 / 60 / 12);
    if di_times < 2 then
        set di_times = 2;
    end if;

    set di_expense = di_times * (di_price / 2);

    if (di_coupon >= di_expense) then
        set di_coupon = di_coupon - di_expense;
    else
        set di_balance = di_balance - (di_expense - di_coupon);
        set di_coupon = 0.00;
    end if;

    update tbl_commodity
       set i_expense = di_expense
     where i_oid = oid
       and i_csn = csn;

    update tbl_platform_account
       set i_balance = di_balance
         , i_coupon = di_coupon
     where i_paid = di_paid;

    return 0;
end //
delimiter ;
