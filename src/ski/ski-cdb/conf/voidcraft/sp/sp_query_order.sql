delete from tbl_instruction where i_inst = (conv(00002006, 16, 10) + 0);
insert into tbl_instruction values((conv(00002006, 16, 10) + 0), 'sp', 2, "sp_query_order(?, ?, $caid)");

-- 查询订单
delimiter //
drop procedure if exists sp_query_order // 
create procedure sp_query_order (
    out i_code  integer,
    out c_desc  mediumblob,
    in  caid    integer
)
begin
    if caid is null then
        call sp_query_order_all(i_code, c_desc);
    else
        call sp_query_order_by_caid(i_code, c_desc, caid);
    end if;

    set c_desc = convert(c_desc using utf8);
end //
delimiter ;
