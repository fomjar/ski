delete from tbl_instruction where i_inst = (conv('00002404', 16, 10) + 0);
insert into tbl_instruction values((conv('00002404', 16, 10) + 0), 'sp', 2, "sp_update_game_account_rent(?, ?, $gaid, $type, $caid, $state)");

-- 更新产品
delimiter //
drop procedure if exists sp_update_game_account_rent // 
create procedure sp_update_game_account_rent (
    out i_code  integer,
    out c_desc  mediumblob,
    in  gaid    integer,    -- 游戏账号ID
    in  type    tinyint,    -- 租赁类型：0-A租，1-B租
    in  caid    integer,    -- 渠道账户账户ID
    in  state   tinyint     -- 租赁状态: 0-空闲，1-租用，2-锁定
)
begin
    select fn_update_game_account_rent(gaid, type, caid, state)
      into i_code;
    set c_desc = null;
end //
delimiter ;
