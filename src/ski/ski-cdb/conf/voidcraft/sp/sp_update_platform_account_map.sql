delete from tbl_instruction where i_inst = (conv('0000240A', 16, 10) + 0);
insert into tbl_instruction values((conv('0000240A', 16, 10) + 0), 'sp', 2, "sp_update_platform_account_map(?, ?, $paid, $caid)");

-- 更新订单
delimiter //
drop procedure if exists sp_update_platform_account_map // 
create procedure sp_update_platform_account_map (
    out i_code      integer,
    out c_desc      mediumblob,
    in  paid        integer,        -- 游戏账户ID
    in  caid        integer
)
begin
    declare di_count    integer default -1;

    select count(1)
      into di_count
      from tbl_platform_account
     where i_paid = paid;

    if di_count = 0 then
        set i_code = 2;
        set c_desc = 'no such a paid in system';
    else
        select count(1)
          into di_count
          from tbl_channel_account
         where i_caid = caid;

        if di_count = 0 then
            set i_code = 2;
            set c_desc = 'no such a caid in system';
        else
            select count(1)
              into di_count
              from tbl_platform_account_map
             where i_paid = paid
               and i_caid = caid;

            if di_count = 0 then
                insert into tbl_platform_account_map (
                    i_paid,
                    i_caid
                ) values (
                    paid,
                    caid
                );
            end if;

            set i_code = 0;
            set c_desc = null;
        end if;
    end if;
end //
delimiter ;
