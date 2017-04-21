delete from tbl_instruction where i_inst = (conv('00002014', 16, 10) + 0);
insert into tbl_instruction values((conv('00002014', 16, 10) + 0), 'sp', 2, "sp_query_game_account_rent_history(?, ?, $gaid)");

-- 查询游戏
delimiter //
drop procedure if exists sp_query_game_account_rent_history //
create procedure sp_query_game_account_rent_history (
    out i_code  integer,
    out c_desc  mediumblob,
    in  gaid    integer
)
begin
    call sp_query_game_account_rent_history_by_gaid(i_code, c_desc, gaid);
end //
delimiter ;
