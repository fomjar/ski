

-- INST_UPDATE_USER                = 0x00003001
delete from tbl_instruction where i_inst = (conv('00003001', 16, 10) + 0);
insert into tbl_instruction(
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00003001', 16, 10) + 0),
    'st',
    0,
    "replace into tbl_user(i_uid, t_create, c_pass, c_phone, c_email, c_name, c_cover) values ((case (select count(1) from tbl_user u where u.c_phone = \"$phone\") when 0 then (select (ifnull(max(u.i_uid), 0) + 1) from tbl_user u) else (select u.i_uid from tbl_user u where u.c_phone = \"$phone\") end), now(), \"$pass\", \"$phone\", \"$email\", \"$name\", \"$cover\")"
);

-- INST_UPDATE_USER_STATE          = 0x00003004
delete from tbl_instruction where i_inst = (conv('00003004', 16, 10) + 0);
insert into tbl_instruction (i_inst, c_mode, i_out, c_sql) values ((conv('00003004', 16, 10) + 0), 'sp', 2, "sp_update_user_state(?, ?, $uid, $state, $terminal, \"$token\", \"$location\")");
delimiter //
drop procedure if exists sp_update_user_state //
create procedure sp_update_user_state (
    out i_code      integer,
    out c_desc      mediumtext,
    in  uid         integer,
    in  state       tinyint,
    in  terminal    tinyint,
    in  token       varchar(64),
    in  location    varchar(64)
)
begin
    declare di_count    integer default -1;
    declare di_record   tinyint default -1;

    if uid is null then
        set i_code = 3;
        set c_desc = 'illegal arguments, uid must be not null';
    else
        select count(1)
          into di_count
          from tbl_user_state
         where i_uid = uid;
        if di_count = 0 then
            insert into tbl_user_state (
                i_uid,
                i_state,
                t_change,
                i_terminal,
                c_token,
                c_location
            ) values (
                uid,
                null,
                null,
                null,
                null,
                null
            );
        end if;

        if state is not null then
            select count(1)
              into di_count
              from tbl_user_state
             where i_state = state
               and i_uid = uid;
            set di_record = di_record & di_count;

            update tbl_user_state
               set i_state = state
             where i_uid = uid;
        end if;
        if terminal is not null then
            update tbl_user_state
               set i_terminal = terminal
             where i_uid = uid;
        end if;
        if token is not null then
            update tbl_user_state
               set c_token = token
             where i_uid = uid;
        end if;
        if location is not null then
            select count(1)
              into di_count
              from tbl_user_state
             where c_location = location
               and i_uid = uid;
            set di_record = di_record & di_count;

            update tbl_user_state
               set c_location = location
             where i_uid = uid;
        end if;

        update tbl_user_state
           set t_change = now()
         where i_uid = uid;

        if di_record = 0 then
            insert into tbl_user_state_history select * from tbl_user_state where i_uid = uid;
        end if;

        set i_code = 0;
    end if;
end //
delimiter ;


