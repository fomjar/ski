delete from tbl_instruction where i_inst = (conv('00002414', 16, 10) + 0);
insert into tbl_instruction values((conv('00002414', 16, 10) + 0), 'sp', 2, "sp_update_chatroom_message(?, ?, $crid, $mid, $member, $type, '$time', \"$message\", \"$arg0\", \"$arg1\", \"$arg2\", \"$arg3\", \"$arg4\")");

-- 更新游戏
delimiter //
drop procedure if exists sp_update_chatroom_message //
create procedure sp_update_chatroom_message (
    out i_code      integer,
    out c_desc      mediumblob,
    in  crid        integer,
    in  mid         integer,
    in  member      integer,
    in  _type       tinyint,
    in  _time       datetime,
    in  message     longtext,
    in  arg0        varchar(64),
    in  arg1        varchar(64),
    in  arg2        varchar(64),
    in  arg3        varchar(64),
    in  arg4        varchar(64)
)
begin

    declare di_mid      integer     default -1;
    declare di_count    integer     default -1;

    select count(1)
      into di_count
      from tbl_chatroom_message;

    if di_count <= 0 then
        set di_mid = 1;
    else
        select max(i_mid)
          into di_mid
          from tbl_chatroom_message;

        set di_mid = di_mid + 1;
    end if;

    insert into tbl_chatroom_message (
        i_crid,
        i_mid,
        i_member,
        i_type,
        t_time,
        c_message,
        c_arg0,
        c_arg1,
        c_arg2,
        c_arg3,
        c_arg4
    ) values (
        crid,
        di_mid,
        member,
        _type,
        ifnull(_time, now()),
        message,
        arg0,
        arg1,
        arg2,
        arg3,
        arg4
    );

    set i_code = 0;
    set c_desc = null;
end //
delimiter ;
