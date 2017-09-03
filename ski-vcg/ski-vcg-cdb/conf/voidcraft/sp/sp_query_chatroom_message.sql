delete from tbl_instruction where i_inst = (conv('00002013', 16, 10) + 0);
insert into tbl_instruction values((conv('00002013', 16, 10) + 0), 'sp', 2, "sp_query_chatroom_message(?, ?, $crid, $mid, $count, '$time')");

-- 查询订单
delimiter //
drop procedure if exists sp_query_chatroom_message //
create procedure sp_query_chatroom_message (
    out i_code  integer,
    out c_desc  mediumblob,
    in  crid    integer,
    in  mid     integer,
    in  _count  integer,
    in  _time   datetime
)
begin
    declare di_count integer    default 1000;
    declare dt_time  datetime   default '1000-01-01 00:00:00';

    if _count is not null then
        set di_count = _count;
    end if;
    if _time is not null then
        set dt_time  = _time;
    end if;

    if crid is null then
        call sp_query_chatroom_message_all(i_code, c_desc, di_count, dt_time);
    else
        if mid is null then
            call sp_query_chatroom_message_by_crid(i_code, c_desc, crid, di_count, dt_time);
        else
            call sp_query_chatroom_message_by_crid_mid(i_code, c_desc, crid, mid);
        end if;
    end if;
end //
delimiter ;
