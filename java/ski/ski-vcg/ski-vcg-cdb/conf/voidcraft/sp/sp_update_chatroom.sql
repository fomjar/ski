delete from tbl_instruction where i_inst = (conv('00002411', 16, 10) + 0);
insert into tbl_instruction values((conv('00002411', 16, 10) + 0), 'sp', 2, "sp_update_chatroom(?, ?, $crid, '$name', '$create')");

-- 更新游戏
delimiter //
drop procedure if exists sp_update_chatroom //
create procedure sp_update_chatroom (
    out i_code      integer,
    out c_desc      mediumblob,
    in  crid        integer,
    in  name        varchar(64),
    in  _create     datetime
)
begin
    declare di_crid     integer default -1;
    declare di_count    integer default -1;

    if crid is null then
        select count(1)
          into di_count
          from tbl_chatroom;

        if di_count = 0 then
            set di_crid = 1;
        else
            select max(i_crid)
              into di_crid
              from tbl_chatroom;

            set di_crid = di_crid + 1;
        end if;

        insert into tbl_chatroom (
            i_crid,
            c_name,
            t_create
        ) values (
            di_crid,
            name,
            ifnull(_create, now())
        );
    else
        set di_crid = crid;

        select count(1)
          into di_count
          from tbl_chatroom
         where i_crid = di_crid;

        if di_count <= 0 then
            insert into tbl_chatroom (
                i_crid,
                c_name,
                t_create
            ) values (
                di_crid,
                name,
                ifnull(_create, now())
            );
        else
            if name is not null then
                update tbl_chatroom cr
                   set cr.c_name = name
                 where cr.i_crid = di_crid;
            end if;
            if _create is not null then
                update tbl_chatroom cr
                   set cr.t_create = _create
                 where cr.i_crid = di_crid;
            end if;
        end if;
    end if;

    set i_code = 0;
    set c_desc = conv(di_crid, 10, 16);
end //
delimiter ;
