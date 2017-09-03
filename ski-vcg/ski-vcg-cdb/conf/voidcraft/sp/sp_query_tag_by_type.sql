delimiter //
drop procedure if exists sp_query_tag_by_type //
create procedure sp_query_tag_by_type (
    out i_code  integer,
    out c_desc  mediumblob,
    in  type    tinyint
)  
begin  
    declare i_type      tinyint;
    declare i_instance  integer;
    declare c_tag       varchar(16);

    declare done        integer default 0;
    declare rs          cursor for
                        select t.i_type, t.i_instance, t.c_tag
                          from tbl_tag t
                         where t.i_type = type
                         order by t.i_type, t.i_instance, t.c_tag;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_type, i_instance, c_tag;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_type, 10, 16),
                '\t',
                conv(i_instance, 10, 16),
                '\t',
                ifnull(c_tag, '')
        );

        fetch rs into i_type, i_instance, c_tag;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
