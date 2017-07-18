delete from tbl_instruction where i_inst = (conv('0000240D', 16, 10) + 0);
insert into tbl_instruction values((conv('0000240D', 16, 10) + 0), 'sp', 2, "sp_update_ticket(?, ?, $tid, $caid, '$open', '$close', $type, '$title', '$content', $state, '$result')");

-- 更新游戏
delimiter //
drop procedure if exists sp_update_ticket //
create procedure sp_update_ticket (
    out i_code      integer,
    out c_desc      mediumblob,
    in  tid         integer,        -- 工单编号
    in  caid        integer,        -- 渠道用户ID
    in  _open       datetime,       -- 打开时间
    in  _close      datetime,       -- 关闭时间
    in  type        tinyint,        -- 0-退款申请，1-意见建议
    in  title       varchar(64),    -- 工单标题
    in  content     varchar(512),   -- 工单内容
    in  state       tinyint,        -- 工单状态：0-open, 1-close, 2-cancel
    in  result      varchar(64)     -- 处理结果
)
begin
    declare di_tid      integer default -1;
    declare di_count    integer default -1;

    if tid is null then
        select count(1)
          into di_count
          from tbl_ticket;

        if di_count = 0 then
            set di_tid = 1;
        else
            select max(i_tid)
              into di_tid
              from tbl_ticket;

            set di_tid = di_tid + 1;
        end if;

        insert into tbl_ticket (
            i_tid,
            i_caid,
            t_open,
            t_close,
            i_type,
            c_title,
            c_content,
            i_state,
            c_result
        ) values (
            di_tid,
            caid,
            ifnull(_open, now()),
            _close,
            type,
            title,
            content,
            ifnull(state, 0),   -- open
            result
        );

        set c_desc = conv(di_tid, 10, 16);
    else
        select count(1)
          into di_count
          from tbl_ticket
         where i_tid = tid;

        if di_count <= 0 then
            insert into tbl_ticket (
                i_tid,
                i_caid,
                t_open,
                t_close,
                i_type,
                c_title,
                c_content,
                i_state,
                c_result
            ) values (
                di_tid,
                caid,
                ifnull(_open, now()),
                _close,
                type,
                title,
                content,
                ifnull(state, 0),   -- open
                result
            );
        else
            if caid is not null then
                update tbl_ticket
                   set i_caid = caid
                 where i_tid = tid;
            end if;
            if _open is not null then
                update tbl_ticket
                   set t_open = _open
                 where i_tid = tid;
            end if;
            if _close is not null then
                update tbl_ticket
                   set t_close = _close
                 where i_tid = tid;
            end if;
            if type is not null then
                update tbl_ticket
                   set i_type = type
                 where i_tid = tid;
            end if;
            if title is not null then
                update tbl_ticket
                   set c_title = title
                 where i_tid = tid;
            end if;
            if content is not null then
                update tbl_ticket
                   set c_content = content
                 where i_tid = tid;
            end if;
            if state is not null then
                update tbl_ticket
                   set i_state = state
                 where i_tid = tid;
            end if;
            if result is not null then
                update tbl_ticket
                   set c_result = result
                 where i_tid = tid;
            end if;
        end if;
        
        set c_desc = conv(tid, 10, 16);
    end if;
    set i_code = 0;
end //
delimiter ;
