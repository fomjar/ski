delimiter // 
drop procedure if exists sp_query_order_all //   
create procedure sp_query_order_all (
    out i_code  integer,
    out c_desc  mediumblob,
    in  type    varchar(16)
)  
begin  
    declare i_prod_type integer default 0;
    declare i_pid       integer default 0;
    declare i_inst_type integer default 0;
    declare i_inst_id   integer default 0;
    declare i_gaid      integer default 0;
    declare c_name      varchar(64);
    declare c_user      varchar(32);
    declare c_pass_cur  varchar(32);
    declare c_pass_a    varchar(32);
    declare c_pass_b    varchar(32);
    declare i           integer default 0;
    declare done        integer default 0;

    declare rs          cursor for
                        select p.i_prod_type, p.i_pid, p.i_inst_type, p.i_inst_id, ga.i_gaid, ga.c_user, ga.c_pass_cur, ga.c_pass_a, ga.c_pass_b
                          from tbl_order o, tbl_order_product p, tbl_game_account ga
                         where o.c_poid = p.c_poid
                           and p.i_inst_id = ga.i_gaid;
                        -- and o.c_caid = user; for query by user
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_prod_type, i_pid, i_inst_type, i_inst_id, i_gaid, c_user, c_pass_cur, c_pass_a, c_pass_b;
    /* 遍历数据表 */
    repeat
        if c_desc is null then
            set c_desc = '';
        else
            set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_prod_type, 10, 16),
                '\t',
                conv(i_pid, 10, 16),
                '\t',
                conv(i_inst_type, 10, 16),
                '\t',
                conv(i_inst_id, 10, 16),
                '\t',
                conv(i_gaid, 10, 16),
                '\t',
                c_user,
                '\t',
                c_pass_cur,
                '\t',
                c_pass_a,
                '\t',
                c_pass_b
        );

    fetch rs into i_prod_type, i_pid, i_inst_type, i_inst_id, i_gaid, c_user, c_pass_cur, c_pass_a, c_pass_b;
    until done end repeat;
    /* 关闭游标 */
    close rs;

    set i_code=0;
end //  
delimiter ; 
