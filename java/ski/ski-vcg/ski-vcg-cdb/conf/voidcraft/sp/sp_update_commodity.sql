delete from tbl_instruction where i_inst = (conv('00002407', 16, 10) + 0);
insert into tbl_instruction values((conv('00002407', 16, 10) + 0), 'sp', 2, "sp_update_commodity(?, ?, $oid, $csn, '$remark', $price, $count, '$begin', '$end', $expense, '$arg0', '$arg1', '$arg2', '$arg3', '$arg4', '$arg5', '$arg6', '$arg7', '$arg8', '$arg9')");

-- 更新游戏
delimiter //
drop procedure if exists sp_update_commodity //
create procedure sp_update_commodity (
    out i_code  integer,
    out c_desc  mediumblob,
    in  oid     integer,        -- 订单ID
    in  csn     integer,
    in  remark  varchar(64),
    in  price   decimal(9, 2),
    in  count   integer,
    in  _begin  datetime,
    in  _end    datetime,
    in  expense decimal(9, 2),
    in  arg0    varchar(64),
    in  arg1    varchar(64),
    in  arg2    varchar(64),
    in  arg3    varchar(64),
    in  arg4    varchar(64),
    in  arg5    varchar(64),
    in  arg6    varchar(64),
    in  arg7    varchar(64),
    in  arg8    varchar(64),
    in  arg9    varchar(64)
)
begin
    declare di_csn      integer default -1;
    declare di_count    integer default -1;
    declare di_caid     integer default -1;

    if oid is null then
        set i_code = -1;
        set c_desc = 'illegal arguments, oid must be not null';
    else
        select count(1)
          into di_count
          from tbl_order
         where i_oid = oid;

        if di_count = 0 then
            set i_code = -1;
            set c_desc = 'order not exist';
        else
            if csn is null then
                select count(1)
                  into di_count
                  from tbl_commodity
                 where i_oid = oid;

                if di_count = 0 then
                    set di_csn = 1;
                else
                    select max(i_csn)
                      into di_csn
                      from tbl_commodity
                     where i_oid = oid;
                    set di_csn = di_csn + 1;
                end if;
                insert into tbl_commodity (
                    i_oid,
                    i_csn,
                    c_remark,
                    i_price,
                    i_count,
                    t_begin,
                    t_end,
                    i_expense,
                    c_arg0,
                    c_arg1,
                    c_arg2,
                    c_arg3,
                    c_arg4,
                    c_arg5,
                    c_arg6,
                    c_arg7,
                    c_arg8,
                    c_arg9
                ) values (
                    oid,
                    di_csn,
                    remark,
                    price,
                    ifnull(count, 1),
                    _begin,
                    _end,
                    expense,
                    arg0,
                    arg1,
                    arg2,
                    arg3,
                    arg4,
                    arg5,
                    arg6,
                    arg7,
                    arg8,
                    arg9
                );
            else
                set di_csn = csn;

                select count(1)
                  into di_count
                  from tbl_commodity
                 where i_oid = oid
                   and i_csn = di_csn;

                if di_count = 0 then
                    insert into tbl_commodity (
                        i_oid,
                        i_csn,
                        c_remark,
                        i_price,
                        i_count,
                        t_begin,
                        t_end,
                        i_expense,
                        c_arg0,
                        c_arg1,
                        c_arg2,
                        c_arg3,
                        c_arg4,
                        c_arg5,
                        c_arg6,
                        c_arg7,
                        c_arg8,
                        c_arg9
                    ) values (
                        oid,
                        di_csn,
                        remark,
                        price,
                        ifnull(count, 1),
                        _begin,
                        _end,
                        expense,
                        arg0,
                        arg1,
                        arg2,
                        arg3,
                        arg4,
                        arg5,
                        arg6,
                        arg7,
                        arg8,
                        arg9
                    );
                else
                    if remark is not null then
                        update tbl_commodity
                           set c_remark = remark
                         where i_oid = oid
                           and i_csn = di_csn;
                    end if;
                    if price is not null then
                        update tbl_commodity
                           set i_price = price
                         where i_oid = oid
                           and i_csn = di_csn;
                    end if;
                    if count is not null then
                        update tbl_commodity
                           set i_count = count
                         where i_oid = oid
                           and i_csn = di_csn;
                    end if;
                    if _begin is not null then
                        update tbl_commodity
                           set t_begin = _begin
                         where i_oid = oid
                           and i_csn = di_csn;
                    end if;
                    if _end is not null then
                        update tbl_commodity
                           set t_end = _end
                         where i_oid = oid
                           and i_csn = di_csn;
                        
                        select i_caid
                          into di_caid
                          from tbl_order
                         where i_oid = oid;
                        select fn_check_and_close_order(di_caid);
                    end if;
                    if expense is not null then
                        update tbl_commodity
                           set i_expense = expense
                         where i_oid = oid
                           and i_csn = di_csn;
                    end if;
                    if arg0 is not null then
                        update tbl_commodity
                           set c_arg0 = arg0
                         where i_oid = oid
                           and i_csn = di_csn;
                    end if;
                    if arg1 is not null then
                        update tbl_commodity
                           set c_arg1 = arg1
                         where i_oid = oid
                           and i_csn = di_csn;
                    end if;
                    if arg2 is not null then
                        update tbl_commodity
                           set c_arg2 = arg2
                         where i_oid = oid
                           and i_csn = di_csn;
                    end if;
                    if arg3 is not null then
                        update tbl_commodity
                           set c_arg3 = arg3
                         where i_oid = oid
                           and i_csn = di_csn;
                    end if;
                    if arg4 is not null then
                        update tbl_commodity
                           set c_arg4 = arg4
                         where i_oid = oid
                           and i_csn = di_csn;
                    end if;
                    if arg5 is not null then
                        update tbl_commodity
                           set c_arg5 = arg5
                         where i_oid = oid
                           and i_csn = di_csn;
                    end if;
                    if arg6 is not null then
                        update tbl_commodity
                           set c_arg6 = arg6
                         where i_oid = oid
                           and i_csn = di_csn;
                    end if;
                    if arg7 is not null then
                        update tbl_commodity
                           set c_arg7 = arg7
                         where i_oid = oid
                           and i_csn = di_csn;
                    end if;
                    if arg8 is not null then
                        update tbl_commodity
                           set c_arg8 = arg8
                         where i_oid = oid
                           and i_csn = di_csn;
                    end if;
                    if arg9 is not null then
                        update tbl_commodity
                           set c_arg9 = arg9
                         where i_oid = oid
                           and i_csn = di_csn;
                    end if;
                end if;
            end if;

            call sp_update_commodity_rent(i_code, c_desc, oid, di_csn, remark, price, count, _begin, _end, expense, arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9);
        end if;

        if i_code is null then
            set i_code = 0;
        end if;
        if i_code = 0 then
            set c_desc = conv(di_csn, 10, 16);
        end if;
    end if;
end //
delimiter ;


-- 指定商品租赁结算
delimiter //
drop function if exists fn_check_and_close_order //
create function fn_check_and_close_order (
    caid    integer
)
returns integer
begin
    declare di_count    integer default 0;

    select count(1)
      into di_count
      from tbl_order o, tbl_commodity c
     where o.i_caid = caid
       and o.i_oid = c.i_oid
       and c.t_end is null;

    if di_count = 0 then
        update tbl_order
           set t_close = now()
         where i_caid = caid
           and t_close is null;
    end if;

    return 0;
end //
delimiter ;

