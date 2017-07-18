
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

