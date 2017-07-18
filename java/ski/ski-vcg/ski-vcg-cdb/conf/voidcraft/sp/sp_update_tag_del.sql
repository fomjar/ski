delete from tbl_instruction where i_inst = (conv('0000240C', 16, 10) + 0);
insert into tbl_instruction values((conv('0000240C', 16, 10) + 0), 'sp', 2, "sp_update_tag_del(?, ?, $type, $instance, '$tag')");

-- 更新游戏
delimiter //
drop procedure if exists sp_update_tag_del //
create procedure sp_update_tag_del (
    out i_code      integer,
    out c_desc      mediumblob,
    in  type        tinyint,    -- 0-game
    in  instance    integer,    -- 标记实例
    in  tag         varchar(16) -- tag值
)
begin
    if type is null then
        set i_code = 2;
        set c_desc = 'illegal argument, type must be not null';
    elseif instance is null then
        set i_code = 2;
        set c_desc = 'illegal argument, instance must be not null';
    elseif tag is null then
        set i_code = 2;
        set c_desc = 'illegal argument, tag must be not null';
    else
        delete from tbl_tag
         where i_type = type
           and i_instance = instance
           and c_tag = tag;

        set i_code = 0;
        set c_desc = null;
    end if;

end //
delimiter ;
