delete from tbl_instruction where i_inst = (conv(00002003, 16, 10) + 0);
insert into tbl_instruction values((conv(00002003, 16, 10) + 0), 'sp', 2, "sp_query_game_account_game(?, ?, $gaid, $gid)");

-- 查询游戏
delimiter //
drop procedure if exists sp_query_game_account_game //
create procedure sp_query_game_account_game (
    out i_code  integer,
    out c_desc  mediumblob,
    in  gaid    integer,
    in  gid     integer
)
begin
    if gaid is null and gid is null then
        call sp_query_game_account_game_all(i_code, c_desc);
    elseif gaid is not null then
        call sp_query_game_account_game_by_gaid(i_code, c_desc, gaid);
    elseif gid is not null then
        call sp_query_game_account_game_by_gid(i_code, c_desc, gid);
    else
        set i_code = 2;
        set c_desc = 'can not determin which to query by, gaid or gid?';
    end if;
    set c_desc = convert(c_desc using utf8);
end //
delimiter ;
