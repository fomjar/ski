delete from tbl_instruction where i_inst = (conv('00002402', 16, 10) + 0);
insert into tbl_instruction values((conv('00002402', 16, 10) + 0), 'sp', 2, "sp_update_game_account(?, ?, $gaid, '$remark', '$user', '$pass', '$name', '$birth', '$create')");

-- 更新订单
delimiter //
drop procedure if exists sp_update_game_account //
create procedure sp_update_game_account (
    out i_code  integer,
    out c_desc  mediumblob,
    in  gaid    integer,        -- 游戏账户ID，对应产品实例
    in  remark  varchar(64),    -- 备注
    in  user    varchar(32),    -- 用户名
    in  pass    varchar(32),    -- 当前密码
    in  name    varchar(32),    -- 名称/昵称
    in  birth   date,           -- 出生日期
    in  _create datetime        -- 创建时间
)
begin
    declare di_gaid     integer default -1;
    declare di_count    integer default -1;

    if gaid is null then
        select count(1)
          into di_count
          from tbl_game_account;

        if di_count = 0 then
            set di_gaid = 1;
        else
            select max(i_gaid)
              into di_gaid
              from tbl_game_account;

            set di_gaid = di_gaid + 1;
        end if;

        insert into tbl_game_account (
            i_gaid,
            c_remark,
            c_user,
            c_pass,
            c_name,
            t_birth,
            t_create
        ) values (
            di_gaid,
            remark,
            user,
            pass,
            name,
            birth,
            ifnull(_create, now())
        );

        set c_desc = conv(di_gaid, 10, 16);
    else
        select count(1)
          into di_count
          from tbl_game_account
         where i_gaid = gaid;

        if di_count <= 0 then
            insert into tbl_game_account (
                i_gaid,
                c_remark,
                c_user,
                c_pass,
                c_name,
                t_birth,
                t_create
            ) values (
                gaid,
                remark,
                user,
                pass,
                name,
                birth,
                ifnull(_create, now())
            );
        else
            if remark is not null then
                update tbl_game_account
                   set c_remark = remark
                 where i_gaid = gaid;
            end if;
            if user is not null then
                update tbl_game_account
                   set c_user = user
                 where i_gaid = gaid;
            end if;
            if pass is not null then
                update tbl_game_account
                   set c_pass = pass
                 where i_gaid = gaid;
            end if;
            if name is not null then
                update tbl_game_account
                   set c_name = name
                 where i_gaid = gaid;
            end if;
            if birth is not null then
                update tbl_game_account
                   set t_birth = birth
                 where i_gaid = gaid;
            end if;
            if _create is not null then
                update tbl_game_account
                   set t_create = _create
                 where i_gaid = gaid;
            end if;
        end if;

        set c_desc = conv(gaid, 10, 16);
    end if;
    set i_code = 0;
end //
delimiter ;
