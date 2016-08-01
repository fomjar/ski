delimiter //
drop procedure if exists sp_query_game_all //
create procedure sp_query_game_all (
    out i_code  integer,
    out c_desc  mediumblob
)  
begin
    declare i_gid           integer         default -1;     -- 游戏ID
    declare c_name_zh_cn    varchar(64)     default null;   -- 简体中文名
    declare c_name_zh_hk    varchar(64)     default null;   -- 繁体中文名
    declare c_name_en       varchar(64)     default null;   -- 英文名
    declare c_name_ja       varchar(64)     default null;   -- 日文名
    declare c_name_ko       varchar(64)     default null;   -- 韩文名
    declare c_name_other    varchar(64)     default null;   -- 其他语言名
    declare c_platform      varchar(16)     default null;   -- 游戏和账号所属平台：PS4、XBOX ONE、PS5、XBOX TWO、PS3、XBOX 360
    declare c_category      varchar(64)     default null;   -- 分类(多种)
    declare c_language      varchar(32)     default null;   -- 语言(多种)
    declare c_size          varchar(16)     default null;   -- 大小
    declare c_vendor        varchar(64)     default null;   -- 发行商
    declare t_sale          date            default null;   -- 发售日期
    declare c_url_icon      varchar(250)    default null;   -- 图标URL
    declare c_url_cover     varchar(250)    default null;   -- 封面URL
    declare c_url_poster    text            default null;   -- 海报(多个)
    declare c_introduction  text            default null;   -- 游戏说明
    declare c_version       text            default null;   -- 版本说明

    declare done            integer default 0;
    declare rs              cursor for
                            select g.i_gid, g.c_name_zh_cn, g.c_name_zh_hk, g.c_name_en, g.c_name_ja, g.c_name_ko, g.c_name_other, g.c_platform, g.c_category, g.c_language, g.c_size, g.c_vendor, g.t_sale, g.c_url_icon, g.c_url_cover, g.c_url_poster, g.c_introduction, g.c_version
                              from tbl_game g
                             order by g.c_name_zh_cn;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_gid, c_name_zh_cn, c_name_zh_hk, c_name_en, c_name_ja, c_name_ko, c_name_other, c_platform, c_category, c_language, c_size, c_vendor, t_sale, c_url_icon, c_url_cover, c_url_poster, c_introduction, c_version;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_gid, 10, 16),
                '\t',
                ifnull(c_name_zh_cn, ''),
                '\t',
                ifnull(c_name_zh_hk, ''),
                '\t',
                ifnull(c_name_en, ''),
                '\t',
                ifnull(c_name_ja, ''),
                '\t',
                ifnull(c_name_ko, ''),
                '\t',
                ifnull(c_name_other, ''),
                '\t',
                ifnull(c_platform, ''),
                '\t',
                ifnull(c_category, ''),
                '\t',
                ifnull(c_language, ''),
                '\t',
                ifnull(c_size, ''),
                '\t',
                ifnull(c_vendor, ''),
                '\t',
                ifnull(t_sale, ''),
                '\t',
                ifnull(c_url_icon, ''),
                '\t',
                ifnull(c_url_cover, ''),
                '\t',
                ifnull(c_url_poster, ''),
                '\t',
                ifnull(c_introduction, ''),
                '\t',
                ifnull(c_version, '')
        );

        fetch rs into i_gid, c_name_zh_cn, c_name_zh_hk, c_name_en, c_name_ja, c_name_ko, c_name_other, c_platform, c_category, c_language, c_size, c_vendor, t_sale, c_url_icon, c_url_cover, c_url_poster, c_introduction, c_version;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
