delete from tbl_instruction where i_inst = (conv(00002006, 16, 10) + 0);
insert into tbl_instruction values((conv(00002006, 16, 10) + 0), 'sp', 2, "sp_query_product(?, ?, $pid)");

-- 查询游戏
delimiter //
drop procedure if exists sp_query_product // 
create procedure sp_query_product (
    out i_code  integer,
    out c_desc  mediumblob,
    in  pid     integer
)
begin
    if pid is null then
        call sp_query_product_all(i_code, c_desc);
    else
        call sp_query_product_by_gaid(i_code, c_desc, pid);
    end if;
    set c_desc = convert(c_desc using utf8);
end //
delimiter ;
