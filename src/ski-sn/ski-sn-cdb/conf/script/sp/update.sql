

-- INST_UPDATE_USER                = 0x00003001
delete from tbl_instruction where i_inst = (conv('00003001', 16, 10) + 0);
insert into tbl_instruction (i_inst, c_mode, i_out, c_sql) values ((conv('00003001', 16, 10) + 0), 'sp', 2, "sp_update_user(?, ?, $uid, \"$pass\", \"$phone\", \"$email\", \"$name\", \"$cover\", $gender)");
delimiter //
drop procedure if exists sp_update_user //
create procedure sp_update_user (
    out i_code  integer,
    out c_desc  mediumtext,
    in  uid     integer,
    in  pass    varchar(16),
    in  phone   varchar(16),
    in  email   varchar(32),
    in  name    varchar(32),
    in  cover   mediumtext,
    in  gender  tinyint
)
begin

    declare di_count    integer default 0;
    declare di_uid      integer default 0;

    if uid is null then
        select count(1)
          into di_count
          from tbl_user
         where c_phone = phone;

        if di_count > 0 then
            set i_code = 5;
            set c_desc = 'phone already registered';
        else
            select count(1)
              into di_count
              from tbl_user;

            if di_count = 0 then
                set di_uid = 1;
            else
                select max(i_uid)
                  into di_uid
                  from tbl_user;

                set di_uid = di_uid + 1;
            end if;

            insert into tbl_user (
                i_uid,
                t_create,
                c_pass,
                c_phone,
                c_email,
                c_name,
                c_cover,
                i_gender
            ) values (
                di_uid,
                now(),
                pass,
                phone,
                email,
                name,
                cover,
                gender
            );
            set i_code = 0;
            set c_desc = null;
        end if;
    else
        select count(1)
          into di_count
          from tbl_user
         where i_uid = uid;

        if di_count = 0 then
            select count(1)
              into di_count
              from tbl_user
             where c_phone = phone;

            if di_count > 0 then
                set i_code = 5;
                set c_desc = 'phone already registered';
            else
                insert into tbl_user (
                    i_uid,
                    t_create,
                    c_pass,
                    c_phone,
                    c_email,
                    c_name,
                    c_cover,
                    i_gender
                ) values (
                    uid,
                    now(),
                    pass,
                    phone,
                    email,
                    name,
                    cover,
                    gender
                );
                set i_code = 0;
                set c_desc = null;
            end if;
        else
            if pass is not null then
                update tbl_user
                   set c_pass = pass
                 where i_uid = uid;
            end if;
            if phone is not null then
                update tbl_user
                   set c_phone = phone
                 where i_uid = uid;
            end if;
            if email is not null then
                update tbl_user
                   set c_email = email
                 where i_uid = uid;
            end if;
            if name is not null then
                update tbl_user
                   set c_name = name
                 where i_uid = uid;
            end if;
            if cover is not null then
                update tbl_user
                   set c_cover = cover
                 where i_uid = uid;
            end if;
            if gender is not null then
                update tbl_user
                   set i_gender = gender
                 where i_uid = uid;
            end if;

            set i_code = 0;
            set c_desc = null;
        end if;
    end if;
end //
delimiter ;

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
insert into tbl_instruction (i_inst, c_mode, i_out, c_sql) values ((conv('00003003', 16, 10) + 0), 'sp', 2, "sp_update_message(?, ?, \"$mid\", $uid, $coosys, $lat, $lng, \"$geohash\", $type, \"$text\", \"$image\")");
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
    in  _type   tinyint,
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
        or geohash  is null
        or _type    is null then

        set i_code = 3;
        set c_desc = 'illegal arguments, null arguments';

    else

        set dc_table = concat('tbl_message_', left(geohash, 6));
        select count(1)
          into di_count
          from information_schema.tables
         where table_name = dc_table;

        if 0 = di_count then
            set dc_create = concat('create table ', dc_table, ' (c_mid varchar(128), t_time datetime, i_uid integer, i_coosys tinyint, i_lat decimal(24, 20), i_lng decimal(24, 20), c_geohash varchar(16), i_type tinyint, c_text text, c_image mediumtext, primary key (c_mid))');
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

        set dc_insert = concat('insert into ', dc_table, " (c_mid, t_time, i_uid, i_coosys, i_lat, i_lng, c_geohash, i_type, c_text, c_image) values (",
                "\"", mid,      "\", ",
                "\"", now(),    "\", ",
                      uid,      ", ",
                      coosys,   ", ",
                      lat,      ", ",
                      lng,      ", ",
                "\"", geohash,  "\", ",
                      _type,    ", ",
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
            " where c_mid = \"", mid, "\" ",
            '   and i_uid = ', uid
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
insert into tbl_instruction (i_inst, c_mode, i_out, c_sql) values ((conv('00003007', 16, 10) + 0), 'sp', 2, "sp_update_message_reply(?, ?, \"$mid\", \"$rid\", $uid, $coosys, $lat, $lng, \"$geohash\", $type, \"$text\", \"$image\")");
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
    in  _type   tinyint,
    in  _text   text,
    in  image   mediumtext
)
begin

    declare dc_statement    varchar(300)    default null;

    call sp_update_message(i_code, c_desc, rid, uid, coosys, lat, lng, geohash, _type, _text, image);

    if i_code = 0 then
        set dc_statement = concat(
                'insert into tbl_message_', left(mid, 6), '_reply (c_mid, c_rid) ',
                "values (\"", mid, "\", \"", rid, "\")"
        );
        set @s = dc_statement;
        prepare s from @s;
        execute s;
        deallocate prepare s;

        if left(mid, 6) != left(rid, 6) then
            set dc_statement = concat(
                    'insert into tbl_message_', left(rid, 6), '_reply (c_mid, c_rid) ',
                    "values (\"", mid, "\", \"", rid, "\")"
            );
            set @s = dc_statement;
            prepare s from @s;
            execute s;
            deallocate prepare s;
        end if;

    end if;

end //
delimiter ;


-- INST_UPDATE_ACTIVITY            = 0x00003008
delete from tbl_instruction where i_inst = (conv('00003008', 16, 10) + 0);
insert into tbl_instruction (i_inst, c_mode, i_out, c_sql) values ((conv('00003008', 16, 10) + 0), 'sp', 2, "sp_update_activity(?, ?, $aid, $owner, $lat, $lng, \"$geohash\", \"$title\", \"$text\", \"$image\", \"$begin\", \"$end\", $state)");
delimiter //
drop procedure if exists sp_update_activity //
create procedure sp_update_activity (
    out i_code      integer,
    out c_desc      mediumtext,
    in  aid         integer,
    in  owner       integer,
    in  lat         decimal(24, 20),
    in  lng         decimal(24, 20),
    in  geohash     varchar(16),
    in  title       varchar(256),
    in  _text       text,
    in  image       mediumtext,
    in  _begin      varchar(32),
    in  _end        varchar(32),
    in  state       tinyint
)
begin

    declare di_count    integer default 0;
    declare di_aid      integer default 0;

    if aid is null then
        select count(1)
          into di_count
          from tbl_activity;

        if di_count = 0 then
            set di_aid = 1;
        else
            select max(i_aid)
              into di_aid
              from tbl_activity;

            set di_aid = di_aid + 1;
        end if;

        insert into tbl_activity (
            i_aid,
            i_owner,
            t_create,
            i_lat,
            i_lng,
            c_geohash,
            c_title,
            c_text,
            c_image,
            c_begin,
            c_end,
            i_state
        ) values (
            di_aid,
            owner,
            now(),
            lat,
            lng,
            geohash,
            title,
            _text,
            image,
            _begin,
            _end,
            0
        );
    else
        set di_aid = aid;

        select count(1)
          into di_count
          from tbl_activity
         where i_aid = di_aid;

        if di_count = 0 then
            insert into tbl_activity (
                i_aid,
                i_owner,
                t_create,
                i_lat,
                i_lng,
                c_geohash,
                c_title,
                c_text,
                c_image,
                c_begin,
                c_end,
                i_state
            ) values (
                di_aid,
                owner,
                now(),
                lat,
                lng,
                geohash,
                title,
                _text,
                image,
                _begin,
                _end,
                0
            );
        else
            if owner is not null then
                update tbl_activity
                   set i_owner = owner
                 where i_aid = di_aid;
            end if;
            if lat is not null then
                update tbl_activity
                   set i_lat = lat
                 where i_aid = di_aid;
            end if;
            if lng is not null then
                update tbl_activity
                   set i_lng = lng
                 where i_aid = di_aid;
            end if;
            if geohash is not null then
                update tbl_activity
                   set c_geohash = geohash
                 where i_aid = di_aid;
            end if;
            if title is not null then
                update tbl_activity
                   set c_title = title
                 where i_aid = di_aid;
            end if;
            if _text is not null then
                update tbl_activity
                   set c_text = _text
                 where i_aid = di_aid;
            end if;
            if image is not null then
                update tbl_activity
                   set c_image = image
                 where i_aid = di_aid;
            end if;
            if _begin is not null then
                update tbl_activity
                   set c_begin = _begin
                 where i_aid = di_aid;
            end if;
            if _end is not null then
                update tbl_activity
                   set c_end = _end
                 where i_aid = di_aid;
            end if;
            if state is not null then
                update tbl_activity
                   set i_state = state
                 where i_aid = di_aid;
            end if;
        end if;
    end if;

    set i_code = 0;
    set c_desc = concat(di_aid, '');

end //
delimiter ;


-- INST_UPDATE_ACTIVITY_ROLE       = 0x00003009
delete from tbl_instruction where i_inst = (conv('00003009', 16, 10) + 0);
insert into tbl_instruction (i_inst, c_mode, i_out, c_sql) values ((conv('00003009', 16, 10) + 0), 'sp', 2, "sp_update_activity_role(?, ?, $aid, $arsn, \"$name\", $apply, $count)");
delimiter //
drop procedure if exists sp_update_activity_role //
create procedure sp_update_activity_role (
    out i_code  integer,
    out c_desc  mediumtext,
    in  aid     integer,
    in  arsn    tinyint,
    in  name    varchar(32),
    in  apply   tinyint,
    in  _count  integer
)
begin

    declare di_count    integer default 0;
    declare di_arsn     tinyint default 0;

    if aid is null then
        set i_code = 3;
        set c_desc = 'illegal arguments, aid must be not null';
    else
        select count(1)
          into di_count
          from tbl_activity
         where i_aid = aid;

        if di_count = 0 then
            set i_code = 3;
            set c_desc = 'illegal arguments, activity does not exist';
        else
            if arsn is null then
                select count(1)
                  into di_count
                  from tbl_activity_role
                 where i_aid = aid;

                if di_count = 0 then
                    set di_arsn = 1;
                else
                    select max(i_arsn)
                      into di_arsn
                      from tbl_activity_role
                     where i_aid = aid;

                    set di_arsn = di_arsn + 1;
                end if;

                insert into tbl_activity_role (
                    i_aid,
                    i_arsn,
                    c_name,
                    i_apply,
                    i_count
                ) values (
                    aid,
                    di_arsn,
                    name,
                    apply,
                    _count
                );
            else
                set di_arsn = arsn;

                select count(1)
                  into di_count
                  from tbl_activity_role
                 where i_aid = aid
                   and i_arsn = di_arsn;

                if di_count = 0 then
                    insert into tbl_activity_role (
                        i_aid,
                        i_arsn,
                        c_name,
                        i_apply,
                        i_count
                    ) values (
                        aid,
                        di_arsn,
                        name,
                        apply,
                        _count
                    );
                else
                    if name is not null then
                        update tbl_activity_role
                           set c_name = name
                         where i_aid = aid
                           and i_arsn = di_arsn;
                    end if;
                    if apply is not null then
                        update tbl_activity_role
                           set i_apply = apply
                         where i_aid = aid
                           and i_arsn = di_arsn;
                    end if;
                    if _count is not null then
                        update tbl_activity_role
                           set i_count = _count
                         where i_aid = aid
                           and i_arsn = di_arsn;
                    end if;
                end if;
            end if;

            set i_code = 0;
            set c_desc = concat(di_arsn, '');
        end if;
    end if;

end //
delimiter ;


-- INST_UPDATE_ACTIVITY_PLAYER     = 0x0000300A
delete from tbl_instruction where i_inst = (conv('0000300A', 16, 10) + 0);
insert into tbl_instruction (i_inst, c_mode, i_out, c_sql) values ((conv('0000300A', 16, 10) + 0), 'sp', 2, "sp_update_activity_player(?, ?, $aid, $uid, $arsn)");
delimiter //
drop procedure if exists sp_update_activity_player //
create procedure sp_update_activity_player (
    out i_code  integer,
    out c_desc  mediumtext,
    in  aid     integer,
    in  uid     integer,
    in  arsn    tinyint
)
begin

    declare di_count    integer default 0;
    declare di_max      integer default 0;

    if aid is null or uid is null then
        set i_code = 3;
        set c_desc = 'illegal arguments, aid and uid must be not null';
    else
        select count(1)
          into di_count
          from tbl_activity
         where i_aid = aid;

        if di_count = 0 then
            set i_code = 3;
            set c_desc = 'illegal arguments, activity does not exist';
        else
            select i_count
              into di_max
              from tbl_activity_role
             where i_aid = aid
               and i_arsn = arsn;

            select count(1)
              into di_count
              from tbl_activity_player
             where i_aid = aid
               and i_arsn = arsn;

            if di_count >= di_max then
                set i_code = 5;
                set c_desc = 'max player';
            else
                select count(1)
                  into di_count
                  from tbl_activity_player
                 where i_aid = aid
                   and i_uid = uid;

                if di_count > 0 then
                    if arsn is not null then
                        update tbl_activity_player
                           set i_arsn = arsn
                         where i_aid = aid
                           and i_uid = uid;
                    end if;
                else
                    insert into tbl_activity_player (
                        i_aid,
                        i_uid,
                        i_arsn,
                        t_time
                    ) values (
                        aid,
                        uid,
                        arsn,
                        now()
                    );
                end if;

                set i_code = 0;

            end if;
        end if;
    end if;

end //
delimiter ;


-- INST_UPDATE_ACTIVITY_MODULE     = 0x0000300B
delete from tbl_instruction where i_inst = (conv('0000300B', 16, 10) + 0);
insert into tbl_instruction (i_inst, c_mode, i_out, c_sql) values ((conv('0000300B', 16, 10) + 0), 'sp', 2, "sp_update_activity_module(?, ?, $owner, $aid, $amsn, $type, \"$title\", \"$text\")");
delimiter //
drop procedure if exists sp_update_activity_module //
create procedure sp_update_activity_module (
    out i_code  integer,
    out c_desc  mediumtext,
    in  owner   integer,
    in  aid     integer,
    in  amsn    tinyint,
    in  _type   tinyint,
    in  title   varchar(256),
    in  _text   text
)
begin

    declare di_count    integer default 0;
    declare di_amsn     tinyint default 0;

    if aid is null then
        set i_code = 3;
        set c_desc = 'illegal arguments, aid must be not null';
    else
        select count(1)
          into di_count
          from tbl_activity
         where i_aid = aid;

        if di_count = 0 then
            set i_code = 3;
            set c_desc = 'illegal arguments, activity does not exist';
        else
            if amsn is null then
                select count(1)
                  into di_count
                  from tbl_activity_module
                 where i_aid = aid;

                if di_count = 0 then
                    set di_amsn = 1;
                else
                    select max(i_amsn)
                      into di_amsn
                      from tbl_activity_module
                     where i_aid = aid;

                    set di_amsn = di_amsn + 1;
                end if;

                insert into tbl_activity_module (
                    i_aid,
                    i_amsn,
                    i_type,
                    c_title,
                    c_text
                ) values (
                    aid,
                    di_amsn,
                    _type,
                    title,
                    _text
                );
            else
                set di_amsn = amsn;

                select count(1)
                  into di_count
                  from tbl_activity_module
                 where i_aid = aid
                   and i_amsn = di_amsn;

                if di_count = 0 then
                    insert into tbl_activity_module (
                        i_aid,
                        i_amsn,
                        i_type,
                        c_title,
                        c_text
                    ) values (
                        aid,
                        di_amsn,
                        _type,
                        title,
                        _text
                    );
                else
                    if _type is not null then
                        update tbl_activity_module
                           set i_type = _type
                         where i_aid = aid
                           and i_amsn = di_amsn;
                    end if;
                    if title is not null then
                        update tbl_activity_module
                           set c_title = title
                         where i_aid = aid
                           and i_amsn = di_amsn;
                    end if;
                    if _text is not null then
                        update tbl_activity_module
                           set c_text = _text
                         where i_aid = aid
                           and i_amsn = di_amsn;
                    end if;
                end if;
            end if;

            set i_code = 0;
            set c_desc = concat(di_amsn, '');
        end if;
    end if;

end //
delimiter ;


-- INST_UPDATE_ACTIVITY_MODULE_PRIVILEGE   = 0x0000300C
delete from tbl_instruction where i_inst = (conv('0000300C', 16, 10) + 0);
insert into tbl_instruction (i_inst, c_mode, i_out, c_sql) values ((conv('0000300C', 16, 10) + 0), 'sp', 2, "sp_update_activity_module_privilege(?, ?, $aid, $amsn, $arsn, $privilege)");
delimiter //
drop procedure if exists sp_update_activity_module_privilege //
create procedure sp_update_activity_module_privilege (
    out i_code      integer,
    out c_desc      mediumtext,
    in  aid         integer,
    in  amsn        tinyint,
    in  arsn        tinyint,
    in  _privilege  integer
)
begin

    declare di_count    integer default 0;

    if aid is null or amsn is null or arsn is null or _privilege is null then
        set i_code = 3;
        set c_desc = 'illegal argument, aid or amsn or arsn or privilege must be not null';
    else
        select count(1)
          into di_count
          from tbl_activity
         where i_aid = aid;

        if di_count = 0 then
            set i_code = 3;
            set c_desc = 'illegal arguments, activity does not exist';
        else
            select count(1)
              into di_count
              from tbl_activity_module_privilege
             where i_aid = aid
               and i_amsn = amsn
               and i_arsn = arsn;

            if di_count = 0 then
                insert into tbl_activity_module_privilege (
                    i_aid,
                    i_amsn,
                    i_arsn,
                    i_privilege
                ) values (
                    aid,
                    amsn,
                    arsn,
                    _privilege
                );
            else
                update tbl_activity_module_privilege
                   set i_privilege = _privilege
                 where i_aid = aid
                   and i_amsn = amsn
                   and i_arsn = arsn;
            end if;

            set i_code = 0;

        end if;
    end if;

end //
delimiter ;


-- INST_UPDATE_ACTIVITY_MODULE_VOTE        = 0x0000300D
delete from tbl_instruction where i_inst = (conv('0000300D', 16, 10) + 0);
insert into tbl_instruction (i_inst, c_mode, i_out, c_sql) values ((conv('0000300D', 16, 10) + 0), 'sp', 2, "sp_update_activity_module_vote(?, ?, $aid, $amsn, $select, $anonym, $item)");
delimiter //
drop procedure if exists sp_update_activity_module_vote //
create procedure sp_update_activity_module_vote (
    out i_code      integer,
    out c_desc      mediumtext,
    in  aid         integer,
    in  amsn        tinyint,
    in  _select     tinyint,
    in  anonym      tinyint,
    in  item        tinyint
)
begin

    declare di_count    integer default 0;

    if aid is null or amsn is null then
        set i_code = 3;
        set c_desc = 'illegal argument, aid or amsn must be not null';
    else
        select count(1)
          into di_count
          from tbl_activity
         where i_aid = aid;

        if di_count = 0 then
            set i_code = 3;
            set c_desc = 'illegal arguments, activity does not exist';
        else
            select count(1)
              into di_count
              from tbl_activity_module_vote
             where i_aid = aid
               and i_amsn = amsn;

            if di_count = 0 then
               insert into tbl_activity_module_vote (
                   i_aid,
                   i_amsn,
                   i_select,
                   i_anonym,
                   i_item
               ) values (
                   aid,
                   amsn,
                   _select,
                   anonym,
                   item
               );
            else
               if _select is not null then
                   update tbl_activity_module_vote
                      set i_select = _select
                    where i_aid = aid
                      and i_amsn = amsn;
               end if;
               if anonym is not null then
                   update tbl_activity_module_vote
                      set i_anonym = anonym
                    where i_aid = aid
                      and i_amsn = amsn;
               end if;
               if item is not null then
                   update tbl_activity_module_vote
                      set i_item = item
                    where i_aid = aid
                      and i_amsn = amsn;
               end if;
            end if;

            set i_code = 0;

        end if;
    end if;
end //
delimiter ;



-- INST_UPDATE_ACTIVITY_MODULE_VOTE_ITEM   = 0x0000300E
delete from tbl_instruction where i_inst = (conv('0000300E', 16, 10) + 0);
insert into tbl_instruction (i_inst, c_mode, i_out, c_sql) values ((conv('0000300E', 16, 10) + 0), 'sp', 2, "sp_update_activity_module_vote_item(?, ?, $aid, $amsn, $amvisn, \"$arg0\", \"$arg1\", \"$arg2\", \"$arg3\", \"$arg4\", \"$arg5\", \"$arg6\", \"$arg7\", \"$arg8\", \"$arg9\")");
delimiter //
drop procedure if exists sp_update_activity_module_vote_item //
create procedure sp_update_activity_module_vote_item (
    out i_code  integer,
    out c_desc  mediumtext,
    in  aid     integer,
    in  amsn    tinyint,
    in  amvisn  tinyint,
    in  arg0    varchar(32),
    in  arg1    varchar(32),
    in  arg2    varchar(32),
    in  arg3    varchar(32),
    in  arg4    varchar(32),
    in  arg5    varchar(32),
    in  arg6    varchar(32),
    in  arg7    varchar(32),
    in  arg8    varchar(32),
    in  arg9    varchar(32)
)
begin

    declare di_count    integer default 0;

    if aid is null or amsn is null or amvisn is null then
        set i_code = 3;
        set c_desc = 'illegal argument, aid or amsn or amvisn must be not null';
    else
        select count(1)
          into di_count
          from tbl_activity
         where i_aid = aid;

        if di_count = 0 then
            set i_code = 3;
            set c_desc = 'illegal arguments, activity does not exist';
        else
            select count(1)
              into di_count
              from tbl_activity_module_vote_item
             where i_aid = aid
               and i_amsn = amsn
               and i_amvisn = amvisn;

            if di_count = 0 then
                insert into tbl_activity_module_vote_item (
                    i_aid,
                    i_amsn,
                    i_amvisn,
                    c_arg0,
                    c_arg1,
                    c_arg2,
                    c_arg3,
                    c_arg4,
                    c_arg5,
                    c_arg6,
                    c_arg7,
                    c_arg8,
                    c_arg9
                ) values (
                    aid,
                    amsn,
                    amvisn,
                    arg0,
                    arg1,
                    arg2,
                    arg3,
                    arg4,
                    arg5,
                    arg6,
                    arg7,
                    arg8,
                    arg9
                );
            else
                if arg0 is not null then
                    update tbl_activity_module_vote_item
                       set c_arg0 = arg0
                     where i_aid = aid
                       and i_amsn = amsn
                       and i_amvisn = amvisn;
                end if;
                if arg1 is not null then
                    update tbl_activity_module_vote_item
                       set c_arg1 = arg1
                     where i_aid = aid
                       and i_amsn = amsn
                       and i_amvisn = amvisn;
                end if;
                if arg2 is not null then
                    update tbl_activity_module_vote_item
                       set c_arg2 = arg2
                     where i_aid = aid
                       and i_amsn = amsn
                       and i_amvisn = amvisn;
                end if;
                if arg3 is not null then
                    update tbl_activity_module_vote_item
                       set c_arg3 = arg3
                     where i_aid = aid
                       and i_amsn = amsn
                       and i_amvisn = amvisn;
                end if;
                if arg4 is not null then
                    update tbl_activity_module_vote_item
                       set c_arg4 = arg4
                     where i_aid = aid
                       and i_amsn = amsn
                       and i_amvisn = amvisn;
                end if;
                if arg5 is not null then
                    update tbl_activity_module_vote_item
                       set c_arg5 = arg5
                     where i_aid = aid
                       and i_amsn = amsn
                       and i_amvisn = amvisn;
                end if;
                if arg6 is not null then
                    update tbl_activity_module_vote_item
                       set c_arg6 = arg6
                     where i_aid = aid
                       and i_amsn = amsn
                       and i_amvisn = amvisn;
                end if;
                if arg7 is not null then
                    update tbl_activity_module_vote_item
                       set c_arg7 = arg7
                     where i_aid = aid
                       and i_amsn = amsn
                       and i_amvisn = amvisn;
                end if;
                if arg8 is not null then
                    update tbl_activity_module_vote_item
                       set c_arg8 = arg8
                     where i_aid = aid
                       and i_amsn = amsn
                       and i_amvisn = amvisn;
                end if;
                if arg9 is not null then
                    update tbl_activity_module_vote_item
                       set c_arg9 = arg9
                     where i_aid = aid
                       and i_amsn = amsn
                       and i_amvisn = amvisn;
                end if;
            end if;

            set i_code = 0;

        end if;
    end if;
end //
delimiter ;


-- INST_UPDATE_ACTIVITY_MODULE_VOTE_PLAYER = 0x0000300F
delete from tbl_instruction where i_inst = (conv('0000300F', 16, 10) + 0);
insert into tbl_instruction (i_inst, c_mode, i_out, c_sql) values ((conv('0000300F', 16, 10) + 0), 'sp', 2, "sp_update_activity_module_vote_player(?, ?, $aid, $amsn, $amvisn, $uid, $result)");
delimiter //
drop procedure if exists sp_update_activity_module_vote_player //
create procedure sp_update_activity_module_vote_player (
    out i_code      integer,
    out c_desc      mediumtext,
    in  aid         integer,
    in  amsn        tinyint,
    in  amvisn      tinyint,
    in  uid         integer,
    in  result      tinyint
)
begin

    declare di_count    integer default 0;

    if aid is null or amsn is null or amvisn is null or uid is null or result is null then
        set i_code = 3;
        set c_desc = 'illegal argument, aid or amsn or amvisn or uid or result must be not null';
    else
        select count(1)
          into di_count
          from tbl_activity
         where i_aid = aid;

        if di_count = 0 then
            set i_code = 3;
            set c_desc = 'illegal arguments, activity does not exist';
        else
            select count(1)
              into di_count
              from tbl_activity_module_vote_player
             where i_aid = aid
               and i_amsn = amsn
               and i_amvisn = amvisn
               and i_uid = uid;

            if di_count = 0 then
                insert into tbl_activity_module_vote_player (
                    i_aid,
                    i_amsn,
                    i_amvisn,
                    i_uid,
                    i_result,
                    t_time
                ) values (
                    aid,
                    amsn,
                    amvisn,
                    uid,
                    result,
                    now()
                );
            else
                update tbl_activity_module_vote_player
                   set i_result = result
                 where i_aid = aid
                   and i_amsn = amsn
                   and i_amvisn = amvisn
                   and i_uid = uid;
            end if;

            set i_code = 0;

        end if;
    end if;
end //
delimiter ;









