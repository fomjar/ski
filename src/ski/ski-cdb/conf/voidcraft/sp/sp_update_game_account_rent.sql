delete from tbl_instruction where i_inst = (conv(00002406, 16, 10) + 0);
insert into tbl_instruction values((conv(00002406, 16, 10) + 0), 'sp', 2, "sp_update_game_account_rent(?, ?, $pid, $gaid, $caid, $state)");

-- 更新产品
delimiter //
drop procedure if exists sp_update_game_account_rent // 
create procedure sp_update_game_account_rent (
    out i_code  integer,
    out c_desc  mediumblob,
    in  pid     integer,    -- 产品ID
    in  gaid    integer,    -- 游戏账号ID
    in  caid    integer,    -- 渠道账户账户ID
    in  state   tinyint     -- 租赁状态: 0-空闲，1-租用，2-锁定
)
begin
    declare di_count    integer default -1;

    select count(1)
      into di_count
      from tbl_game_account_rent
     where i_pid = pid
       and i_gaid = gaid;

    if di_count = 0 then
        insert into tbl_game_account_rent (
            i_pid,
            i_gaid,
            i_caid,
            i_state
        ) values (
            pid,
            gaid,
            caid,
            -1
        );
        set i_code = 0;
        set c_desc = null;
    end if;

    select fn_update_game_account_rent(pid, gaid, caid, state)
      into i_code;
    set c_desc = null;
end //
delimiter ;
