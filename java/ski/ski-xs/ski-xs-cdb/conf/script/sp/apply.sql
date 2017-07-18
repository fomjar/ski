
delete from tbl_instruction where i_inst = (conv('00002001', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00002001', 16, 10) + 0),
    'sp',
    2,
    "sp_apply_authorize(?, ?, \"$phone\", \"$pass\", \"$token\")"
);
delimiter //
drop procedure if exists sp_apply_authorize //
create procedure sp_apply_authorize (
    out i_code  integer,
    out c_desc  mediumtext,
    in  phone   varchar(16),
    in  pass    varchar(16),
    in  token   varchar(64)
)
begin

    declare di_uid      integer default null;
    declare dc_token    varchar(64);

    if phone is null then
        set i_code = 3;
        set c_desc = 'phone must be not null';
    else
        if pass is not null then
            select i_uid
              into di_uid
              from tbl_user
             where c_phone = phone
               and c_pass = pass;

            if di_uid is null then
                set i_code = 4;
                set c_desc = 'authorize failed';
            else
                set dc_token = replace(uuid(), '-', '');

                replace into tbl_user_state (
                    i_uid,
                    c_token,
                    i_state,
                    t_time
                ) values (
                    di_uid,
                    dc_token,
                    1,
                    now()
                );
                set i_code = 0;
                set c_desc = concat(dc_token);
            end if;
        elseif token is not null then
            select u.i_uid
              into di_uid
              from tbl_user u, tbl_user_state s
             where u.i_uid = s.i_uid
               and u.c_phone = phone
               and s.c_token = token;

            if di_uid is null then
                set i_code = 4;
                set c_desc = 'authorize failed';
            else
                set i_code = 0;
                set c_desc = concat(token);
            end if;
        else
            set i_code = 3;
            set c_desc = 'pass or token must be not null';
        end if;
    end if;

end //
delimiter ;

