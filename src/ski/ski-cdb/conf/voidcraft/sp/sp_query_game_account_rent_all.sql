delimiter // 
drop procedure if exists sp_query_game_account_rent_all //
create procedure sp_query_game_account_rent_all (
    out i_code  integer,
    out c_desc  mediumblob
)  
begin
    declare i_gaid      integer     default -1;
    declare i_type      integer     default -1;
    declare i_caid      integer     default -1;
    declare i_state     integer     default -1;
    declare t_change    datetime    default null;

    declare done        integer default 0;
    declare rs          cursor for
                        select gar.i_gaid, gar.i_type, gar.i_caid, gar.i_state, gar.t_change
                          from tbl_game_account_rent gar
                         order by gar.i_caid, gar.t_change desc;

    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_gaid, i_type, i_caid, i_state, t_change;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_gaid, 10, 16),
                '\t',
                conv(i_type, 10, 16),
                '\t',
                conv(i_caid, 10, 16),
                '\t',
                conv(i_state, 10, 16),
                '\t',
                ifnull(t_change, '')
        );

        fetch rs into i_gaid, i_type, i_caid, i_state, t_change;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
