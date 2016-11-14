

-- INST_QUERY_USER_STATE           = 0x00002004
delete from tbl_instruction where i_inst = (conv('00002004', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00002004', 16, 10) + 0),
    'st',
    6,
    "select u.i_uid, u.t_create, u.c_phone, u.c_email, u.c_name, u.c_cover from tbl_user u, tbl_user_state s where u.i_uid = s.i_uid and s.i_uid = $uid and s.c_token = \"$token\""
);

-- INST_QUERY_MESSAGE              = 0x00002003
delete from tbl_instruction where i_inst = (conv('00002003', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00002003', 16, 10) + 0),
    'sp',
    2,
    "sp_query_message(?, ?, $lat, $lng, \"$geohash\")"
);

delimiter //
drop procedure if exists sp_query_message;
create procedure sp_query_message (
    out i_code  integer,
    out c_desc  longtext,
    in  lat     decimal(24, 20),
    in  lng     decimal(24, 20),
    in  geohash varchar(16)
)
begin

    declare dc_statement    varchar(500)    default null;
    declare di_count        integer         default 0;
    declare di_length       tinyint         default 6;
    declare dc_cgh          varchar(16)     default null;
    declare dc_mid          varchar(128)    default null;

    declare di_distance     integer         default 0;
    declare di_second       integer         default 0;
    declare di_focus        integer         default 0;
    declare di_reply        integer         default 0;

    declare done            integer         default 0;
    declare rs_gh           cursor for select distinct c_geohash from tmp_geohash;
    declare rs_mid          cursor for select c_mid from tmp_message;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;


    -- 生成GeoHash清单
    call sp_generate_geohash(left(geohash, 6));

    -- 获得消息清单
    delete from tmp_message;
    while di_length >= 3 do

        set done = 0;
        open rs_gh;
        fetch rs_gh into dc_cgh;
        while done = 0 do

            if left(dc_cgh, di_length) = left(geohash, di_length)
                and (di_length = 6
                    or left(dc_cgh, di_length+1) != left(geohash, di_length+1))
                then

                set dc_statement = concat(
                        'insert into tmp_message (c_mid, t_time, i_uid, i_coosys, i_lat, i_lng, c_geohash, c_text, c_image, i_distance, i_second, i_focus, i_reply, i_weight) ',
                        'select c_mid, t_time, i_uid, i_coosys, i_lat, i_lng, c_geohash, c_text, c_image, 0, 0, 0, 0, 0 ',
                        '  from tbl_message_', dc_cgh , ' ',
                        ' where c_mid not in (select c_rid from tbl_message_', dc_cgh,'_reply)'
                );
                set @s = dc_statement;
                prepare s from @s;
                execute s;
                deallocate prepare s;
            end if;

            fetch rs_gh into dc_cgh;

        end while;
        close rs_gh;

        select count(1)
          into di_count
          from tmp_message;

        if di_count >= 500 then
            set di_length = 0;
        else
            set di_length = di_length - 1;
        end if;

    end while;


    -- 逐一计算权重
    set done = 0;
    open rs_mid;
    fetch rs_mid into dc_mid;
    while done = 0 do

        select fn_distance_flattern(i_lat, i_lng, lat, lng)
          into di_distance
          from tmp_message
         where c_mid = dc_mid;

        select timestampdiff(second, t_time, now())
          into di_second
          from tmp_message
         where c_mid = dc_mid;

        call sp_message_focus_count(di_focus, dc_mid);

        call sp_message_reply_count(di_reply, dc_mid);

        -- 权重公式
        update tmp_message
           set i_distance = di_distance,
               i_second = di_second,
               i_focus = di_focus,
               i_reply = di_reply,
               i_weight = 0 - di_distance - di_second * 2 + di_focus * 50 + di_reply * 100
         where c_mid = dc_mid;

        fetch rs_mid into dc_mid;

    end while;
    close rs_mid;

    set i_code = 0;
    select group_concat(concat(
                    m.c_mid,
            '\'\t', m.t_time,
            '\'\t', m.i_distance,
            '\'\t', m.i_second,
            '\'\t', m.i_focus,
            '\'\t', m.i_reply,
            '\'\t', u.i_uid,
            '\'\t', u.c_name,
            '\'\t', ifnull(u.c_cover, ''),
            '\'\t', ifnull(m.c_text, ''),
            '\'\t', ifnull(m.c_image, ''))
           order by m.i_weight desc, m.c_mid desc
           separator '\'\n')
      into c_desc
      from tmp_message m, tbl_user u
     where m.i_uid = u.i_uid;

end //
delimiter ;

-- INST_QUERY_MESSAGE_FOCUS        = 0x00002006
delete from tbl_instruction where i_inst = (conv('00002006', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00002006', 16, 10) + 0),
    'st',
    6,
    "select f.c_mid, u.i_uid, u.c_name, u.c_cover, f.t_time, f.i_type from tbl_message_$geohash6_focus f, tbl_user u where f.i_uid = u.i_uid and f.c_mid = \"$mid\""
);

-- INST_QUERY_MESSAGE_REPLY        = 0x00002007
delete from tbl_instruction where i_inst = (conv('00002007', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00002007', 16, 10) + 0),
    'sp',
    2,
    "sp_query_message_reply(?, ?, \"$mid\")"
);


delimiter //
drop procedure if exists sp_query_message_reply;
create procedure sp_query_message_reply (
    out i_code  integer,
    out c_desc  mediumtext,
    in  mid     varchar(128)
)
begin

    declare dc_statement    varchar(400)    default null;
    declare dc_mid          varchar(128)    default null;

    declare done            integer         default 0;
    declare rs_mid          cursor for select c_mid from tmp_message;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    delete from tmp_message;
    set dc_statement = concat(
            'insert into tmp_message (c_mid) ',
            'select c_rid ',
            '  from tbl_message_', left(mid, 6), '_reply ',
            " where c_mid = \"", mid, "\""
    );
    set @s = dc_statement;
    prepare s from @s;
    execute s;
    deallocate prepare s;

    open rs_mid;
    fetch rs_mid into dc_mid;
    while done = 0 do

        set dc_statement = concat(
            'update tmp_message t, tbl_message_', left(dc_mid, 6), ' m',
            '   set t.t_time    = m.t_time,',
            '       t.i_uid     = m.i_uid,',
            '       t.i_coosys  = m.i_coosys,',
            '       t.i_lat     = m.i_lat,',
            '       t.i_lng     = m.i_lng,',
            '       t.c_geohash = m.c_geohash,',
            '       t.c_text    = m.c_text,',
            '       t.c_image   = m.c_image ',
            ' where t.c_mid = m.c_mid ',
            "   and t.c_mid = \"", dc_mid, "\""
        );
        set @s = dc_statement;
        prepare s from @s;
        execute s;
        deallocate prepare s;

        fetch rs_mid into dc_mid;
    end while;
    close rs_mid;

    set i_code = 0;
    select group_concat(concat(
                    m.c_mid,
            '\'\t', m.t_time,
            '\'\t', u.i_uid,
            '\'\t', u.c_name,
            '\'\t', ifnull(u.c_cover, ''),
            '\'\t', ifnull(m.c_text, ''),
            '\'\t', ifnull(m.c_image, ''))
           order by m.t_time desc, m.c_mid desc
           separator '\'\n')
      into c_desc
      from tmp_message m, tbl_user u
     where m.i_uid = u.i_uid;

end //
delimiter ;



