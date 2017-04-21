delimiter //
drop procedure if exists sp_query_platform_account_all //
create procedure sp_query_platform_account_all (
    out i_code  integer,
    out c_desc  mediumblob
)  
begin
    declare i_paid      integer         default -1;     -- 平台账户ID
    declare c_user      varchar(32)     default null;   -- 用户名
    declare c_pass      varchar(32)     default null;   -- 密码
    declare c_name      varchar(32)     default null;   -- 姓名
    declare c_mobile    varchar(20)     default null;   -- 手机
    declare c_email     varchar(32)     default null;   -- 邮箱
    declare t_birth     date            default null;   -- 出生日期
    declare i_cash      decimal(9, 2)   default 0.0;    -- 现金（可退的）
    declare i_coupon    decimal(9, 2)   default 0.0;    -- 优惠券（不可退）
    declare t_create    datetime        default null;   -- 创建时间
    declare c_url_cover varchar(255)    default null;

    declare done            integer default 0;
    declare rs              cursor for
                            select pa.i_paid, pa.c_user, pa.c_pass, pa.c_name, pa.c_mobile, pa.c_email, pa.t_birth, pa.i_cash, pa.i_coupon, pa.t_create, pa.c_url_cover
                              from tbl_platform_account pa
                             order by pa.c_user;
    /* 异常处理 */
    declare continue handler for sqlstate '02000' set done = 1;

    /* 打开游标 */
    open rs;  
    /* 逐个取出当前记录i_gaid值*/
    fetch rs into i_paid, c_user, c_pass, c_name, c_mobile, c_email, t_birth, i_cash, i_coupon, t_create, c_url_cover;
    /* 遍历数据表 */
    while (done = 0) do
        if c_desc is null then set c_desc = '';
        else set c_desc = concat(c_desc, '\n');
        end if;

        set c_desc = concat(
                c_desc,
                conv(i_paid, 10, 16),
                '\t',
                ifnull(c_user, ''),
                '\t',
                ifnull(c_pass, ''),
                '\t',
                ifnull(c_name, ''),
                '\t',
                ifnull(c_mobile, ''),
                '\t',
                ifnull(c_email, ''),
                '\t',
                ifnull(t_birth, ''),
                '\t',
                ifnull(i_cash, 0.0),
                '\t',
                ifnull(i_coupon, 0.0),
                '\t',
                ifnull(t_create,  ''),
                '\t',
                ifnull(c_url_cover,  '')
        );

        fetch rs into i_paid, c_user, c_pass, c_name, c_mobile, c_email, t_birth, i_cash, i_coupon, t_create, c_url_cover;
    end while;
    /* 关闭游标 */
    close rs;

    set i_code = 0;
end //  
delimiter ; 
