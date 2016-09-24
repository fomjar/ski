delete from tbl_instruction where i_inst = (conv('00002414', 16, 10) + 0);
insert into tbl_instruction values((conv('00002414', 16, 10) + 0), 'sp', 2, "sp_update_chatroom_message(?, ?, $crid, $member, $type, \"$message\", '$time')");

-- 更新游戏
delimiter //
drop procedure if exists sp_update_chatroom_message //
create procedure sp_update_chatroom_message (
    out i_code      integer,
    out c_desc      mediumblob,
    in  crid        integer,
    in  member      integer,
    in  _type       tinyint,
    in  message     text,
    in  _time       datetime
)
begin

    insert into tbl_chatroom_message (
        i_crid,
        i_member,
        i_type,
        c_message,
        t_time
    ) values (
        crid,
        member,
        _type,
        message,
        ifnull(_time, now())
    );

    set i_code = 0;
    set c_desc = null;
end //
delimiter ;
