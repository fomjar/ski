delete from tbl_instruction where i_inst = (conv('00002403', 16, 10) + 0);
insert into tbl_instruction values((conv('00002403', 16, 10) + 0), 'sp', 2, "sp_update_game_account_game(?, ?, $gaid, $gid)");

-- 更新订单
delimiter //
drop procedure if exists sp_update_game_account_game //
create procedure sp_update_game_account_game (
    out i_code      integer,
    out c_desc      mediumblob,
    in  gaid        integer,        -- 游戏账户ID
    in  gid         integer
)
begin
    declare di_count    integer default -1;

    select count(1)
      into di_count
      from tbl_game_account
     where i_gaid = gaid;

    if di_count = 0 then
        set i_code = 2;
        set c_desc = 'no such a gaid in system';
    else
        select count(1)
          into di_count
          from tbl_game
         where i_gid = gid;

        if di_count = 0 then
            set i_code = 2;
            set c_desc = 'no such a gid in system';
        else
            select count(1)
              into di_count
              from tbl_game_account_game
             where i_gaid = gaid
               and i_gid = gid;

            if di_count = 0 then
                insert into tbl_game_account_game (
                    i_gaid,
                    i_gid
                ) values (
                    gaid,
                    gid
                );
            end if;

            set i_code = 0;
            set c_desc = null;
        end if;
    end if;
end //
delimiter ;
