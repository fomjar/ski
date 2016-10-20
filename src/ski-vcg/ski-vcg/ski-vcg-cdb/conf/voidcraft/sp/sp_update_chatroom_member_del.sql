delete from tbl_instruction where i_inst = (conv('00002413', 16, 10) + 0);
insert into tbl_instruction values((conv('00002413', 16, 10) + 0), 'sp', 2, "sp_update_chatroom_member_del(?, ?, $crid, $member)");

-- 更新游戏
delimiter //
drop procedure if exists sp_update_chatroom_member_del //
create procedure sp_update_chatroom_member_del (
    out i_code      integer,
    out c_desc      mediumblob,
    in  crid        integer,
    in  member      integer
)
begin

    declare di_count integer default -1;

    select count(1)
      into di_count
      from tbl_chatroom_member
     where i_crid = crid
       and i_member = member;

    delete from tbl_chatroom_member
     where i_crid = crid
       and i_member = member;

    set i_code = 0;
    set c_desc = concat(di_count, '');
end //
delimiter ;
