delimiter // 
drop procedure if exists sp_query_game_account_all //   
create procedure sp_query_game_account_all (
    out i_code  integer,
    out c_desc  mediumblob,
    in  gid     integer     -- 游戏ID
)  
begin
    declare i_gaid      integer     default -1;
    declare c_user      varchar(32) default null;
    declare c_pass_a    varchar(32) default null;
    declare c_pass_b    varchar(32) default null;
    declare c_pass_curr varchar(32) default null;
    declare t_birth     date        default null;

    declare done        integer default 0;
    declare rs          cursor for
                        select ga.i_gaid, ga.c_user, ga.c_pass_a, ga.c_pass_b, ga.c_pass_curr, ga.t_birth
                          from tbl_game_account
                         where ga.i_gid = gid
                         order by ga.i_gid, ga.i_gaid;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_gaid, c_user, c_pass_a, c_pass_b, c_pass_curr, t_birth;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_gaid, 10, 16),
                '\t',
                ifnull(c_user, ''),
                '\t',
                ifnull(c_pass_a, ''),
                '\t',
                ifnull(c_pass_b, ''),
                '\t',
                ifnull(c_pass_curr, ''),
                '\t',
                ifnull(t_birth, '')
        );

        fetch rs into i_gaid, c_user, c_pass_a, c_pass_b, c_pass_curr, t_birth;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
