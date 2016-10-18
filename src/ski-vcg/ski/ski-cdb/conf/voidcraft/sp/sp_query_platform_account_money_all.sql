delimiter //
drop procedure if exists sp_query_platform_account_money_all //
create procedure sp_query_platform_account_money_all (
    out i_code  integer,
    out c_desc  mediumblob
)  
begin
    declare i_paid      integer         default -1;     -- 平台账户ID
    declare c_remark    varchar(64)     default null;   -- 备注
    declare t_time      datetime        default null;   -- 时间
    declare i_type      tinyint         default -1;     -- 0-余额，1-优惠券
    declare i_base      decimal(9, 2)   default 0.00;   -- 基准值
    declare i_money     decimal(9, 2)   default 0.00;   -- 充值(变化值)

    declare done            integer default 0;
    declare rs              cursor for
                            select pam.i_paid, pam.c_remark, pam.t_time, pam.i_type, pam.i_base, pam.i_money
                              from tbl_platform_account_money pam
                             order by pam.i_paid, pam.t_time;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_paid, c_remark, t_time, i_type, i_base, i_money;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_paid, 10, 16),
                '\t',
                c_remark,
                '\t',
                t_time,
                '\t',
                i_type,
                '\t',
                i_base,
                '\t',
                i_money
        );

        fetch rs into i_paid, c_remark, t_time, i_type, i_base, i_money;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
