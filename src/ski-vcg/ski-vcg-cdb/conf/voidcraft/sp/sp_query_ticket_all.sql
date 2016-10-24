delimiter //
drop procedure if exists sp_query_ticket_all //
create procedure sp_query_ticket_all (
    out i_code  integer,
    out c_desc  mediumblob
)  
begin
    declare i_tid       integer         default -1;     -- 工单编号
    declare i_caid      integer         default -1;     -- 渠道用户ID
    declare i_type      tinyint         default -1;     -- 0-退款申请，1-意见建议
    declare t_open      datetime        default null;   -- 打开时间
    declare t_close     datetime        default null;   -- 关闭时间
    declare c_title     varchar(64)     default null;   -- 工单标题
    declare c_content   varchar(512)    default null;   -- 工单内容
    declare i_state     tinyint         default -1;     -- 工单状态：0-open, 1-close, 2-cancel
    declare c_result    varchar(64)     default null;   -- 处理结果

    declare done            integer default 0;
    declare rs              cursor for
                            select t.i_tid, t.i_caid, t.i_type, t.t_open, t.t_close, t.c_title, t.c_content, t.i_state, t.c_result
                              from tbl_ticket t
                             order by t.i_type, t.t_open;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_tid, i_caid, i_type, t_open, t_close, c_title, c_content, i_state, c_result;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_tid, 10, 16),
                '\t',
                conv(i_caid, 10, 16),
                '\t',
                conv(i_type, 10, 16),
                '\t',
                ifnull(t_open, ''),
                '\t',
                ifnull(t_close, ''),
                '\t',
                ifnull(c_title, ''),
                '\t',
                ifnull(c_content, ''),
                '\t',
                conv(i_state, 10, 16),
                '\t',
                ifnull(c_result, '')
        );

        fetch rs into i_tid, i_caid, i_type, t_open, t_close, c_title, c_content, i_state, c_result;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
