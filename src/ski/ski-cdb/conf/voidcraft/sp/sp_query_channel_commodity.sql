delete from tbl_instruction where i_inst = (conv('00002010', 16, 10) + 0);
insert into tbl_instruction values((conv('00002010', 16, 10) + 0), 'sp', 2, "sp_query_channel_commodity(?, ?, $cid)");

-- 查询订单
delimiter //
drop procedure if exists sp_query_channel_commodity //
create procedure sp_query_order (
    out i_code  integer,
    out c_desc  mediumblob,
    in  cid     integer
)
begin
    if cid is null then
        call sp_query_channel_commodity_all(i_code, c_desc);
    else
        call sp_query_channel_commodity_by_cid(i_code, c_desc, cid);
    end if;
end //
delimiter ;
