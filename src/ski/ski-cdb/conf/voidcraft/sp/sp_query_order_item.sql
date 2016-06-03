delete from tbl_instruction where i_inst = (conv(00002007, 16, 10) + 0);
insert into tbl_instruction values((conv(00002007, 16, 10) + 0), 'sp', 2, "sp_query_order_item(?, ?, $oid)");

-- 查询订单
delimiter //
drop procedure if exists sp_query_order_item // 
create procedure sp_query_order_item (
    out i_code  integer,
    out c_desc  mediumblob,
    in  oid     integer
)
begin
    if oid is null then
        call sp_query_order_item_all(i_code, c_desc);
    else
        call sp_query_order_item_by_oisn(i_code, c_desc, oid);
    end if;

    set c_desc = convert(c_desc using utf8);
end //
delimiter ;
