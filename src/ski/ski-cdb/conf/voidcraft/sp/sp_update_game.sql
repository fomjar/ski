delete from tbl_instruction where i_inst = (conv(00002403, 16, 10) + 0);
insert into tbl_instruction values((conv(00002403, 16, 10) + 0), 'sp', 2, "sp_update_game(?, ?, $gid, '$platform', '$country', '$url_icon', '$url_poster', '$url_buy', '$sale', '$name_cn', '$name_en')");

-- 更新游戏
delimiter //
drop procedure if exists sp_update_game // 
create procedure sp_update_game (
    out i_code      integer,
    out c_desc      mediumblob,
    in  gid         integer,        -- 游戏ID
    in  platform    varchar(16),    -- 游戏和账号所属平台：PS4、XBOX ONE、PS5、XBOX TWO、PS3、XBOX 360
    in  country     varchar(32),    -- 国家
    in  url_icon    varchar(128),   -- 图标URL
    in  url_poster  varchar(128),   -- 海报URL
    in  url_buy     varchar(128),   -- 采购网址
    in  sale        date,           -- 发售日期
    in  name_zh     varchar(64),    -- 中文名
    in  name_en     varchar(64)     -- 英文名
)
begin
    declare i_gid   integer default -1;
    declare i_count integer default -1;

    if gid is null then
        select max(i_gid)
          into i_gid
          from tbl_game;

        set i_gid = i_gid + 1;

        insert into tbl_game (
            i_gid,
            c_platform,
            c_country,
            c_url_icon,
            c_url_poster,
            c_url_buy,
            t_sale,
            c_name_zh,
            c_name_en
        )
        values (
            i_gid,
            platform,
            country,
            url_icon,
            url_poster,
            url_buy,
            sale,
            name_zh,
            name_en
        );
    else
        select count(1)
          into i_count
          from tbl_game
         where i_gid = gid;

        if i_count <= 0 then
            insert into tbl_game (
                i_gid,
                c_platform,
                c_country,
                c_url_icon,
                c_url_poster,
                c_url_buy,
                t_sale,
                c_name_zh,
                c_name_en
            )
            values (
                i_gid,
                platform,
                country,
                url_icon,
                url_poster,
                url_buy,
                sale,
                name_zh,
                name_en
            );
        else
            if platform is not null then
                update tbl_game
                   set c_platform = platform
                 where i_gid = gid;
            end if;
            if country is not null then
                update tbl_game
                   set c_country = country
                 where i_gid = gid;
            end if;
            if url_icon is not null then
                update tbl_game
                   set c_url_icon = url_icon
                 where i_gid = gid;
            end if;
            if url_poster is not null then
                update tbl_game
                   set c_url_poster = url_poster
                 where i_gid = gid;
            end if;
            if url_buy is not null then
                update tbl_game
                   set c_url_buy = url_buy
                 where i_gid = gid;
            end if;
            if sale is not null then
                update tbl_game
                   set t_sale = sale
                 where i_gid = gid;
            end if;
            if name_zh is not null then
                update tbl_game
                   set c_name_zh = name_zh
                 where i_gid = gid;
            end if;
            if name_en is not null then
                update tbl_game
                   set c_name_en = name_en
                 where i_gid = gid;
            end if;
        end if;
    end if;
    set i_code = 0;
    set c_desc = null;
end //
delimiter ;
