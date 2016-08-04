delete from tbl_instruction where i_inst = (conv('00002401', 16, 10) + 0);
insert into tbl_instruction values((conv('00002401', 16, 10) + 0), 'sp', 2, "sp_update_game(?, ?, $gid, \"$name_zh_cn\", \"$name_zh_hk\", \"$name_en\", \"$name_ja\", \"$name_ko\", \"$name_other\", \"$platform\", \"$category\", \"$language\", \"$size\", \"$vendor\", \"$sale\", \"$url_icon\", \"$url_cover\", \"$url_poster\", \"$introduction\", \"$version\", \"$vedio\")");

-- 更新游戏
delimiter //
drop procedure if exists sp_update_game //
create procedure sp_update_game (
    out i_code          integer,
    out c_desc          mediumblob,
    in  gid             integer,        -- 游戏ID
    in  name_zh_cn      varchar(64),    -- 简体中文名
    in  name_zh_hk      varchar(64),    -- 繁体中文名
    in  name_en         varchar(64),    -- 英文名
    in  name_ja         varchar(64),    -- 日文名
    in  name_ko         varchar(64),    -- 韩文名
    in  name_other      varchar(64),    -- 其他语言名
    in  platform        varchar(16),    -- 游戏和账号所属平台：PS4、XBOX ONE、PS5、XBOX TWO、PS3、XBOX 360
    in  category        varchar(64),    -- 分类(多种)
    in  language        varchar(32),    -- 语言(多种)
    in  size            varchar(16),    -- 大小
    in  vendor          varchar(64),    -- 发行商
    in  sale            date,           -- 发售日期
    in  url_icon        varchar(250),   -- 图标URL
    in  url_cover       varchar(250),   -- 封面URL
    in  url_poster      text,           -- 海报(多个)
    in  introduction    text,           -- 游戏说明
    in  version         text,           -- 版本说明
    in  vedio           text            -- 视频脚本
)
begin
    declare di_gid      integer default -1;
    declare di_count    integer default -1;

    if gid is null then
        select count(1)
          into di_count
          from tbl_game;

        if di_count = 0 then
            set di_gid = 1;
        else
            select max(i_gid)
              into di_gid
              from tbl_game;

            set di_gid = di_gid + 1;
        end if;

        insert into tbl_game (
            i_gid,
            c_name_zh_cn,
            c_name_zh_hk,
            c_name_en,
            c_name_ja,
            c_name_ko,
            c_name_other,
            c_platform,
            c_category,
            c_language,
            c_size,
            c_vendor,
            t_sale,
            c_url_icon,
            c_url_cover,
            c_url_poster,
            c_introduction,
            c_version,
            c_vedio
        ) values (
            di_gid,
            name_zh_cn,
            name_zh_hk,
            name_en,
            name_ja,
            name_ko,
            name_other,
            platform,
            category,
            language,
            size,
            vendor,
            sale,
            url_icon,
            url_cover,
            url_poster,
            introduction,
            version,
            vedio
        );
        
        set c_desc = conv(di_gid, 10, 16);
    else
        select count(1)
          into di_count
          from tbl_game
         where i_gid = gid;

        if di_count <= 0 then
            insert into tbl_game (
                i_gid,
                c_name_zh_cn,
                c_name_zh_hk,
                c_name_en,
                c_name_ja,
                c_name_ko,
                c_name_other,
                c_platform,
                c_category,
                c_language,
                c_size,
                c_vendor,
                t_sale,
                c_url_icon,
                c_url_cover,
                c_url_poster,
                c_introduction,
                c_version,
                c_vedio
            ) values (
                gid,
                name_zh_cn,
                name_zh_hk,
                name_en,
                name_ja,
                name_ko,
                name_other,
                platform,
                category,
                language,
                size,
                vendor,
                sale,
                url_icon,
                url_cover,
                url_poster,
                introduction,
                version,
                vedio
            );
        else
            if name_zh_cn is not null then
                update tbl_game
                   set c_name_zh_cn = name_zh_cn
                 where i_gid = gid;
            end if;
            if name_zh_hk is not null then
                update tbl_game
                   set c_name_zh_hk = name_zh_hk
                 where i_gid = gid;
            end if;
            if name_en is not null then
                update tbl_game
                   set c_name_en = name_en
                 where i_gid = gid;
            end if;
            if name_ja is not null then
                update tbl_game
                   set c_name_ja = name_ja
                 where i_gid = gid;
            end if;
            if name_ko is not null then
                update tbl_game
                   set c_name_ko = name_ko
                 where i_gid = gid;
            end if;
            if name_other is not null then
                update tbl_game
                   set c_name_other = name_other
                 where i_gid = gid;
            end if;
            if platform is not null then
                update tbl_game
                   set c_platform = platform
                 where i_gid = gid;
            end if;
            if category is not null then
                update tbl_game
                   set c_category = category
                 where i_gid = gid;
            end if;
            if language is not null then
                update tbl_game
                   set c_language = language
                 where i_gid = gid;
            end if;
            if size is not null then
                update tbl_game
                   set c_size = size
                 where i_gid = gid;
            end if;
            if vendor is not null then
                update tbl_game
                   set c_vendor = vendor
                 where i_gid = gid;
            end if;
            if sale is not null then
                update tbl_game
                   set t_sale = sale
                 where i_gid = gid;
            end if;
            if url_icon is not null then
                update tbl_game
                   set c_url_icon = url_icon
                 where i_gid = gid;
            end if;
            if url_cover is not null then
                update tbl_game
                   set c_url_cover = url_cover
                 where i_gid = gid;
            end if;
            if url_poster is not null then
                update tbl_game
                   set c_url_poster = url_poster
                 where i_gid = gid;
            end if;
            if introduction is not null then
                update tbl_game
                   set c_introduction = introduction
                 where i_gid = gid;
            end if;
            if version is not null then
                update tbl_game
                   set c_version = version
                 where i_gid = gid;
            end if;
            if vedio is not null then
                update tbl_game
                   set c_vedio = vedio
                 where i_gid = gid;
            end if;
        end if;

        set c_desc = conv(gid, 10, 16);
    end if;
    set i_code = 0;
end //
delimiter ;
