-- 修改租赁实例状态
delimiter //
drop function if exists fn_update_game_account_rent // 
create function fn_update_game_account_rent (
    pid     integer,    -- 产品ID
    gaid    integer,    -- 游戏账户ID
    caid    integer,    -- 渠道账户ID
    state   tinyint     -- 目标状态
)
returns integer
begin
    declare di_count        integer default -1;
    declare di_caid         integer default null;
    declare di_state_before tinyint default -1;
    declare di_state_after  tinyint default -1;

    select count(1)
      into di_count
      from tbl_game_account_rent
     where i_pid = pid
       and i_gaid = gaid;

    if di_count = 0 then
        return -1;
    end if;

    if caid is null then
        select distinct i_caid
          into di_caid
          from tbl_game_account_rent
         where i_pid = pid
           and i_gaid = gaid;
    else
        set di_caid = caid;
    end if;

    select i_state
      into di_state_before
      from tbl_game_account_rent
     where i_pid = pid
       and i_gaid = gaid;

    set di_state_after = state;

    insert into tbl_game_account_rent_history (
        i_pid,
        i_gaid,
        i_caid,
        i_state_before,
        i_state_after,
        t_change
    ) values (
        pid,
        gaid,
        di_caid,
        di_state_before,
        di_state_after,
        now()
    );

    update tbl_game_account_rent
       set i_state = state
       and i_caid = di_caid
     where i_pid = pid
       and i_gaid = gaid;

    return 0;
end //
delimiter ;
