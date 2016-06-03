delete from tbl_instruction where i_inst = (conv(00002406, 16, 10) + 0);
insert into tbl_instruction values((conv(00002406, 16, 10) + 0), 'sp', 2, "sp_update_order(?, ?, $oid, $platform, $caid, '$create')");

-- 更新游戏
delimiter //
drop procedure if exists sp_update_order // 
create procedure sp_update_order (
    out i_code          integer,
    out c_desc          mediumblob,
    in  oid             integer,        -- 订单ID
    in  platform        tinyint,        -- 平台类型：0-淘宝 1-微信
    in  caid            integer,        -- 渠道账户ID
    in  create_time     datetime        -- 创建时间
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
            i_caid,
            t_create
        ) values (
            di_oid,
            platform,
            caid,
            ifnull(create_time, now())
        );
    else
        set di_oid = oid;

        select count(1)
          into di_count
          from tbl_order
         where i_oid = di_oid;

        if di_count <= 0 then
            insert into tbl_order (
                i_oid,
                i_platform,
                i_caid,
                t_create
            ) values (
                di_oid,
                platform,
                caid,
                ifnull(create_time, now())
            );
        else
            if platform is not null then
                update tbl_order o
                   set o.i_platform = platform
                 where o.i_oid = di_oid;
            end if;
            if caid is not null then
                update tbl_order o
                   set o.i_caid = caid
                 where o.i_oid = di_oid;
            end if;
            if create_time is not null then
                update tbl_order o
                   set o.t_create = create_time
                 where o.i_oid = di_oid;
            end if;
        end if;
    end if;

    set i_code = 0;
    set c_desc = null;
end //
delimiter ;
