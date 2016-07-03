delete from tbl_instruction where i_inst = (conv('00002004', 16, 10) + 0);
insert into tbl_instruction values((conv('00002004', 16, 10) + 0), 'sp', 2, "sp_query_game_account_rent(?, ?, $caid)");

-- 查询游戏
delimiter //
drop procedure if exists sp_query_game_account_rent //
create procedure sp_query_game_account_rent (
    out i_code  integer,
    out c_desc  mediumblob,
    in  caid    integer     -- null：所有账号；非空：指定账号
)
begin
    if caid is null then
        call sp_query_game_account_rent_all(i_code, c_desc);
    else
        call sp_query_game_account_rent_by_caid(i_code, c_desc, caid);
    end if;
    set c_desc = convert(c_desc using utf8);
end //
delimiter ;
