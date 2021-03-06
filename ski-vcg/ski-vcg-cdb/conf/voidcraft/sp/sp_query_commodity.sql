delete from tbl_instruction where i_inst = (conv('00002007', 16, 10) + 0);
insert into tbl_instruction values((conv('00002007', 16, 10) + 0), 'sp', 2, "sp_query_commodity(?, ?)");

-- 查询订单
delimiter //
drop procedure if exists sp_query_commodity //
create procedure sp_query_commodity (
    out i_code  integer,
    out c_desc  mediumblob
)
begin
    call sp_query_commodity_all(i_code, c_desc);
end //
delimiter ;
