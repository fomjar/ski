delete from tbl_instruction where i_inst = (conv('00002405', 16, 10) + 0);
insert into tbl_instruction values((conv('00002405', 16, 10) + 0), 'sp', 2, "sp_update_channel_account(?, ?, '$caid', '$user', $channel, '$nick', $gender, '$phone', '$address', '$zipcode', '$birth')");

-- 更新订单
delimiter //
drop procedure if exists sp_update_channel_account // 
create procedure sp_update_channel_account (
    out i_code      integer,
    out c_desc      mediumblob,
    in  caid        integer,        -- 渠道账户ID
    in  user        varchar(32),    -- 用户名
    in  channel     tinyint,        -- 渠道：0-淘宝 1-微信
    in  nick        varchar(32),    -- 昵称
    in  gender      tinyint,        -- 性别：0-女 1-男 2-人妖
    in  phone       varchar(20),    -- 电话
    in  address     varchar(100),   -- 地址
    in  zipcode     varchar(10),    -- 邮编
    in  birth       date            -- 生日
)
begin
    declare di_caid     integer default -1;
    declare di_paid     integer default -1;
    declare di_count    integer default -1;

    if caid is null then
        select count(1)
          into di_count
          from tbl_channel_account;

        if di_count = 0 then
            set di_caid = 1;
        else
            select max(i_caid)
              into di_caid
              from tbl_channel_account;

            set di_caid = di_caid + 1;
        end if;

        insert into tbl_channel_account (
            i_caid,
            c_user,
            i_channel,
            c_nick,
            i_gender,
            c_phone,
            c_address,
            c_zipcode,
            t_birth
        ) values (
            di_caid,
            user,
            channel,
            nick,
            gender,
            phone,
            address,
            zipcode,
            birth
        );
    else
        set di_caid = caid;

        select count(1)
          into di_count
          from tbl_channel_account
         where i_caid = di_caid;

        if di_count <= 0 then
            insert into tbl_channel_account (
                i_caid,
                c_user,
                i_channel,
                c_nick,
                i_gender,
                c_phone,
                c_address,
                c_zipcode,
                t_birth
            ) values (
                di_caid,
                user,
                channel,
                nick,
                gender,
                phone,
                address,
                zipcode,
                birth
            );
        else
            if user is not null then
                update tbl_channel_account
                   set c_user = user
                 where i_caid = di_caid;
            end if;
            if channel is not null then
                update tbl_channel_account
                   set i_channel = channel
                 where i_caid = di_caid;
            end if;
            if nick is not null then
                update tbl_channel_account
                   set c_nick = nick
                 where i_caid = di_caid;
            end if;
            if gender is not null then
                update tbl_channel_account
                   set i_gender = gender
                 where i_caid = di_caid;
            end if;
            if phone is not null then
                update tbl_channel_account
                   set c_phone = phone
                 where i_caid = di_caid;
            end if;
            if address is not null then
                update tbl_channel_account
                   set c_address = address
                 where i_caid = di_caid;
            end if;
            if zipcode is not null then
                update tbl_channel_account
                   set c_zipcode = zipcode
                 where i_caid = di_caid;
            end if;
            if birth is not null then
                update tbl_channel_account
                   set t_birth = birth
                 where i_caid = di_caid;
            end if;
        end if;
    end if;

    select count(1)
      into di_count
      from tbl_platform_account pa, tbl_platform_account_map pam
     where pa.i_paid = pam.i_paid
       and pam.i_caid = di_caid;

    if di_count = 0 then
        call sp_update_platform_account(i_code, c_desc, null, conv(di_caid, 10, 16), null, null, null, null, null, null, null, null);
        select i_paid
          into di_paid
          from tbl_platform_account
         where c_user = conv(di_caid, 10, 16);
        call sp_update_platform_account(i_code, c_desc, di_paid, '', null, null, null, null, null, null, null, null);

        call sp_update_platform_account_map(i_code, c_desc, di_paid, di_caid);
    end if;

    set i_code = 0;
    set c_desc = null;
end //
delimiter ;
