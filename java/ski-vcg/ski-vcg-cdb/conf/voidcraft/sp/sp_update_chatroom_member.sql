delete from tbl_instruction where i_inst = (conv('00002412', 16, 10) + 0);
insert into tbl_instruction values((conv('00002412', 16, 10) + 0), 'sp', 2, "sp_update_chatroom_member(?, ?, $crid, $member)");

-- 更新游戏
delimiter //
drop procedure if exists sp_update_chatroom_member //
create procedure sp_update_chatroom_member (
    out i_code      integer,
    out c_desc      mediumblob,
    in  crid        integer,
    in  member      integer
)
begin
    declare di_count    integer default -1;

    select count(1)
      into di_count
      from tbl_chatroom
     where i_crid = crid;

    if di_count <= 0 then
        set i_code = 2;
        set c_desc = 'chatroom not exist';
    else 
        select count(1)
          into di_count
          from tbl_chatroom_member
         where i_crid = crid
           and i_member = member;
        
        if di_count <= 0 then
            insert into tbl_chatroom_member (
                i_crid,
                i_member
            ) values (
                crid,
                member
            );
        end if;

        set i_code = 0;
        set c_desc = null;
    end if;
end //
delimiter ;
