delimiter //
drop procedure if exists sp_query_game_all //
create procedure sp_query_game_all (
    out i_code  integer,
    out c_desc  mediumblob
)  
begin
    declare i_gid           integer         default -1;
    declare c_platform      varchar(16)     default null;
    declare c_country       varchar(32)     default null;
    declare c_url_icon      varchar(128)    default null;
    declare c_url_poster    varchar(128)    default null;
    declare c_url_buy       varchar(128)    default null;
    declare t_sale          date            default null;
    declare c_name_zh       varchar(64)     default null;
    declare c_name_en       varchar(64)     default null;

    declare done            integer default 0;
    declare rs              cursor for
                            select g.i_gid, g.c_platform, g.c_country, g.c_url_icon, g.c_url_poster, g.c_url_buy, g.t_sale, g.c_name_zh, g.c_name_en
                              from tbl_game g
                             order by g.c_name_zh;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_gid, c_platform, c_country, c_url_icon, c_url_poster, c_url_buy, t_sale, c_name_zh, c_name_en;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_gid, 10, 16),
                '\t',
                ifnull(c_platform, ''),
                '\t',
                ifnull(c_country, ''),
                '\t',
                ifnull(c_url_icon, ''),
                '\t',
                ifnull(c_url_poster, ''),
                '\t',
                ifnull(c_url_buy, ''),
                '\t',
                ifnull(t_sale, ''),
                '\t',
                ifnull(c_name_zh, ''),
                '\t',
                ifnull(c_name_en, '')
        );

        fetch rs into i_gid, c_platform, c_country, c_url_icon, c_url_poster, c_url_buy, t_sale, c_name_zh, c_name_en;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
