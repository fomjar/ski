delete from tbl_instruction where i_inst = (conv('0000200F', 16, 10) + 0);
insert into tbl_instruction values((conv('0000200F', 16, 10) + 0), 'sp', 2, "sp_query_access_record(?, ?)");

-- 查询游戏
delimiter //
drop procedure if exists sp_query_access_record //
create procedure sp_query_access_record (
    out i_code  integer,
    out c_desc  mediumblob
)
begin
    call sp_query_access_record_all(i_code, c_desc);
end //
delimiter ;
