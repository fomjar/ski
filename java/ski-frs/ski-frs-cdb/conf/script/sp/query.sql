
delete from tbl_instruction where i_inst = (conv('00002000', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00002000', 16, 10) + 0),
    'st',
    7,
    "select i_pid, c_did, c_name, t_time, i_size, i_type, c_path from tbl_pic where i_pid = $pid"
);




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
    "sp_pic_fv_i(?, ?, \"$fv\", $vd)"
);
-- 计算特征向量初始化
delimiter //
drop procedure if exists sp_pic_fv_i //
create procedure sp_pic_fv_i (
    out i_code  integer,
    out c_desc  text,
    in  fv       varchar(1600),  -- 向量
    in  vd       integer         -- 向量维数
)
begin
    
    declare i   tinyint default 1;
    declare v   double  default 0;
    
    delete from tbl_pic_fv_tmp;
    
    while i <= vd do
       set v = substring_index(substring_index(fv, ' ', i), ' ', -1);
       insert into tbl_pic_fv_tmp (
           i_fvsn,
           i_fv
       ) values (
           i,
           v
       );
       
       set i = i + 1;
    end while;
    
    set i_code = 0;
    set c_desc = null;
    
end //
delimiter ;





delete from tbl_instruction where i_inst = (conv('00002002', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00002002', 16, 10) + 0),
    'st',
    8,
    "select pic.i_pid, pic.c_did, pic.c_name, pic.t_time, pic.i_size, pic.i_type, pic.c_path, sum(fv.i_fv * tmp.i_fv) as tv0 from tbl_pic pic left join tbl_pic_fv fv on pic.i_pid = fv.i_pid left join tbl_pic_fv_tmp tmp on fv.i_fvsn = tmp.i_fvsn group by pic.i_pid having tv0 > $tv limit $pf, $pt;"
);





delete from tbl_instruction where i_inst = (conv('00002010', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00002010', 16, 10) + 0),
    'st',
    5,
    "select l.i_slid, l.c_name, l.i_type, l.t_time, count(p.i_smid) from tbl_sub_lib l left join tbl_sub_man p on l.i_slid = p.i_slid group by l.i_slid"
);



delete from tbl_instruction where i_inst = (conv('00002030', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00002030', 16, 10) + 0),
    'st',
    6,
    "select c_did, c_path, t_time, c_ip, c_user, c_pass from tbl_dev"
);

