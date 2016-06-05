-- 修改租赁实例状态
delimiter //
drop function if exists fn_update_game_account_rent // 
create function fn_update_game_account_rent (
    gaid    integer,    -- 游戏账户ID
    type    tinyint,    -- 租赁类型：0-A租，1-B租
    caid    integer,    -- 渠道账户ID
    state   tinyint     -- 目标状态
)
returns integer
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

    return 0;
end //
delimiter ;
