delete from tbl_instruction where i_inst = (conv('0000200D', 16, 10) + 0);
insert into tbl_instruction values((conv('0000200D', 16, 10) + 0), 'sp', 2, "sp_query_ticket(?, ?, $type)");

-- 查询游戏
delimiter //
drop procedure if exists sp_query_ticket //
create procedure sp_query_ticket (
    out i_code  integer,
    out c_desc  mediumblob,
    in  type    tinyint -- null：所有游戏；其他：指定游戏ID
)
begin
    if type is null then
        call sp_query_ticket_all(i_code, c_desc);
    else
        call sp_query_ticket_by_type(i_code, c_desc, type);
    end if;
end //
delimiter ;
