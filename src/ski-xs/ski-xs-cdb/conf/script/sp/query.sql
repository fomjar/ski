
delete from tbl_instruction where i_inst = (conv('00004001', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00004001', 16, 10) + 0),
    'st',
    7,
    "select u.i_uid, u.t_create, u.c_phone, u.c_email, u.c_cover, u.c_name, u.i_gender from tbl_user u, tbl_user_state s where u.i_uid = s.i_uid and s.c_token = \"token\""
);




delete from tbl_instruction where i_inst = (conv('00004002', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00004002', 16, 10) + 0),
    'sp',
    2,
    "sp_query_article_by_aid(?, ?, $aid)"
);
delimiter //
drop procedure if exists sp_query_article_by_aid //
create procedure sp_query_article_by_aid (
    out i_code  integer,
    out c_desc  mediumtext, 
    in  aid     integer
)   
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

    if di_count = 0 then
        set i_code = 3;
        set c_desc = 'article not found';
    else
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

        set i_code = 0;
        set c_desc = dj_article;
    end if;
end //
delimiter ;


