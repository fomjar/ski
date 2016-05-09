delimiter // 
drop procedure if exists sp_query_order_by_user //   
create procedure sp_query_order_by_user (
    out i_code  integer,
    out c_desc  mediumblob,
    in  user    varchar(64)
)  
begin
    declare i_pid       integer     default -1;
    declare i_prod_type integer     default -1;
    declare c_prod_name varchar(64) default null;
    declare i_prod_inst integer     default -1;
    declare c_user      varchar(32) default null;
    declare c_pass_a    varchar(32) default null;
    declare c_pass_b    varchar(32) default null;
    declare c_pass_curr varchar(32) default null;
    declare t_birth     date;

    declare done        integer default 0;
    declare rs          cursor for
                        select p.i_pid, p.i_prod_type, p.c_prod_name, p.i_prod_inst, ga.c_user, ga.c_pass_a, ga.c_pass_b, ga.c_pass_curr, ga.t_birth
                          from tbl_order o, tbl_order_product p, tbl_game_account ga
                         where o.c_poid = p.c_poid
                           and p.i_pid = ga.i_pid
                           and o.c_caid = user;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_pid, i_prod_type, c_prod_name, i_prod_inst, c_user, c_pass_a, c_pass_b, c_pass_curr, t_birth;
    /* 遍历数据表 */
    repeat
        if c_desc is null then
            set c_desc = '';
        else
            set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_pid, 10, 16),
                '\t',
                conv(i_prod_type, 10, 16),
                '\t',
                c_prod_name,
                '\t',
                conv(i_prod_inst, 10, 16),
                '\t',
                c_user,
                '\t',
                c_pass_a,
                '\t',
                c_pass_b,
                '\t',
                c_pass_curr,
                '\t',
                t_birth
        );

    fetch rs into i_pid, i_prod_type, c_prod_name, i_prod_inst, c_user, c_pass_a, c_pass_b, c_pass_curr, t_birth;
    until done end repeat;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
