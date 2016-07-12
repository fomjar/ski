-- 修改租赁实例状态
delimiter //
drop procedure if exists sp_update_commodity_rent //
create procedure sp_update_commodity_rent (
    out i_code      integer,
    out c_desc      mediumblob,
    in  oid         integer,        -- 订单ID
    in  csn         integer,        -- 商品序列号
    in  remark      varchar(64),    -- 备注
    in  price       decimal(9, 2),  -- 单价
    in  count       integer,        -- 数量
    in  _begin      datetime,       -- 购买/租用开始时间
    in  _end        datetime,       -- 购买/租用结束时间
    in  expense     decimal(9, 2),  -- 商品费用
    in  arg0        varchar(64),    -- 参数0
    in  arg1        varchar(64),    -- 参数1
    in  arg2        varchar(64),    -- 参数2
    in  arg3        varchar(64),    -- 参数3
    in  arg4        varchar(64),    -- 参数4
    in  arg5        varchar(64),    -- 参数5
    in  arg6        varchar(64),    -- 参数6
    in  arg7        varchar(64),    -- 参数7
    in  arg8        varchar(64),    -- 参数8
    in  arg9        varchar(64)     -- 参数9
)
begin
    declare di_caid     integer         default -1;
    declare dc_arg0     varchar(64)     default null;
    declare dc_arg1     varchar(64)     default null;
    declare di_paid     integer         default -1;
    declare di_money    decimal(9, 2)   default 0.00;

    select i_caid
      into di_caid
      from tbl_order
     where i_oid = oid;

    if _begin is not null then
        call sp_update_game_account_rent(i_code,
                c_desc,
                conv(arg0, 16, 10),
                (case arg1 when 'A' then 0 when 'B' then 1 else -1 end),
                di_caid,
                1);
    end if;

    if _end is not null then
        select c_arg0, c_arg1
          into dc_arg0, dc_arg1
          from tbl_commodity
         where i_oid = oid
           and i_csn = csn;

        -- 更新账号状态
        call sp_update_game_account_rent(i_code,
                c_desc,
                conv(dc_arg0, 16, 10),
                (case dc_arg1 when 'A' then 0 when 'B' then 1 else -1 end),
                di_caid,
                0);

        if i_code = 0 then
            -- 结算计算
            select fn_update_commodity_statement(oid, csn)
              into di_money;
            
            -- 更新订单商品金额
            update tbl_commodity
               set i_expense = di_money
             where i_oid = oid
               and i_csn = csn;

            select i_paid
              into di_paid
              from tbl_platform_account_map
             where i_caid = di_caid;

            -- 更新用户金额
            call sp_apply_platform_account_money (
                i_code,
                c_desc,
                di_paid,
                concat('【消费结算】订单商品：', conv(oid, 10, 16), ':', conv(csn, 10, 16)),
                0,
                di_money
            );
        end if;
    end if;
end //
delimiter ;
