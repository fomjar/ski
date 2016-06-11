delimiter // 
drop procedure if exists sp_query_game_rent_price_all //   
create procedure sp_query_game_rent_price_all (
    out i_code  integer,
    out c_desc  mediumblob
)  
begin
    declare i_gid   integer         default -1;
    declare i_type  tinyint         default -1;
    declare i_price decimal(4, 2)   default 0;

    declare done    integer default 0.0;
    declare rs      cursor for
                    select grp.i_gid, grp.i_type, grp.i_price
                      from tbl_game_rent_price grp
                     order by grp.i_gid, grp.i_type;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_gid, i_type, i_price;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_gid, 10, 16),
                '\t',
                i_type,
                '\t',
                ifnull(i_price, 0.0)
        );

        fetch rs into i_gid, i_type, i_price;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
