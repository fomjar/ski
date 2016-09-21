delete from tbl_instruction where i_inst = (conv('00002013', 16, 10) + 0);
insert into tbl_instruction values((conv('00002013', 16, 10) + 0), 'sp', 2, "sp_query_chatroom_message(?, ?, $crid)");

-- 查询订单
delimiter //
drop procedure if exists sp_query_chatroom_message //
create procedure sp_query_chatroom_message (
    out i_code  integer,
    out c_desc  mediumblob,
    in  crid    integer
)
begin
    if crid is null then
        call sp_query_chatroom_message_all(i_code, c_desc);
    else
        call sp_query_chatroom_message_by_crid(i_code, c_desc, crid);
    end if;
end //
delimiter ;
