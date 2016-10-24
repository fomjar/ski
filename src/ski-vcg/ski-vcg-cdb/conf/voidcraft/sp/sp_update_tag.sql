delete from tbl_instruction where i_inst = (conv('0000240B', 16, 10) + 0);
insert into tbl_instruction values((conv('0000240B', 16, 10) + 0), 'sp', 2, "sp_update_tag(?, ?, $type, $instance, '$tag')");

-- 更新游戏
delimiter //
drop procedure if exists sp_update_tag //
create procedure sp_update_tag (
    out i_code      integer,
    out c_desc      mediumblob,
    in  type        tinyint,    -- 0-game
    in  instance    integer,    -- 标记实例
    in  tag         varchar(16) -- tag值
)
begin
    insert into tbl_tag (
        i_type,
        i_instance,
        c_tag
    ) values (
        type,
        instance,
        tag
    );

    set i_code = 0;
    set c_desc = null;
end //
delimiter ;
