delete from tbl_instruction where i_inst = (conv(00002004, 16, 10) + 0);
insert into tbl_instruction values((conv(00002004, 16, 10) + 0), 'sp', 2, "sp_query_game_account(?, ?, '$gid', '$gaid')");

-- 查询游戏
delimiter //
drop procedure if exists sp_query_game_account // 
create procedure sp_query_game_account (
    out i_code  integer,
    out c_desc  mediumblob,
    in  gid     integer,    -- 必填，非空
    in  gaid    integer     -- null：所有账号；非空：指定账号
)
begin
    if gid is null then
        set i_code = conv(00000002, 16, 10) + 0;
        set c_desc = 'illegal args, gid must be not null';
    else
        if gaid is null then
            call sp_query_game_account_all(i_code, c_desc, gid);
        else
            call sp_query_game_account_by_gaid(i_code, c_desc, gid, gaid);
        end if;
        set c_desc = convert(c_desc using utf8);
    end if;
end //
delimiter ;
