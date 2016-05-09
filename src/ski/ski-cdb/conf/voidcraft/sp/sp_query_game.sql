delete from tbl_instruction where i_inst = (conv(00002003, 16, 10) + 0);
insert into tbl_instruction values((conv(00002003, 16, 10) + 0), 'sp', 2, "sp_query_game(?, ?, '$gid')");

-- 查询游戏
delimiter //
drop procedure if exists sp_query_game // 
create procedure sp_query_game (
    out i_code  integer,
    out c_desc  mediumblob,
    in  gid     integer -- null：所有游戏；其他：指定游戏ID
)
begin
    if gid is null then
        call sp_query_game_all(i_code, c_desc);
    else
        call sp_query_game_by_gid(i_code, c_desc, gid);
    end if;

    set c_desc = convert(c_desc using utf8);
end //
delimiter ;
