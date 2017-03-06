
delete from tbl_instruction where i_inst = (conv('00001001', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00001001', 16, 10) + 0),
    'st',
    0,
    "insert into tbl_pic (c_did, c_name, t_time, i_size, i_type, c_desc1, c_desc2, c_desc3) values (\"$did\", \"$name\", \"$time\", $size, $type, \"$desc1\", \"$desc2\", \"$desc3\")"
);
