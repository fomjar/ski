delete from tbl_instruction where i_inst = (conv(00002406, 16, 10) + 0);
insert into tbl_instruction values((conv(00002406, 16, 10) + 0), 'sp', 2, "sp_update_order(?, ?, $oid, $platform, $caid, $oisn, '$oper_time', $oper_type, $oper_object, $money, '$remark')");

-- 更新游戏
delimiter //
drop procedure if exists sp_update_game // 
create procedure sp_update_game (
    out i_code          integer,
    out c_desc          mediumblob,
    in  oid             integer,        -- 订单ID
    in  platform        tinyint,        -- 平台类型：0-淘宝 1-微信 2-京东
    in  caid            integer,        -- 渠道账户ID
    in  oisn            integer,
    in  oper_time       datetime,
    in  oper_type       integer,        -- 操作类型，0-购买，1-充值，2-起租，3-退租，4-停租，5-续租，6-换租，7-送券
    in  oper_object     integer,
    in  money           decimal(8, 2),
    in  remark          varchar(64)
)
begin
    declare di_oid      integer default -1;
    declare di_oisn     integer default -1;
    declare di_count    integer default -1;

    if oid is null then
        select count(1)
          into di_count
          from tbl_order;

        if di_count = 0 then
            set di_oid = 1;
        else
            select max(i_oid)
              into di_oid
              from tbl_order;

            set di_oid = di_oid + 1;
        end if;

        insert into tbl_order (
            i_oid,
            i_platform,
            i_caid
        ) values (
            di_oid,
            platform,
            caid
        );
    else
        set di_oid = oid;

        select count(1)
          into di_count
          from tbl_game
         where i_oid = di_oid;

        if di_count <= 0 then
            insert into tbl_order (
                i_oid,
                i_platform,
                i_caid
            ) values (
                di_oid,
                platform,
                caid
            );
        else
            if platform is not null then
                update tbl_order
                   set i_platform = platform
                 where i_oid = di_oid;
            end if;
            if caid is not null then
                update tbl_order
                   set i_caid = caid
                 where i_oid = di_oid;
            end if;
        end if;
    end if;

    if oisn is null
        and oper_time is null
        and oper_type is null
        and oper_object is null
        and money is null
        and remark is null then
        -- do nothing
        set di_oisn = 1;
    elseif oisn is null then
        set di_oisn = 1;
        insert into tbl_order_item (
            i_oid,
            i_oisn,
            t_oper_time,
            i_oper_type,
            i_oper_object,
            i_money,
            c_remark
        ) values (
            di_oid,
            di_oisn,
            ifnull(oper_time, now()),
            oper_type,
            oper_object,
            money,
            remark
        );
    else
        select count(1)
          into di_count
          from tbl_order_item
         where i_oid = di_oid
           and i_oisn = di_oisn;

        if di_count <= 0 then
            set di_oisn = 1;
            insert into tbl_order_item (
                i_oid,
                i_oisn,
                t_oper_time,
                i_oper_type,
                i_oper_object,
                i_money,
                c_remark
            ) values (
                di_oid,
                di_oisn,
                ifnull(oper_time, now()),
                oper_type,
                oper_object,
                money,
                remark
            );
        else
            if oper_time is not null then
                update tbl_order_item
                   set t_oper_time = oper_time
                 where i_oid = di_oid
                   and i_oisn = di_oisn;
            end if;
            if oper_type is not null then
                update tbl_order_item
                   set i_oper_type = oper_type
                 where i_oid = di_oid
                   and i_oisn = di_oisn;
            end if;
            if oper_object is not null then
                update tbl_order_item
                   set i_oper_object = oper_object
                 where i_oid = di_oid
                   and i_oisn = di_oisn;
            end if;
            if money is not null then
                update tbl_order_item
                   set i_money = money
                 where i_oid = di_oid
                   and i_oisn = di_oisn;
            end if;
            if remark is not null then
                update tbl_order_item
                   set c_remark = remark
                 where i_oid = di_oid
                   and i_oisn = di_oisn;
            end if;
        end if;
    end if;

    set i_code = 0;
    set c_desc = null;
end //
delimiter ;
