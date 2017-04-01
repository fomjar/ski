
-- 向量内积
delimiter //
drop function if exists transvection //
create function transvection (
    v1  varchar(1600),   -- 向量1
    v2  varchar(1600),   -- 向量2
    d   integer -- 向量维数
)
returns double
begin
	
	declare    t       double  default 0;  -- transvection
    declare    i       tinyint default 1;
	declare    v1d     double  default 0;
	declare    v2d     double  default 0;
	
	while i <= d do
       set v1d = substring_index(substring_index(v1, ' ', i), ' ', -1);
       set v2d = substring_index(substring_index(v2, ' ', i), ' ', -1);
       
       set t = t + v1d * v2d;
       set i = i + 1;
	end while;
	
	return t;
	
end //
delimiter ;





delimiter //
drop procedure if exists sp_build_fv //
create procedure sp_build_fv ()
begin
	
	declare di_pid integer default 0;
	declare dc_fv0 varchar(1600);
    declare i      tinyint default 1;
    declare vd     double  default 0;
	
    declare done   integer default 0;
    declare rs     cursor for
                   select i_pid, c_fv0
                     from tbl_pic
                    where c_fv0 is not null;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;
    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into di_pid, dc_fv0;
    /* 遍历数据表 */
    while (done = 0) do
    
        set i = 1;
	    while i <= 88 do
	       set vd = substring_index(substring_index(dc_fv0, ' ', i), ' ', -1);
	       insert into tbl_pic_fv (
	           i_pid,
	           i_fvsn,
	           i_fv
	       ) values (
	           di_pid,
	           i,
	           vd
	       );
	       
	       set i = i + 1;
	    end while;
    
        fetch rs into di_pid, dc_fv0;
    end while;
    /* 关闭游标 */
    close rs;
	
end //
delimiter ;

