
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
    "sp_update_pic(?, ?, $pid, \"$did\", \"$name\", \"$time\", $size, $type, \"$fv\", $vd)"
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
        i_type
    ) values (
        _pid,
        _did,
        _name,
        ifnull(_time, now()),
        _size,
        _type
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


delete from tbl_instruction where i_inst = (conv('00001002', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00001002', 16, 10) + 0),
    'st',
    0,
    "replace into tbl_sub_lib (i_slid, c_name, i_type, t_time) values ($slid, \"$name\", $type, now())"
);
