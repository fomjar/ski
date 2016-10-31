

-- INST_QUERY_USER_STATE           = 0x00002004
delete from tbl_instruction where i_inst = (conv('00002004', 16, 10) + 0);
insert into tbl_instruction(
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


