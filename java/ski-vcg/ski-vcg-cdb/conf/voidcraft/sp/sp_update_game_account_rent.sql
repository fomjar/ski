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
    declare di_count        integer default -1;

    select count(1)
      into di_count
      from tbl_game_account_rent
     where i_gaid = gaid
       and i_type = type;

    if di_count = 0 then
        insert into tbl_game_account_rent (
            i_gaid,
            i_type,
            i_caid,
            i_state,
            t_change
        ) values (
            gaid,
            type,
            caid,
            state,
            now()
        );
    else
        update tbl_game_account_rent
          set i_caid = caid
            , i_state = state
            , t_change = now()
        where i_gaid = gaid
          and i_type = type;
    end if;

    insert into tbl_game_account_rent_history (
        i_gaid,
        i_type,
        i_caid,
        i_state,
        t_change
    ) values (
        gaid,
        type,
        caid,
        state,
        now()
    );
    set i_code = 0;
    set c_desc = null;
end //
delimiter ;
