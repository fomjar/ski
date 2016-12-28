
delete from tbl_instruction where i_inst = (conv('00002001', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00002001', 16, 10) + 0),
    'sp',
    2
    "sp_update_user(?, ?, $uid, \"$pass\", \"$phone\", \"$email\", \"$cover\", \"$name\", $gender)"
);
delimiter //
drop procedure if exists sp_update_user //
create procedure sp_update_user (
    out i_code  integer,
    out c_desc  mediumtext,
    in  uid     integer,
    in  pass    varchar(16),
    in  phone   varchar(16),
    in  email   varchar(32),
    in  cover   varchar(256),
    in  name    varchar(32),
    in  gender  tinyint
)
begin

    declare di_count    integer default 0;
    declare di_uid      integer default 0;

    if uid is null then
        select count(1)
          into di_count
          from tbl_user
         where c_phone = phone;

        if di_count > 0 then
            set i_code = 5;
            set c_desc = 'phone already registered';
        else
            select max(i_uid)
              into di_uid
              from tbl_user;

            set di_uid = ifnull(di_uid, 0);
            set di_uid = di_uid + 1;

            insert into tbl_user (
                i_uid,
                t_create,
                c_pass,
                c_phone,
                c_email,
                c_cover,
                c_name,
                i_gender
            ) values (
                di_uid,
                now(),
                pass,
                phone,
                email,
                name,
                cover,
                gender
            );

            set i_code = 0;
            set c_desc = concat(di_uid, '');
        end if;
    else
        set di_uid = uid;

        select count(1)
          into di_count
          from tbl_user
         where i_uid = di_uid;

        if di_count = 0 then
            select count(1)
              into di_count
              from tbl_user
             where c_phone = phone;

            if di_count > 0 then
                set i_code = 5;
                set c_desc = 'phone already registered';
            else
                insert into tbl_user (
                    i_uid,
                    t_create,
                    c_pass,
                    c_phone,
                    c_email,
                    c_cover,
                    c_name,
                    i_gender
                ) values (
                    di_uid,
                    now(),
                    pass,
                    phone,
                    email,
                    name,
                    cover,
                    gender
                );

                set i_code = 0;
                set c_desc = concat(di_uid, '');
            end if;
        else
            if pass is not null then
                update tbl_user
                   set c_pass = pass
                 where i_uid = di_uid;
            end if;
            if phone is not null then
                update tbl_user
                   set c_phone = phone
                 where i_uid = di_uid;
            end if;
            if email is not null then
                update tbl_user
                   set c_email = email
                 where i_uid = di_uid;
            end if;
            if cover is not null then
                update tbl_user
                   set c_cover = cover
                 where i_uid = di_uid;
            end if;
            if name is not null then
                update tbl_user
                   set c_name = name
                 where i_uid = di_uid;
            end if;
            if gender is not null then
                update tbl_user
                   set i_gender = gender
                 where i_uid = di_uid;
            end if;

            set i_code = 0;
            set c_desc = concat(di_uid, '');
        end if;
    end if;
end //
delimiter ;



delete from tbl_instruction where i_inst = (conv('00002002', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00002002', 16, 10) + 0),
    'sp',
    2
    "sp_update_article(?, ?, $aid, $author, \"$location\", $weather, \"$title\", $status)"
);
delimiter //
drop procedure if exists sp_update_article //
create procedure sp_update_article (
    out i_code      integer,
    out c_desc      mediumtext,
    in  aid         integer,
    in  author      integer,
    in  location    varchar(64),
    in  weather     tinyint,
    in  title       varchar(64),
    in  status      tinyint
)
begin

    declare di_aid      integer default 0;
    declare di_count    integer default 0;

    if aid is null then
        select max(i_aid)
          into di_aid
          from tbl_article;

        set di_aid = ifnull(di_aid, 0);
        set di_aid = di_aid + 1;

        insert into tbl_article (
            i_aid,
            i_author,
            t_create,
            t_modify,
            c_location,
            i_weather,
            c_title,
            i_status
        ) values (
            di_aid,
            author,
            now(),
            now(),
            location,
            weather,
            title,
            status
        );

        set i_code = 0;
        set c_desc = concat(di_aid, '');
    else
        set di_aid = aid;

        select count(1)
          into di_count
          from tbl_article
         where i_aid = aid;

        if di_count = 0 then
            insert into tbl_article (
                i_aid,
                i_author,
                t_create,
                t_modify,
                c_location,
                i_weather,
                c_title,
                i_status
            ) values (
                di_aid,
                author,
                now(),
                now(),
                location,
                weather,
                title,
                status
            );
        else
            if author is not null then
                update tbl_article
                   set i_author = author
                 where i_aid = di_aid;
            end if;
            if location is not null then
                update tbl_article
                   set c_location = location
                 where i_aid = di_aid;
            end if;
            if weather is not null then
                update tbl_article
                   set i_weather = weather
                 where i_aid = di_aid;
            end if;
            if title is not null then
                update tbl_article
                   set c_title = title
                 where i_aid = di_aid;
            end if;
            if status is not null then
                update tbl_article
                   set i_status = status
                 where i_aid = di_aid;
            end if;

            -- update modify time
            update tbl_article
               set t_modify = now()
             where i_aid = di_aid;
        end if;

        set i_code = 0;
        set c_desc = concat(di_aid, '');
    end if;

end //
delimiter ;



delete from tbl_instruction where i_inst = (conv('00002003', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00002003', 16, 10) + 0),
    'sp',
    2
    "sp_update_paragraph(?, ?, $aid, $pid, $psn)"
);
delimiter //
drop procedure if exists sp_update_paragraph //
create procedure sp_update_paragraph (
    out i_code      integer,
    out c_desc      mediumtext,
    in  aid         integer,
    in  pid         integer,
    in  psn         integer
)
begin

    declare di_count    integer default 0;
    declare di_pid      integer default 0;
    declare di_psn      integer default 0;

    if aid is null then
        set i_code = 3;
        set c_desc = 'aid must be not null';
    else
        if pid is null then
            if pid is not null then
                set di_pid = pid;
            else
                select max(i_pid)
                  into di_pid
                  from tbl_paragraph
                 where i_aid = aid;

                set di_pid = ifnull(di_pid, 0);
                set di_pid = di_pid + 1;
            end if;

            if psn is not null then
                set di_psn = psn;
            else
                select max(i_psn)
                  into di_psn
                  from tbl_paragraph
                 where i_aid = aid;

                set di_psn = ifnull(di_psn, 0);
                set di_psn = di_psn + 1;
            end if;

            insert into tbl_paragraph (
                i_aid,
                i_pid,
                i_psn
            ) values (
                aid,
                di_pid,
                di_psn
            );

            set i_code = 0;
            set c_desc = concat(di_pid, '');
        else
            select count(1)
              into di_count
              from tbl_paragraph
             where i_aid = aid
               and i_pid = pid;

            if 
        end if;
    end if;
end //
delimiter ;
