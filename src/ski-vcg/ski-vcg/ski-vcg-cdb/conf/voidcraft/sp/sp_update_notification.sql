delete from tbl_instruction where i_inst = (conv('0000240E', 16, 10) + 0);
insert into tbl_instruction values((conv('0000240E', 16, 10) + 0), 'sp', 2, "sp_update_notification(?, ?, $nid, $caid, '$content', '$create')");

-- 更新游戏
delimiter //
drop procedure if exists sp_update_notification //
create procedure sp_update_notification (
    out i_code      integer,
    out c_desc      mediumblob,
    in  nid         integer,        -- 通知编号
    in  caid        integer,        -- 被通知的用户
    in  content     varchar(64),    -- 通知内容
    in  _create     datetime        -- 创建通知的时间
)
begin
    declare di_nid      integer default -1;
    declare di_count    integer default -1;

    if nid is null then
        select count(1)
          into di_count
          from tbl_notification;

        if di_count = 0 then
            set di_nid = 1;
        else
            select max(i_nid)
              into di_nid
              from tbl_notification;

            set di_nid = di_nid + 1;
        end if;

        insert into tbl_notification (
            i_nid,
            i_caid,
            c_content,
            t_create
        ) values (
            di_nid,
            caid,
            content,
            ifnull(_create, now())
        );

        set c_desc = conv(di_nid, 10, 16);
    else
        select count(1)
          into di_count
          from tbl_notification
         where i_nid = nid;

        if di_count <= 0 then
            insert into tbl_notification (
                i_nid,
                i_caid,
                c_content,
                t_create
            ) values (
                di_nid,
                caid,
                content,
                ifnull(_create, now())
            );
        else
            if caid is not null then
                update tbl_notification
                   set i_caid = caid
                 where i_nid = nid;
            end if;
            if content is not null then
                update tbl_notification
                   set c_content = content
                 where i_nid = nid;
            end if;
            if _create is not null then
                update tbl_notification
                   set t_create = _create
                 where i_nid = nid;
            end if;
        end if;
        
        set c_desc = conv(nid, 10, 16);
    end if;
    set i_code = 0;
end //
delimiter ;
