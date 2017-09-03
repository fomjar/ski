delete from tbl_instruction where i_inst = (conv('0000240F', 16, 10) + 0);
insert into tbl_instruction values((conv('0000240F', 16, 10) + 0), 'sp', 2, "sp_update_access_record(?, ?, $caid, '$remote', '$local', '$time')");

-- 更新游戏
delimiter //
drop procedure if exists sp_update_access_record //
create procedure sp_update_access_record (
    out i_code      integer,
    out c_desc      mediumblob,
    in  caid        integer,        -- 渠道用户
    in  remote      varchar(250),   -- 远程主机
    in  local       varchar(250),   -- 本地主机和资源
    in  time        datetime        -- 访问事件
)
begin

    insert into tbl_access_record (
        i_caid,
        c_remote,
        c_local,
        t_time
    ) values (
        caid,
        remote,
        local,
        ifnull(time, now())
    );
    
    set i_code = 0;
    set c_desc = null;
end //
delimiter ;
