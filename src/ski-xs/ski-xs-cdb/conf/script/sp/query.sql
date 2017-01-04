
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

    declare dj_article  json    default null;

    set dj_article = fn_get_article_by_aid(aid);
    if dj_article is null then
        set i_code = 3;
        set c_desc = 'article not found';
    else
        set i_code = 0;
        set c_desc = dj_article;
    end if;

end //
delimiter ;


