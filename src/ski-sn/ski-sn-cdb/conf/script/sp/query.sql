

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
    'st',
    2,
    "sp_query_message(?, ?, $lat, $lng, \"$geohash\")"
);

delimiter //
drop procedure if exists sp_query_message;
create procedure sp_query_message (
    out i_code  integer,
    out c_desc  mediumtext,
    in  lat     decimal(24, 20),
    in  lng     decimal(24, 20),
    in  geohash varchar(16)
)
begin


    create temporary table tmp_geohash (c_geohash varchar(16));
    call sp_generate_geohash(left(geohash, 6));

    create temporary table tmp_message (c_mid varchar(128), i_weight integer);

    

    drop table tmp_message;
    drop table tmp_geohash;

end //
delimiter ;

