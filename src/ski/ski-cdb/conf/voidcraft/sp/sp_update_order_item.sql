delete from tbl_instruction where i_inst = (conv(00002407, 16, 10) + 0);
insert into tbl_instruction values((conv(00002407, 16, 10) + 0), 'sp', 2, "sp_update_order_item(?, ?, $oid, $oisn, '$oper_time', $oper_type, $oper_object, '$remark', '$oper_arg0', '$oper_arg1', '$oper_arg2')");

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
    in  oper_object     integer,
    in  remark          varchar(64),
    in  oper_arg0       varchar(64),
    in  oper_arg1       varchar(64),
    in  oper_arg2       varchar(64)
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
                    i_oper_object,
                    c_remark,
                    c_oper_arg0,
                    c_oper_arg1,
                    c_oper_arg2
                ) values (
                    oid,
                    di_oisn,
                    ifnull(oper_time, now()),
                    oper_type,
                    oper_object,
                    remark,
                    oper_arg0,
                    oper_arg1,
                    oper_arg2
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
                        i_oper_object,
                        c_remark,
                        c_oper_arg0,
                        c_oper_arg1,
                        c_oper_arg2
                    ) values (
                        oid,
                        di_oisn,
                        ifnull(oper_time, now()),
                        oper_type,
                        oper_object,
                        remark,
                        oper_arg0,
                        oper_arg1,
                        oper_arg2
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
                    if oper_object is not null then
                        update tbl_order_item
                           set i_oper_object = oper_object
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
                end if;
            end if;

            select fn_update_order_item(oid, oisn, oper_type, oper_object, oper_arg0, oper_arg1, oper_arg2)
              into i_code;
            set c_desc = null;
        end if;
    end if;
end //
delimiter ;
