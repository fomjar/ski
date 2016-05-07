delete from tbl_instruction where i_inst = (conv(00002404, 16, 10) + 0);
insert into tbl_instruction values((conv(00002404, 16, 10) + 0), 'sp', 2, "sp_update_game_account(?, ?, $gid, $gaid, '$user', '$pass_a', '$pass_b', '$pass_curr', '$birth')");

-- 更新订单
delimiter //
drop procedure if exists sp_update_game_account // 
create procedure sp_update_game_account (
    out i_code      integer,
    out c_desc      mediumblob,
    in  gid         integer,        -- 对应游戏ID
    in  gaid        integer,        -- 游戏账户ID，对应产品实例
    in  user        varchar(32),    -- 用户名
    in  pass_a      varchar(32),    -- 密码A
    in  pass_b      varchar(32),    -- 密码B
    in  pass_curr   varchar(32),    -- 当前密码
    in  birth       date            -- 出生日期
)
begin
    declare i_gaid  integer default -1;
    declare i_count integer default -1;

    if gid is null then
        set i_code := conv(00000002, 16, 10) + 0;
        set c_desc := 'illegal args, gid must be not null';
    else
        if gaid is null then
            select max(i_gaid)
              into i_gaid
              from tbl_game_account;

            set i_gaid := i_gaid + 1;

            insert into tbl_game_account (
                i_gid,
                i_gaid,
                c_user,
                c_pass_a,
                c_pass_b,
                c_pass_curr,
                t_birth
            )
            values (
                gid,
                gaid,
                user,
                pass_a,
                pass_b,
                pass_curr,
                birth
            );
        else
            select count(1)
              into i_count
              from tbl_game_account
             where i_gid = gid
               and i_gaid = gaid;

            if i_count <= 0 then
                insert into tbl_game_account (
                    i_gid,
                    i_gaid,
                    c_user,
                    c_pass_a,
                    c_pass_b,
                    c_pass_curr,
                    t_birth
                )
                values (
                    gid,
                    gaid,
                    user,
                    pass_a,
                    pass_b,
                    pass_curr,
                    birth
                );
            else
                if user is not null then
                    update tbl_game_account
                       set c_user = user
                     where i_gid = gid
                       and i_gaid = gaid;
                end if;
                if pass_a is not null then
                    update tbl_game_account
                       set c_pass_a = pass_a
                     where i_gid = gid
                       and i_gaid = gaid;
                end if;
                if pass_b is not null then
                    update tbl_game_account
                       set c_pass_b = pass_b
                     where i_gid = gid
                       and i_gaid = gaid;
                end if;
                if pass_curr is not null then
                    update tbl_game_account
                       set c_pass_curr = pass_curr
                     where i_gid = gid
                       and i_gaid = gaid;
                end if;
                if birth is not null then
                    update tbl_game_account
                       set t_birth = birth
                     where i_gid = gid
                       and i_gaid = gaid;
                end if;
            end if;
        end if;
        set i_code = 0;
        set c_desc = null;
    end if;
end //
delimiter ;
