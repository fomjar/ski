delimiter //
drop procedure if exists sp_rebuild_game_account_rent //
create procedure sp_rebuild_game_account_rent (
)  
begin
    declare gaid    integer;
    declare type    tinyint;
    declare caid    integer;
    declare state   tinyint;
    declare _change datetime;
    declare done            integer default 0;
    declare rs              cursor for
                            select conv(c.c_arg0, 16, 10) as i_gaid, (case c.c_arg1 when 'A' then 0 when 'B' then 1 end) as i_type, o.i_caid, 1 as i_state, c.t_begin as _change
                              from tbl_order o, tbl_commodity c
                             where o.i_oid = c.i_oid
                               and c.t_end is null
                             order by c.t_begin;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    delete from tbl_game_account_rent;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into gaid, type, caid, state, _change;
    /* 遍历数据表 */
    while (done = 0) do
        insert into tbl_game_account_rent (
            i_gaid,
            i_type,
            i_caid,
            i_state,
            t_change
        ) values (
            gaid,
            type,
            caid,
            state,
            _change
        );
        fetch rs into gaid, type, caid, state, _change;
    end while;
    /* 关闭游标 */
    close rs;
end //  
delimiter ; 
