delete from tbl_instruction where i_inst = (conv('00002010', 16, 10) + 0);
insert into tbl_instruction values((conv('00002010', 16, 10) + 0), 'sp', 2, "sp_query_channel_commodity(?, ?, $osn, $cid)");

-- 查询订单
delimiter //
drop procedure if exists sp_query_channel_commodity //
create procedure sp_query_channel_commodity (
    out i_code  integer,
    out c_desc  mediumblob,
    in  osn     integer,
    in  cid     integer
)
begin
    if osn is null and cid is null then
        call sp_query_channel_commodity_all(i_code, c_desc);
    elseif osn is null then
        call sp_query_channel_commodity_by_osn(i_code, c_desc, osn);
    elseif cid is null then
        call sp_query_channel_commodity_by_cid(i_code, c_desc, cid);
    else
        set i_code = 2;
        set c_desc = 'illegal argument, osn and cid can not be both not null';
    end if;
end //
delimiter ;
