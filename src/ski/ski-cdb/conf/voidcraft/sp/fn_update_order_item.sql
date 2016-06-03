-- 修改租赁实例状态
delimiter //
drop function if exists fn_update_order_item // 
create function fn_update_order_item (
    oid         integer,        -- 平台订单ID
    oisn        integer,
    oper_type   integer,        -- 操作类型，0-购买，1-充值，2-起租，3-退租，4-停租，5-续租，6-换租，7-赠券
    oper_object integer,
    oper_arg0   varchar(64),    -- 参数
    oper_arg1   varchar(64),    -- 参数
    oper_arg2   varchar(64)     -- 参数
)
returns integer
begin
    declare di_code integer     default -1;
    declare dc_desc mediumblob  default null;
    declare di_caid integer     default -1;

    if oper_type is not null then
        select i_caid
          into di_caid
          from tbl_order
         where i_oid = oid;

        if oper_type = 0 then
            set di_code = 0; -- do nothing
        elseif oper_type = 1 then
            set di_code = 0; -- do nothing
        elseif oper_type = 2 then
            call sp_update_game_account_rent(di_code,
                    dc_desc,
                    oper_object,
                    (case oper_arg0 when 'A' then 0 when 'B' then 1 else -1 end),
                    di_caid,
                    1);
        elseif oper_type = 3 then
            call sp_update_game_account_rent(di_code,
                    dc_desc,
                    oper_object,
                    (case oper_arg0 when 'A' then 0 when 'B' then 1 else -1 end),
                    di_caid,
                    0);
        elseif oper_type = 4 then
            set di_code = 0; -- do nothing
        elseif oper_type = 5 then
            set di_code = 0; -- do nothing
        elseif oper_type = 6 then
            call sp_update_game_account_rent(di_code,
                    dc_desc,
                    oper_object,
                    (case oper_arg0 when 'A' then 0 when 'B' then 1 else -1 end),
                    di_caid,
                    1);
            call sp_update_game_account_rent(di_code,
                    dc_desc,
                    conv(oper_arg1, 16, 10),
                    (case oper_arg2 when 'A' then 0 when 'B' then 1 else -1 end),
                    di_caid,
                    0);
        elseif oper_type = 7 then
            set di_code = 0; -- do nothing
        end if;

    end if;

    return di_code;
end //
delimiter ;
