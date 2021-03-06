
delimiter //
drop function if exists fn_get_article_by_aid //
create function fn_get_article_by_aid (
    aid integer
)
returns json
begin

    declare di_count        integer     default 0;
    declare dj_article      json        default null;
    declare dj_paragraph    json        default null;
    declare di_pid          integer     default 0;
    declare di_psn          integer     default 0;
    declare di_esn          integer     default 0;
    declare di_et           integer     default 0;
    declare dc_ec           mediumtext  default null;

    declare done        integer default 0;
    declare rs          cursor for
                        select p.i_pid, p.i_psn, e.i_esn, e.i_et, e.c_ec
                          from tbl_article a, tbl_paragraph p, tbl_element e
                         where a.i_aid = p.i_aid
                           and p.i_pid = e.i_pid
                           and a.i_aid = aid
                         order by p.i_psn, e.i_esn;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    select count(1)
      into di_count
      from tbl_article
     where i_aid = aid;

    if di_count > 0 then
        /* article */
        select json_object(
            'aid',          a.i_aid,
            'uid',          u.i_uid,
            'ucover',       u.c_cover,
            'uname',        u.c_name,
            'ugender',      u.i_gender,
            'create',       a.t_create,
            'modify',       a.t_modify,
            'location',     a.c_location,
            'weather',      a.i_weather,
            'title',        a.c_title,
            'status',       a.i_status,
            'paragraph',    json_array()
        ) into dj_article
          from tbl_article a, tbl_user u
         where a.i_uid = u.i_uid
           and a.i_aid = aid;

        /* paragraph & element */
        open rs;  
        fetch rs into di_pid, di_psn, di_esn, di_et, dc_ec;
        while (done = 0) do

            if dj_paragraph is null then    -- first paragraph
                set dj_paragraph = json_object(
                    'pid',      di_pid,
                    'psn',      di_psn,
                    'element',  json_array(
                        json_object(
                            'esn',  di_esn,
                            'et',   di_et,
                            'ec',   ec_ec
                        )
                    )
                );
            else
                if json_extract(dj_paragraph, '$.psn') = di_psn then    -- same paragraph, combine
                    set dj_paragraph = json_array_append(dj_paragraph, '$.element', json_object(
                        'esn',  di_esn,
                        'et',   di_et,
                        'ec',   dc_ec
                    ));
                else    -- next paragraph
                    set dj_article = json_array_append(dj_article, '$.paragraph', dj_paragraph);

                    set dj_paragraph = json_object(
                        'pid',      di_pid,
                        'psn',      di_psn,
                        'element',  json_array(
                            json_object(
                                'esn',  di_esn,
                                'et',   di_et,
                                'ec',   ec_ec
                            )
                        )
                    );
                end if;
            end if;

            fetch rs into di_pid, di_psn, di_esn, di_et, dc_ec;
        end while;
        /* 关闭游标 */
        close rs;

        if dj_paragraph is not null then    -- last paragraph
            set dj_article = json_array_append(dj_article, '$.paragraph', dj_paragraph);
        end if;
    end if;

    return dj_article;
end //
delimiter ;

