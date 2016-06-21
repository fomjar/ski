delete from tbl_instruction where i_inst = (conv('00002009', 16, 10) + 0);
insert into tbl_instruction values((conv('00002009', 16, 10) + 0), 'sp', 2, "sp_query_platform_account(?, ?, $paid)");

-- 查询游戏
delimiter //
drop procedure if exists sp_query_platform_account // 
create procedure sp_query_platform_account (
    out i_code  integer,
    out c_desc  mediumblob,
    in  paid    integer
)
begin
    if paid is null then
        call sp_query_platform_account_all(i_code, c_desc);
    else
        call sp_query_platform_account_by_paid(i_code, c_desc, paid);
    end if;

    set c_desc = convert(c_desc using utf8);
end //
delimiter ;
