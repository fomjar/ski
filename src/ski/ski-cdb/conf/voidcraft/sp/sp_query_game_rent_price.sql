delete from tbl_instruction where i_inst = (conv('00002008', 16, 10) + 0);
insert into tbl_instruction values((conv('00002008', 16, 10) + 0), 'sp', 2, "sp_query_game_rent_price(?, ?)");

-- 查询游戏
delimiter //
drop procedure if exists sp_query_game_rent_price // 
create procedure sp_query_game_rent_price (
    out i_code  integer,
    out c_desc  mediumblob
)
begin
    call sp_query_game_rent_price_all(i_code, c_desc);

    set c_desc = convert(c_desc using utf8);
end //
delimiter ;
