
delete from tbl_instruction where i_inst = (conv('00003001', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00003001', 16, 10) + 0),
    'sp',
    2,
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



delete from tbl_instruction where i_inst = (conv('00003002', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00003002', 16, 10) + 0),
    'sp',
    2,
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



delete from tbl_instruction where i_inst = (conv('00003003', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00003003', 16, 10) + 0),
    'sp',
    2,
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
            select max(i_pid)
              into di_pid
              from tbl_paragraph
             where i_aid = aid;

            set di_pid = ifnull(di_pid, 0);
            set di_pid = di_pid + 1;

            if psn is not null then
                select count(1)
                  into di_count
                  from tbl_paragraph
                 where i_aid = aid
                   and i_psn >= psn;

                if di_count > 0 then
                    update tbl_paragraph
                       set i_psn = i_psn + 1
                     where i_aid = aid
                       and i_psn >= psn;
                end if;

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
            set di_pid = pid;

            select count(1)
              into di_count
              from tbl_paragraph
             where i_aid = aid
               and i_pid = di_pid;

            if di_count = 0 then
                if psn is not null then
                    select count(1)
                      into di_count
                      from tbl_paragraph
                     where i_aid = aid
                       and i_psn >= psn;

                    if di_count > 0 then
                        update tbl_paragraph
                           set i_psn = i_psn + 1
                         where i_aid = aid
                           and i_psn >= psn;
                    end if;

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
                if psn is null then
                    set i_code = 3;
                    set c_desc = 'psn must be not null';
                else
                    select count(1)
                      into di_count
                      from tbl_paragraph
                     where i_aid = aid
                       and i_psn >= psn;

                    if di_count > 0 then
                        update tbl_paragraph
                           set i_psn = i_psn + 1
                         where i_aid = aid
                           and i_psn >= psn;
                    end if;

                    set di_psn = psn;

                    update tbl_paragraph
                       set i_psn = di_psn
                     where i_aid = aid
                       and i_pid = di_pid;

                    set i_code = 0;
                    set c_desc = concat(di_pid, '');
                end if;
            end if;
        end if;
    end if;
end //
delimiter ;




delete from tbl_instruction where i_inst = (conv('00003004', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00003004', 16, 10) + 0),
    'sp',
    2,
    "sp_update_paragraph_del(?, ?, $aid, $pid)"
);
delimiter //
drop procedure if exists sp_update_paragraph_del //
create procedure sp_update_paragraph_del (
    out i_code      integer,
    out c_desc      mediumtext,
    in  aid         integer,
    in  pid         integer
)
begin

    declare di_count    integer default 0;
    declare di_psn      integer default 0;

    select count(1)
      into di_count
      from tbl_paragraph
     where i_aid = aid
       and i_pid = pid;

    if di_count = 0 then
        set i_code = 3;
        set c_desc = 'no paragraph found';
    else
        select i_psn
          into di_psn
          from tbl_paragraph
         where i_aid = aid
           and i_pid = pid;

        delete from tbl_paragraph
         where i_aid = aid
           and i_pid = pid;

        update tbl_paragraph
           set i_psn = i_psn - 1
         where i_aid = aid
           and i_psn > di_psn;

        call sp_update_element_del(i_code, c_desc, pid, null);
    end if;
end //
delimiter ;




delete from tbl_instruction where i_inst = (conv('00003005', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00003005', 16, 10) + 0),
    'sp',
    2,
    "sp_update_element(?, ?, $pid, $eid, $esn, $et, \"$ec\")"
);
delimiter //
drop procedure if exists sp_update_element //
create procedure sp_update_element (
    out i_code      integer,
    out c_desc      mediumtext,
    in  pid         integer,
    in  eid         integer,
    in  esn         integer,
    in  et          tinyint,
    in  ec          mediumtext
)
begin

    declare di_count    integer default 0;
    declare di_eid      integer default 0;
    declare di_esn      integer default 0;

    if pid is null then
        set i_code = 3;
        set c_desc = 'pid must be not null';
    else
        if eid is null then
            select max(i_eid)
              into di_eid
              from tbl_element;

            set di_eid = ifnull(di_eid, 0);
            set di_eid = di_eid + 1;

            if esn is null then
                select max(i_esn)
                  into di_esn
                  from tbl_element
                 where i_pid = pid;

                set di_esn = di_esn + 1;
            else
                update tbl_element
                   set i_esn = i_esn + 1
                 where i_pid = pid
                   and i_esn >= esn;

                set di_esn = esn;
            end if;

            insert into tbl_element (
                i_pid,
                i_eid,
                i_esn,
                i_et,
                c_ec
            ) values (
                pid,
                di_eid,
                di_esn,
                et,
                ec
            );
        else
            set di_eid = eid;

            select count(1)
              into di_count
              from tbl_element
             where i_pid = pid
               and i_eid = di_eid;
            
            if di_count = 0 then
                if esn is null then
                    select max(i_esn)
                      into di_esn
                      from tbl_element
                     where i_pid = pid;

                    set di_esn = di_esn + 1;
                else
                    update tbl_element
                       set i_esn = i_esn + 1
                     where i_pid = pid
                       and i_esn >= esn;

                    set di_esn = esn;
                end if;

                insert into tbl_element (
                    i_pid,
                    i_eid,
                    i_esn,
                    i_et,
                    c_ec
                ) values (
                    pid,
                    di_eid,
                    di_esn,
                    et,
                    ec
                );
            else
                if esn is not null then
                    update tbl_element
                       set i_esn = esn
                     where i_pid = pid
                       and i_eid = di_eid;
                end if;
                if et is not null then
                    update tbl_element
                       set i_et = et
                     where i_pid = pid
                       and i_eid = di_eid;
                end if;
                if ec is not null then
                    update tbl_element
                       set c_ec = ec
                     where i_pid = pid
                       and i_eid = di_eid;
                end if;

            end if;
        end if;

        set i_code = 0;
        set c_desc = concat(di_eid, '');
    end if;
end //
delimiter ;




delete from tbl_instruction where i_inst = (conv('00003006', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00003006', 16, 10) + 0),
    'sp',
    2,
    "sp_update_element_del(?, ?, $pid, $eid)"
);
delimiter //
drop procedure if exists sp_update_element_del //
create procedure sp_update_element_del (
    out i_code      integer,
    out c_desc      mediumtext,
    in  pid         integer,
    in  eid         integer
)
begin

    declare di_count    integer default 0;
    declare di_esn      integer default 0;

    if pid is null then
        set i_code = 3;
        set c_desc = 'pid must be not null';
    else
        if eid is null then
            select count(1)
              into di_count
              from tbl_element
             where i_pid = pid;

            delete from tbl_element
             where i_pid = pid;
        else
            select count(1)
              into di_count
              from tbl_element
             where i_pid = pid
               and i_eid = eid;

            if di_count > 0 then
                select i_esn
                  into di_esn
                  from tbl_element
                 where i_pid = pid
                   and i_eid = eid;

                delete from tbl_element
                 where i_pid = pid
                   and i_eid = eid;

                update tbl_element
                   set i_esn = i_esn - 1
                 where i_pid = pid
                   and i_esn >= di_esn;
            end if;
        end if;

        set i_code = 0;
        set c_desc = concat(di_count, '');
    end if;

end //
delimiter ;





delete from tbl_instruction where i_inst = (conv('00003007', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00003007', 16, 10) + 0),
    'sp',
    2,
    "sp_update_tag(?, ?, $tid, $it, $io, \"$tag\")"
);
delimiter //
drop procedure if exists sp_update_tag //
create procedure sp_update_tag (
    out i_code      integer,
    out c_desc      mediumtext,
    in  tid         integer,
    in  it          integer,
    in  io          integer,
    in  tag         varchar(256)
)
begin

    declare di_count    integer default 0;
    declare di_tid      integer default 0;

    if tid is null then
        select max(i_tid)
          into di_tid
          from tbl_tag;

        set di_tid = ifnull(di_tid, 0);
        set di_tid = di_tid + 1;

        insert into tbl_tag (
            i_tid,
            i_it,
            i_io,
            c_tag
        ) values (
            di_tid,
            it,
            io,
            tag
        );

        set i_code = 0;
        set c_desc = concat(di_tid, '');
    else
        set di_tid = tid;

        select count(1)
          into di_count
          from tbl_tag
         where i_tid = di_tid;

        if di_count = 0 then
            insert into tbl_tag (
                i_tid,
                i_it,
                i_io,
                c_tag
            ) values (
                di_tid,
                it,
                io,
                tag
            );

            set i_code = 0;
            set c_desc = concat(di_tid, '');
        else
            if it is not null then
                update tbl_tag
                   set i_it = it
                 where i_tid = di_tid;
            end if;
            if io is not null then
                update tbl_tag
                   set i_io = io
                 where i_tid = di_tid;
            end if;
            if tag is not null then
                update tbl_tag
                   set c_tag = tag
                 where i_tid = di_tid;
            end if;

            set i_code = 0;
            set c_desc = concat(di_tid, '');
        end if;
    end if;

end //
delimiter ;



delete from tbl_instruction where i_inst = (conv('00003008', 16, 10) + 0);
insert into tbl_instruction (
    i_inst,
    c_mode,
    i_out,
    c_sql
) values (
    (conv('00003008', 16, 10) + 0),
    'sp',
    2,
    "sp_update_tag_del(?, ?, $tid)"
);
delimiter //
drop procedure if exists sp_update_tag_del //
create procedure sp_update_tag_del (
    out i_code      integer,
    out c_desc      mediumtext,
    in  tid         integer
)
begin

    declare di_count    integer default 0;

    select count(1)
      into di_count
      from tbl_tag
     where i_tid = tid;

    delete from tbl_tag
     where i_tid = tid;

    set i_code = 0;
    set c_desc = concat(di_count, '');

end //
delimiter ;


     
delete from tbl_instruction where i_inst = (conv('00003008', 16, 10) + 0);        
insert into tbl_instruction (                                                     
    i_inst,
    c_mode,
    i_out,  
    c_sql   
) values (  
    (conv('00003008', 16, 10) + 0),
    'sp',   
    2,      
    "sp_update_tag_del(?, ?, $tid)"
);          
delimiter //
drop procedure if exists sp_update_tag_del //                                     
create procedure sp_update_tag_del (
    out i_code      integer,
    out c_desc      mediumtext,                                                   
    in  tid         integer
)        
begin      

end //
delimiter ;
