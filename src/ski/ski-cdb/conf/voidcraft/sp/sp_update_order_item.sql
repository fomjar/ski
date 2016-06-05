delete from tbl_instruction where i_inst = (conv(00002407, 16, 10) + 0);
insert into tbl_instruction values((conv(00002407, 16, 10) + 0), 'sp', 2, "sp_update_order_item(?, ?, $oid, $oisn, '$oper_time', $oper_type, '$remark', '$oper_arg0', '$oper_arg1', '$oper_arg2', '$oper_arg3', '$oper_arg4', '$oper_arg5', '$oper_arg6', '$oper_arg7', '$oper_arg8', '$oper_arg9')");

-- 更新游戏
delimiter //
drop procedure if exists sp_update_order_item // 
create procedure sp_update_order_item (
    out i_code          integer,
    out c_desc          mediumblob,
    in  oid             integer,        -- 订单ID
    in  oisn            integer,
    in  oper_time       datetime,
    in  oper_type       integer,        -- 操作类型，0-购买，1-充值，2-起租，3-退租，4-停租，5-续租，6-换租，7-送券
    in  remark          varchar(64),
    in  oper_arg0       varchar(64),
    in  oper_arg1       varchar(64),
    in  oper_arg2       varchar(64),
    in  oper_arg3       varchar(64),
    in  oper_arg4       varchar(64),
    in  oper_arg5       varchar(64),
    in  oper_arg6       varchar(64),
    in  oper_arg7       varchar(64),
    in  oper_arg8       varchar(64),
    in  oper_arg9       varchar(64)
)
begin
    declare di_oisn     integer default -1;
    declare di_count    integer default -1;

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
            if oisn is null then
                select count(1)
                  into di_count
                  from tbl_order_item
                 where i_oid = oid;

                if di_count = 0 then
                    set di_oisn = 1;
                else
                    select max(i_oisn)
                      into di_oisn
                      from tbl_order_item
                     where i_oid = oid;
                    set di_oisn = di_oisn + 1;
                end if;
                insert into tbl_order_item (
                    i_oid,
                    i_oisn,
                    t_oper_time,
                    i_oper_type,
                    c_remark,
                    c_oper_arg0,
                    c_oper_arg1,
                    c_oper_arg2,
                    c_oper_arg3,
                    c_oper_arg4,
                    c_oper_arg5,
                    c_oper_arg6,
                    c_oper_arg7,
                    c_oper_arg8,
                    c_oper_arg9
                ) values (
                    oid,
                    di_oisn,
                    ifnull(oper_time, now()),
                    oper_type,
                    remark,
                    oper_arg0,
                    oper_arg1,
                    oper_arg2,
                    oper_arg3,
                    oper_arg4,
                    oper_arg5,
                    oper_arg6,
                    oper_arg7,
                    oper_arg8,
                    oper_arg9
                );
            else
                select count(1)
                  into di_count
                  from tbl_order_item
                 where i_oid = oid
                   and i_oisn = oisn;

                if di_count = 0 then
                    insert into tbl_order_item (
                        i_oid,
                        i_oisn,
                        t_oper_time,
                        i_oper_type,
                        c_remark,
                        c_oper_arg0,
                        c_oper_arg1,
                        c_oper_arg2,
                        c_oper_arg3,
                        c_oper_arg4,
                        c_oper_arg5,
                        c_oper_arg6,
                        c_oper_arg7,
                        c_oper_arg8,
                        c_oper_arg9
                    ) values (
                        oid,
                        di_oisn,
                        ifnull(oper_time, now()),
                        oper_type,
                        remark,
                        oper_arg0,
                        oper_arg1,
                        oper_arg2,
                        oper_arg3,
                        oper_arg4,
                        oper_arg5,
                        oper_arg6,
                        oper_arg7,
                        oper_arg8,
                        oper_arg9
                    );
                else
                    if oper_time is not null then
                        update tbl_order_item
                           set t_oper_time = oper_time
                         where i_oid = oid
                           and i_oisn = di_oisn;
                    end if;
                    if oper_type is not null then
                        update tbl_order_item
                           set i_oper_type = oper_type
                         where i_oid = oid
                           and i_oisn = di_oisn;
                    end if;
                    if remark is not null then
                        update tbl_order_item
                           set c_remark = remark
                         where i_oid = oid
                           and i_oisn = di_oisn;
                    end if;
                    if oper_arg0 is not null then
                        update tbl_order_item
                           set c_oper_arg0 = oper_arg0
                         where i_oid = oid
                           and i_oisn = di_oisn;
                    end if;
                    if oper_arg1 is not null then
                        update tbl_order_item
                           set c_oper_arg1 = oper_arg1
                         where i_oid = oid
                           and i_oisn = di_oisn;
                    end if;
                    if oper_arg2 is not null then
                        update tbl_order_item
                           set c_oper_arg2 = oper_arg2
                         where i_oid = oid
                           and i_oisn = di_oisn;
                    end if;
                    if oper_arg3 is not null then
                        update tbl_order_item
                           set c_oper_arg3 = oper_arg3
                         where i_oid = oid
                           and i_oisn = di_oisn;
                    end if;
                    if oper_arg4 is not null then
                        update tbl_order_item
                           set c_oper_arg4 = oper_arg4
                         where i_oid = oid
                           and i_oisn = di_oisn;
                    end if;
                    if oper_arg5 is not null then
                        update tbl_order_item
                           set c_oper_arg5 = oper_arg5
                         where i_oid = oid
                           and i_oisn = di_oisn;
                    end if;
                    if oper_arg6 is not null then
                        update tbl_order_item
                           set c_oper_arg6 = oper_arg6
                         where i_oid = oid
                           and i_oisn = di_oisn;
                    end if;
                    if oper_arg7 is not null then
                        update tbl_order_item
                           set c_oper_arg7 = oper_arg7
                         where i_oid = oid
                           and i_oisn = di_oisn;
                    end if;
                    if oper_arg8 is not null then
                        update tbl_order_item
                           set c_oper_arg8 = oper_arg8
                         where i_oid = oid
                           and i_oisn = di_oisn;
                    end if;
                    if oper_arg9 is not null then
                        update tbl_order_item
                           set c_oper_arg9 = oper_arg9
                         where i_oid = oid
                           and i_oisn = di_oisn;
                    end if;
                end if;
            end if;

            select fn_update_order_item(oid, oisn, oper_type, oper_arg0, oper_arg1, oper_arg2, oper_arg3, oper_arg4, oper_arg5, oper_arg6, oper_arg7, oper_arg8, oper_arg9)
              into i_code;
            set c_desc = null;
        end if;
    end if;
end //
delimiter ;
