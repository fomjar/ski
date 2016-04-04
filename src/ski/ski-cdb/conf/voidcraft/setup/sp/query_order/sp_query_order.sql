delete from tbl_instruction where i_inst = (conv(00002002, 16, 10) + 0);
insert into tbl_instruction values((conv(00002002, 16, 10) + 0), 'sp', 2, "sp_query_order(?, ?, '$user')");

delimiter //
drop procedure if exists sp_query_order // 
create procedure sp_query_order (
    out i_code  integer,
    out c_desc  mediumblob,
    in  user    varchar(64) -- null：所有用户；其他：指定用户caid
)
begin
    if user is null then
        call sp_query_order_all(i_code, c_desc, type);
    else
        call sp_query_order_by_user(i_code, c_desc, user, type);
    end if;

    set c_desc = convert(c_desc using utf8);
end //
delimiter ;
