
delete from tbl_instruction where i_inst = (conv('00002001', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00002001', 16, 10) + 0),
    'st',
    9,
    "select i_pid, c_did, c_name, t_time, i_size, i_type, c_desc1, c_desc2, c_desc3 from tbl_pic where $con limit $pf, $pt"
);

delete from tbl_instruction where i_inst = (conv('00002002', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00002002', 16, 10) + 0),
    'st',
    10,
    "select i_pid, c_did, c_name, t_time, i_size, i_type, c_desc1, c_desc2, c_desc3, transvection(\"$fv\", c_desc1, $vd) as tv from tbl_pic where c_desc1 is not null having tv > $tv limit $pf, $pt"
);
