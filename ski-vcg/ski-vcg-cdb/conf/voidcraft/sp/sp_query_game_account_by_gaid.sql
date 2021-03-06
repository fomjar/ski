delimiter //
drop procedure if exists sp_query_game_account_by_gaid //
create procedure sp_query_game_account_by_gaid (
    out i_code  integer,
    out c_desc  mediumblob,
    in  gaid    integer     -- 游戏账号ID
)  
begin
    declare i_gaid      integer     default -1;
    declare c_remark    varchar(64) default null;
    declare c_user      varchar(32) default null;
    declare c_pass      varchar(32) default null;
    declare c_name      varchar(32) default null;
    declare t_birth     date        default null;
    declare t_create    datetime    default null;

    declare done        integer default 0;
    declare rs          cursor for
                        select ga.i_gaid, ga.c_remark, ga.c_user, ga.c_pass, ga.c_name, ga.t_birth, ga.t_create
                          from tbl_game_account ga
                         where ga.i_gaid = gaid
                         order by ga.i_gaid;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_gaid, c_remark, c_user, c_pass, c_name, t_birth, t_create;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_gaid, 10, 16),
                '\t',
                ifnull(c_remark, ''),
                '\t',
                ifnull(c_user, ''),
                '\t',
                ifnull(c_pass, ''),
                '\t',
                ifnull(c_name, ''),
                '\t',
                ifnull(t_birth, ''),
                '\t',
                ifnull(t_create, '')
        );

        fetch rs into i_gaid, c_remark, c_user, c_pass, c_name, t_birth, t_create;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
