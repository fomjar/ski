-- 指定商品租赁结算
delimiter //
drop function if exists fn_update_commodity_statement //
create function fn_update_commodity_statement (
    oid       integer,  -- 订单ID
    csn       integer   -- 商品序列号
)
returns integer
begin
    declare i_code      integer         default -1;
    declare c_desc      varchar(64)     default null;
    declare di_paid     integer         default -1;
    declare di_price    decimal(9, 2)   default 0.00;
    declare dt_end      datetime        default null;
    declare dt_begin    datetime        default null;
    declare di_times    integer         default 0;
    declare di_money    decimal(9, 2)   default 0.00;

    select pam.i_paid
      into di_paid
      from tbl_platform_account_map pam, tbl_order o
     where pam.i_caid = o.i_caid
       and o.i_oid = oid;

    select i_price, t_begin, t_end
      into di_price, dt_begin, dt_end
      from tbl_commodity
     where i_oid = oid
       and i_csn = csn;

    -- 至少算24小时，之后不满12小时算12小时
    set di_times = ceil(timestampdiff(second, dt_begin, dt_end) / 60 / 60 / 12);
    if di_times < 2 then
        set di_times = 2;
    end if;

    set di_money = di_times * (di_price / 2);

    update tbl_commodity
       set i_expense = di_money
     where i_oid = oid
       and i_csn = csn;

    call sp_apply_platform_account_money(
        i_code,
        c_desc,
        di_paid,
        concat('【消费结算】订单商品：', conv(oid, 10, 16), ':', conv(csn, 10, 16)),
        0,
        di_money
    );

    return 0;
end //
delimiter ;
