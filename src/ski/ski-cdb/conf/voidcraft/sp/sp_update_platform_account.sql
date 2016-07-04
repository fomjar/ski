delete from tbl_instruction where i_inst = (conv('00002409', 16, 10) + 0);
insert into tbl_instruction values((conv('00002409', 16, 10) + 0), 'sp', 2, "sp_update_platform_account(?, ?, $paid, '$user', '$pass', '$name', '$mobile', '$email', '$birth', $cash, $coupon, '$create')");

-- 更新游戏
delimiter //
drop procedure if exists sp_update_platform_account //
create procedure sp_update_platform_account (
    out i_code  integer,
    out c_desc  mediumblob,
    in  paid    integer,        -- 平台账户ID
    in  user    varchar(32),    -- 用户名
    in  pass    varchar(32),    -- 密码
    in  name    varchar(32),    -- 姓名
    in  mobile  varchar(20),    -- 手机
    in  email   varchar(32),    -- 邮箱
    in  birth   date,           -- 出生日期
    in  cash    decimal(9, 2),  -- 现金余额（可退的）
    in  coupon  decimal(9, 2),  -- 优惠券（不可退）
    in  _create datetime        -- 创建时间
)
begin
    declare di_paid     integer default -1;
    declare di_count    integer default -1;

    if paid is null then
        select count(1)
          into di_count
          from tbl_platform_account;

        if di_count = 0 then
            set di_paid = 1;
        else
            select max(i_paid)
              into di_paid
              from tbl_platform_account;

            set di_paid = di_paid + 1;
        end if;

        insert into tbl_platform_account (
            i_paid,
            c_user,
            c_pass,
            c_name,
            c_mobile,
            c_email,
            t_birth,
            i_cash,
            i_coupon,
            t_create
        ) values (
            di_paid,
            user,
            pass,
            name,
            mobile,
            email,
            birth,
            ifnull(cash, 0.00),
            ifnull(coupon, 0.00),
            ifnull(_create, now())
        );
    else
        set di_paid = paid;

        select count(1)
          into di_count
          from tbl_platform_account
         where i_paid = di_paid;

        if di_count <= 0 then
            insert into tbl_platform_account (
                i_paid,
                c_user,
                c_pass,
                c_name,
                c_mobile,
                c_email,
                t_birth,
                i_cash,
                i_coupon,
                t_create
            ) values (
                di_paid,
                user,
                pass,
                name,
                mobile,
                email,
                birth,
                ifnull(cash, 0.00),
                ifnull(coupon, 0.00),
                ifnull(_create, now())
            );
        else
            if user is not null then
                update tbl_platform_account
                   set c_user = user
                 where i_paid = di_paid;
            end if;
            if pass is not null then
                update tbl_platform_account
                   set c_pass = pass
                 where i_paid = di_paid;
            end if;
            if name is not null then
                update tbl_platform_account
                   set c_name = name
                 where i_paid = di_paid;
            end if;
            if mobile is not null then
                update tbl_platform_account
                   set c_mobile = mobile
                 where i_paid = di_paid;
            end if;
            if email is not null then
                update tbl_platform_account
                   set c_email = email
                 where i_paid = di_paid;
            end if;
            if birth is not null then
                update tbl_platform_account
                   set t_birth = birth
                 where i_paid = di_paid;
            end if;
            if cash is not null then
                update tbl_platform_account
                   set i_cash = cash
                 where i_paid = di_paid;
            end if;
            if coupon is not null then
                update tbl_platform_account
                   set i_coupon = coupon
                 where i_paid = di_paid;
            end if;
            if _create is not null then
                update tbl_platform_account
                   set c_create = _create
                 where i_paid = di_paid;
            end if;
        end if;
    end if;
    set i_code = 0;
    set c_desc = null;
end //
delimiter ;
