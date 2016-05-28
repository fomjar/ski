delimiter // 
drop procedure if exists sp_query_game_account_game_all //   
create procedure sp_query_game_account_game_all (
    out i_code  integer,
    out c_desc  mediumblob
)  
begin
    declare i_gaid      integer     default -1;
    declare i_gid       integer     default -1;

    declare done        integer default 0;
    declare rs          cursor for
                        select gag.i_gaid, gag.i_gid
                          from tbl_game_account_game gag
                         order by gag.i_gaid, gag.i_gid;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_gaid, i_gid;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_gaid, 10, 16),
                '\t',
                conv(i_gid, 10, 16)
        );

        fetch rs into i_gaid, i_gid;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
