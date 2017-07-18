delete from tbl_instruction where i_inst = (conv('0000200C', 16, 10) + 0);
insert into tbl_instruction values((conv('0000200C', 16, 10) + 0), 'sp', 2, "sp_query_tag(?, ?, '$type')");

-- 查询订单
delimiter //
drop procedure if exists sp_query_tag //
create procedure sp_query_tag (
    out i_code  integer,
    out c_desc  mediumblob,
    in  type    tinyint
)
begin
    if type is null then
        call sp_query_tag_all(i_code, c_desc);
    else
        call sp_query_tag_by_type(i_code, c_desc, type);
    end if;
end //
delimiter ;
