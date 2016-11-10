

-- INST_UPDATE_USER                = 0x00003001
delete from tbl_instruction where i_inst = (conv('00003001', 16, 10) + 0);
insert into tbl_instruction (
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



-- INST_UPDATE_MESSAGE             = 0x00003003
delete from tbl_instruction where i_inst = (conv('00003003', 16, 10) + 0);
insert into tbl_instruction (i_inst, c_mode, i_out, c_sql) values ((conv('00003003', 16, 10) + 0), 'sp', 2, "sp_update_message(?, ?, \"$mid\", $uid, $coosys, $lat, $lng, \"$geohash\", \"$text\", \"$image\")");
delimiter //
drop procedure if exists sp_update_message //
create procedure sp_update_message (
    out i_code  integer,
    out c_desc  mediumtext,
    in  mid     varchar(128),
    in  uid     integer,
    in  coosys  tinyint,
    in  lat     decimal(24, 20),
    in  lng     decimal(24, 20),
    in  geohash varchar(16),
    in  _text   text,
    in  image   mediumtext
)
begin

    declare di_count    integer         default -1;
    declare dc_table    varchar(32)     default null;
    declare dc_create   varchar(300)    default null;
    declare dc_insert   mediumtext      default null;

    if     mid      is null
        or uid   is null
        or coosys   is null
        or lat      is null
        or lng      is null
        or geohash  is null then

        set i_code = 3;
        set c_desc = 'illegal arguments, null arguments';

    else

        set dc_table = concat('tbl_message_', left(geohash, 6));
        select count(1)
          into di_count
          from information_schema.tables
         where table_name = dc_table;

        if 0 = di_count then
            set dc_create = concat('create table ', dc_table, ' (c_mid varchar(128), t_time datetime, i_uid integer, i_coosys tinyint, i_lat decimal(24, 20), i_lng decimal(24, 20), c_geohash varchar(16), c_text text, c_image mediumtext, primary key (c_mid))');
            set @s = dc_create;
            prepare s from @s;
            execute s;
            deallocate prepare s;

            set dc_create = concat('create table ', dc_table, '_focus (c_mid varchar(128), i_uid integer, t_time datetime, i_type tinyint)');
            set @s = dc_create;
            prepare s from @s;
            execute s;
            deallocate prepare s;

            set dc_create = concat('create table ', dc_table, '_reply (c_mid varchar(128), c_rid varchar(128))');
            set @s = dc_create;
            prepare s from @s;
            execute s;
            deallocate prepare s;
        end if;

        set dc_insert = concat('insert into ', dc_table, " (c_mid, t_time, i_uid, i_coosys, i_lat, i_lng, c_geohash, c_text, c_image) values (",
                "\"", mid,      "\", ",
                "\"", now(),    "\", ",
                      uid,      ", ",
                      coosys,   ", ",
                      lat,      ", ",
                      lng,      ", ",
                "\"", geohash,  "\", ",
                ifnull(concat("\"", _text, "\""), 'null'), ", ",
                ifnull(concat("\"", image, "\""), 'null'), ")");
        set @s = dc_insert;
        prepare s from @s;
        execute s;
        deallocate prepare s;

        set i_code = 0;

    end if;

end //
delimiter ;



-- INST_UPDATE_MESSAGE_FOCUS       = 0x00003006
delete from tbl_instruction where i_inst = (conv('00003006', 16, 10) + 0);
insert into tbl_instruction (i_inst, c_mode, i_out, c_sql) values ((conv('00003006', 16, 10) + 0), 'sp', 2, "sp_update_message_focus(?, ?, \"$mid\", $uid, $type)");
delimiter //
drop procedure if exists sp_update_message_focus //
create procedure sp_update_message_focus (
    out i_code  integer,
    out c_desc  mediumtext,
    in  mid     varchar(128),
    in  uid     integer,
    in  _type    tinyint
)
begin

    declare dc_statement    varchar(300)    default 0;

    set dc_statement = concat(
            'delete from tbl_message_', left(mid, 6), '_focus ',
            " where c_mid = \"", mid, "\""
    );
    set @s = dc_statement;
    prepare s from @s;
    execute s;
    deallocate prepare s;

    set dc_statement = concat(
            'insert into tbl_message_', left(mid, 6), '_focus (c_mid, i_uid, t_time, i_type) ',
            "values (\"", mid, "\", ", uid, ", now(), ", _type, ")"
    );
    set @s = dc_statement;
    prepare s from @s;
    execute s;
    deallocate prepare s;

end //
delimiter ;



-- INST_UPDATE_MESSAGE_REPLY       = 0x00003007
delete from tbl_instruction where i_inst = (conv('00003007', 16, 10) + 0);
insert into tbl_instruction (i_inst, c_mode, i_out, c_sql) values ((conv('00003007', 16, 10) + 0), 'sp', 2, "sp_update_message_reply(?, ?, \"$mid\", \"$rid\", $uid, $coosys, $lat, $lng, \"$geohash\", \"$text\", \"$image\")");
delimiter //
drop procedure if exists sp_update_message_reply //
create procedure sp_update_message_reply (
    out i_code  integer,
    out c_desc  mediumtext,
    in  mid     varchar(128),
    in  rid     varchar(128),
    in  uid     integer,
    in  coosys  tinyint,
    in  lat     decimal(24, 20),
    in  lng     decimal(24, 20),
    in  geohash varchar(16),
    in  _text   text,
    in  image   mediumtext
)
begin

    declare dc_statement    varchar(300)    default null;

    call sp_update_message(i_code, c_desc, rid, uid, coosys, lat, lng, geohash, _text, image);

    if i_code = 0 then
        set dc_statement = concat(
                'insert into tbl_message_', left(mid, 6), '_reply (c_mid, c_rid) ',
                "values (\"", mid, "\", \"", rid, "\")"
        );
        set @s = dc_statement;
        prepare s from @s;
        execute s;
        deallocate prepare s;
    end if;

end //
delimiter ;




