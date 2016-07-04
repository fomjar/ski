delete from tbl_instruction where i_inst = (conv('00002104', 16, 10) + 0);
insert into tbl_instruction values((conv('00002104', 16, 10) + 0), 'sp', 2, "sp_apply_platform_account_money(?, ?, $paid, '$remark', $type, $money)");

-- 查询游戏
delimiter //
drop procedure if exists sp_apply_platform_account_money //
create procedure sp_apply_platform_account_money (
    out i_code      integer,
    out c_desc      mediumblob,
    in  paid        integer,        -- 平台账户ID
    in  remark      varchar(64),    -- 备注
    in  type        tinyint,        -- 0-消费，1-充值，2-充券
    in  money       decimal(9, 2)   -- 金额
)
begin
    declare di_count    integer         default -1;
    declare di_caid     integer         default -1;
    declare di_cash     decimal(9, 2)   default 0.00;
    declare di_coupon   decimal(9, 2)   default 0.00;

    select count(1)
      into di_count
      from tbl_platform_account
     where i_paid = paid;

    if di_count <= 0 then
        set i_code = 2;
        set c_desc = 'illegal arguments, paid does not exist';
    elseif remark is null then
        set i_code = 2;
        set c_desc = 'illegal arguments, remark is null';
    elseif type is null then
        set i_code = 2;
        set c_desc = 'illegal arguments, type is null';
    elseif money is null then
        set i_code = 2;
        set c_desc = 'illegal arguments, money is null';
    else
        select i_cash, i_coupon
          into di_cash, di_coupon
          from tbl_platform_account
         where i_paid = paid;

        if type = 0 then -- cost
            insert into tbl_platform_account_money (
                i_paid,
                c_remark,
                t_time,
                i_type,
                i_base,
                i_money
            ) values (
                paid,
                remark,
                now(),
                type,
                di_cash + di_coupon,
                money
            );

            if (di_coupon >= money) then
                set di_coupon = di_coupon - money;
            else
                set di_cash = di_cash - (money - di_coupon);
                set di_coupon = 0.00;
            end if;

            update tbl_platform_account
               set i_cash = di_cash
                 , i_coupon = di_coupon
             where i_paid = paid;
        elseif type = 1 then -- cash
            insert into tbl_platform_account_money (
                i_paid,
                c_remark,
                t_time,
                i_type,
                i_base,
                i_money
            ) values (
                paid,
                remark,
                now(),
                type,
                di_cash,
                money
            );

            set di_cash = di_cash + money;
            update tbl_platform_account
               set i_cash = di_cash
             where i_paid = paid;
        elseif type = 2 then -- coupon
            insert into tbl_platform_account_money (
                i_paid,
                c_remark,
                t_time,
                i_type,
                i_base,
                i_money
            ) values (
                paid,
                remark,
                now(),
                type,
                di_coupon,
                money
            );

            set di_coupon = di_coupon + money;
            update tbl_platform_account
               set i_coupon = di_coupon
             where i_paid = paid;
        end if;

        set i_code = 0;
    end if;
end //
delimiter ;
