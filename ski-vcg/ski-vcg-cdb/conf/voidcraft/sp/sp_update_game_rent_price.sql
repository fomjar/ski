delete from tbl_instruction where i_inst = (conv('00002408', 16, 10) + 0);
insert into tbl_instruction values((conv('00002408', 16, 10) + 0), 'sp', 2, "sp_update_game_rent_price(?, ?, $gid, $type, $price)");

-- 更新游戏
delimiter //
drop procedure if exists sp_update_game_rent_price //
create procedure sp_update_game_rent_price (
    out i_code  integer,
    out c_desc  mediumblob,
    in  gid     integer,
    in  type    tinyint,
    in  price   decimal(4, 2)
)
begin
    declare di_count    integer default -1;

    if gid is null then
        set i_code = -1;
        set c_desc = 'illegal arguments, gid must be not null';
    elseif type is null then
        set i_code = -1;
        set c_desc = 'illegal arguments, type must be not null';
    else
        select count(1)
          into di_count
          from tbl_game_rent_price
         where i_gid = gid
           and i_type = type;

        if di_count = 0 then
            insert into tbl_game_rent_price (
                i_gid,
                i_type,
                i_price
            ) values (
                gid,
                type,
                price
            );
        else
            if price is not null then
                update tbl_game_rent_price
                   set i_price = price
                 where i_gid = gid
                   and i_type = type;
            end if;
        end if;
        set i_code = 0;
        set c_desc = null;
    end if;
end //
delimiter ;
