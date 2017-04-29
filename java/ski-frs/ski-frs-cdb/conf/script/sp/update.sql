
delete from tbl_instruction where i_inst = (conv('00001000', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00001000', 16, 10) + 0),
    'sp',
    2,
    "sp_update_pic(?, ?, $pid, \"$did\", \"$name\", \"$time\", $size, $type, \"$path\", \"$fv\", $vd)"
);
delimiter //
drop procedure if exists sp_update_pic //
create procedure sp_update_pic (
    out i_code  integer,
    out c_desc  text,
    in  _pid    integer, -- 图片编号
    in  _did    varchar(64),                -- 设备编号
    in  _name   varchar(64),                -- 名称
    in  _time   datetime,                   -- 生成时间
    in  _size   tinyint,                    -- 尺寸：0 - 大图(全图)，1 - 中图(半身)，2 - 小图(头像)
    in  _type   tinyint,                    -- 类型：0 - 人物，1 - 汽车
    in  _path   varchar(512),               -- 路径
    in  _fv     varchar(1600),              -- 特征向量
    in  _vd     tinyint
)
begin
    
	declare di_pid integer default 0;
    declare i      tinyint default 1;
    declare v      double  default 0;
    
    insert into tbl_pic (
        i_pid,
        c_did,
        c_name,
        t_time,
        i_size,
        i_type,
        c_path
    ) values (
        _pid,
        _did,
        _name,
        ifnull(_time, now()),
        _size,
        _type,
        _path
    );
    
    if _pid is null then
        select max(i_pid)
          into di_pid
          from tbl_pic;
    else
        set di_pid = _pid;
    end if;
    
    if _fv is not null and _vd is not null then
	    while i <= _vd do
	       set v = substring_index(substring_index(_fv, ' ', i), ' ', -1);
	       insert into tbl_pic_fv (
	           i_pid,
	           i_fvsn,
	           i_fv
	       ) values (
	           di_pid,
	           i,
	           v
	       );
	       
	       set i = i + 1;
	    end while;
    end if;
    
    set i_code = 0;
    set c_desc = conv(di_pid, 10, 16);
    
end //
delimiter ;





delete from tbl_instruction where i_inst = (conv('00001001', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00001001', 16, 10) + 0),
    'sp',
    2,
    "sp_update_pic_del(?, ?, $pid)"
);
delimiter //
drop procedure if exists sp_update_pic_del //
create procedure sp_update_pic_del (
    out i_code  integer,
    out c_desc  text,
    in  _pid    integer
)
begin
	
	declare di_count   integer default 0;
	
	select count(1)
	  into di_count
	  from tbl_pic
	 where i_pid = _pid;
	
	delete from tbl_pic
	 where i_pid = _pid;
	
	delete from tbl_pic_fv
	 where i_pid = _pid;
	 
	set i_code = 0;
	set c_desc = concat(di_count, '');
	
end //
delimiter ;




delete from tbl_instruction where i_inst = (conv('00001010', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00001010', 16, 10) + 0),
    'st',
    0,
    "replace into tbl_sub_lib (i_slid, c_name, i_type, t_time) values ($slid, \"$name\", $type, ifnull(\"$time\", now()))"
);




delete from tbl_instruction where i_inst = (conv('00001011', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00001011', 16, 10) + 0),
    'sp',
    2,
    "sp_update_sub_lib_del(?, ?, $slid)"
);
delimiter //
drop procedure if exists sp_update_sub_lib_del //
create procedure sp_update_sub_lib_del (
    out i_code  integer,
    out c_desc  text,
    in  _slid   integer
)
begin
	
	declare di_type    tinyint default 0;
	declare di_count   integer default 0;
	
	select count(1)
	  into di_count
	  from tbl_sub_lib
	 where i_slid = _slid;
	
	select i_type
	  into di_type
	  from tbl_sub_lib
	 where i_slid = _slid;
	
	if di_type = 0 then    -- man
       delete pic
         from tbl_pic pic, tbl_sub_man_pic smp, tbl_sub_man sm
        where pic.i_pid = smp.i_pid
          and smp.i_smid = sm.i_smid
          and sm.i_slid = _slid;
          
       delete fv
         from tbl_pic_fv fv, tbl_sub_man_pic smp, tbl_sub_man sm
        where fv.i_pid = smp.i_pid
          and smp.i_smid = sm.i_smid
          and sm.i_slid = _slid;
          
       delete smp
         from tbl_sub_man_pic smp, tbl_sub_man sm
        where smp.i_smid = sm.i_smid
          and sm.i_slid = _slid;
          
	   delete from tbl_sub_man
	    where i_slid = _slid;
       
	   delete from tbl_sub_lib
	    where i_slid = _slid;
	
	-- elseif di_type = 1 then    -- car
	end if;
	
	set i_code = 0;
	set c_desc = concat(di_count, '');
	
end //
delimiter ;




delete from tbl_instruction where i_inst = (conv('00001020', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00001020', 16, 10) + 0),
    'sp',
    2,
    "sp_update_sub_man(?, ?, $smid, $slid, \"$time\", \"$name\", $gender, \"$birth\", \"$idno\", \"$phone\", \"$addr\")"
);
delimiter //
drop procedure if exists sp_update_sub_man //
create procedure sp_update_sub_man (
    out i_code  integer,
    out c_desc  text,
    in  _smid      integer, -- 主体编号
    in  _slid      integer,                    -- 主体库编号
    in  _time      datetime,                   -- 创建时间
    in  _name      varchar(32),                -- 姓名
    in  _gender    tinyint,                    -- 性别：0 - 女，1 - 男
    in  _birth     date,                       -- 生日
    in  _idno      varchar(32),                -- 身份证号
    in  _phone     varchar(32),                -- 电话
    in  _addr      varchar(256)                -- 地址
)
begin
    
    declare di_smid integer default 0;
    
    insert into tbl_sub_man (
        i_smid,
        i_slid,
        t_time,
        c_name,
        i_gender,
        t_birth,
        c_idno,
        c_phone,
        c_addr
    ) values (
        _smid,
        _slid,
        ifnull(_time, now()),
        _name,
        _gender,
        _birth,
        _idno,
        _phone,
        _addr
    );
    
    if _smid is null then
        select max(i_smid)
          into di_smid
          from tbl_sub_man;
    else
        set di_smid = _smid;
    end if;
    
    set i_code = 0;
    set c_desc = conv(di_smid, 10, 16);
    
end //
delimiter ;




delete from tbl_instruction where i_inst = (conv('00001021', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00001021', 16, 10) + 0),
    'sp',
    2,
    "sp_update_sub_man_del(?, ?, $smid)"
);
delimiter //
drop procedure if exists sp_update_sub_man_del //
create procedure sp_update_sub_man_del (
    out i_code  integer,
    out c_desc  text,
    in  _smid   integer
)
begin
	
	declare di_count   integer default 0;
	
	select count(1)
	  into di_count
	  from tbl_sub_man
	 where i_smid = _smid;
	 
    delete pic
      from tbl_pic pic, tbl_sub_man sm, tbl_sub_man_pic smp
     where pic.i_pid = smp.i_pid
       and sm.i_smid = smp.i_smid
       and sm.i_smid = _smid;
    
    delete fv
      from tbl_pic_fv fv, tbl_sub_man sm, tbl_sub_man_pic smp
     where fv.i_pid = smp.i_pid
       and sm.i_smid = smp.i_smid
       and sm.i_smid = _smid;
    
    delete from tbl_sub_man
     where i_smid = _smid;
     
	delete from tbl_sub_man_pic
	 where i_smid = _smid;
	
	set i_code = 0;
	set c_desc = concat(di_count, '');
	
end //
delimiter ;





delete from tbl_instruction where i_inst = (conv('00001022', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00001022', 16, 10) + 0),
    'st',
    0,
    "insert into tbl_sub_man_pic (i_smid, i_pid) values ($smid, $pid)"
);



delete from tbl_instruction where i_inst = (conv('00001030', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00001030', 16, 10) + 0),
    'st',
    0,
    "replace into tbl_dev (c_did, c_path, t_time, c_ip, i_port, c_user, c_pass) values (\"$did\", \"$path\", ifnull(\"$time\", now()), \"$ip\", $port, \"$user\", \"$pass\")"
);



delete from tbl_instruction where i_inst = (conv('00001031', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00001031', 16, 10) + 0),
    'st',
    0,
    "delete from tbl_dev where c_did = \"$did\""
);



