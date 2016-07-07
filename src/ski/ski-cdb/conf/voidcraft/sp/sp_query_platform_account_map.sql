delete from tbl_instruction where i_inst = (conv('0000200A', 16, 10) + 0);
insert into tbl_instruction values((conv('0000200A', 16, 10) + 0), 'sp', 2, "sp_query_platform_account_map(?, ?, $paid, $caid)");

-- 查询游戏
delimiter //
drop procedure if exists sp_query_platform_account_map //
create procedure sp_query_platform_account_map (
    out i_code  integer,
    out c_desc  mediumblob,
    in  paid    integer,
    in  caid    integer
)
begin
    if paid is null and caid is null then
        call sp_query_platform_account_map_all(i_code, c_desc);
    elseif paid is not null then
        call sp_query_platform_account_map_by_paid(i_code, c_desc, paid);
    elseif caid is not null then
        call sp_query_platform_account_map_by_caid(i_code, c_desc, caid);
    else
        set i_code = 2;
        set c_desc = 'can not determin which to query by, paid or caid?';
    end if;
end //
delimiter ;
