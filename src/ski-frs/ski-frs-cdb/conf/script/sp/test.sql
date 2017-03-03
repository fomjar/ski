
delimiter //
drop function if exists transvection //
create function transvection (
    v1  text,
    v2  text
)
returns float
begin
	
	declare    t   float   default 0;
	
	
end //
delimiter ;
