-- 指定商品租赁结算
delimiter //
drop function if exists fn_update_commodity_statement //
create function fn_update_commodity_statement (
    oid       integer,  -- 订单ID
    csn       integer   -- 商品序列号
)
returns decimal(9, 2)
begin
    declare di_price    decimal(9, 2)   default 0.00;
    declare di_count    integer         default 0;
    declare dt_end      datetime        default null;
    declare dt_begin    datetime        default null;
    declare di_times    integer         default 0;
    declare di_money    decimal(9, 2)   default 0.00;


    select i_price, i_count, t_begin, t_end
      into di_price, di_count, dt_begin, dt_end
      from tbl_commodity
     where i_oid = oid
       and i_csn = csn;

    if dt_end is not null then
        set di_times = timestampdiff(second, dt_begin, dt_end) - 20 * 60; -- 优惠20分钟
        if di_times <= 0 then
            set di_money = 0.00;
        else
            set di_times = ceil(di_times / 60 / 60); -- hours
            set di_times = ceil(di_times / 12); -- times
            if di_times < 2 then -- at least one day
                set di_times = 2;
            end if;

            set di_money = di_times * (di_price / 2) * di_count;
        end if;
    end if;

    return di_money;
end //
delimiter ;
